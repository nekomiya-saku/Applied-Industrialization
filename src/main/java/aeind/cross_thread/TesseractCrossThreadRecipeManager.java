package aeind.cross_thread;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import aeind.isolation.RoomInputStorage;
import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationRoom;
import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.FluidInput;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.ItemInput;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.FluidOutput;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.ItemOutput;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition.Context;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.ModularCrafterAccessBehavior;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;
import net.swedz.tesseract.neoforge.compat.mi.helper.CrafterComponentHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Cross-thread execution for Tesseract's multiplied crafters (EI and IO arrays). */
public final class TesseractCrossThreadRecipeManager {
    private final Map<String, State> states = new LinkedHashMap<>();
    private long lastEuPerTick;

    public long lastEuPerTick() { return lastEuPerTick; }

    public List<CrossThreadRecipeManager.ThreadProgress> progress() {
        return states.values().stream().filter(State::hasWork).sorted(Comparator.comparing(s -> s.roomId))
            .map(s -> new CrossThreadRecipeManager.ThreadProgress(
                s.totalEnergy <= 0 ? 0 : (float)Math.min(1.0, (double)s.usedEnergy / s.totalEnergy),
                Math.max(1, s.parallel), s.outputsReady)).toList();
    }

    public boolean hasWork() { return states.values().stream().anyMatch(State::hasWork); }

    public boolean tick(MachineBlockEntity machine, MultipliedCrafterComponent crafter, ThreadIsolationAccess access, boolean crossThread) {
        CrafterComponent.Inventory inventory = (CrafterComponent.Inventory)crafter.getInventory();
        ModularCrafterAccessBehavior behavior = crafter.getBehavior();
        LinkedHashMap<String, ThreadIsolationRoom> rooms = new LinkedHashMap<>();
        for (ThreadIsolationRoom room : access.aeind$isolationRooms()) rooms.putIfAbsent(room.id(), room);
        boolean changed = flushOutputs(inventory);
        lastEuPerTick = 0;
        if (!behavior.isEnabled()) return finishTick(machine, rooms, changed);

        int arrayLimit = Math.max(1, crafter.getMaxMultiplier());
        int limit = crossThread ? Math.max(arrayLimit, access.aeind$maxParallelPerThread()) : 1;
        for (ThreadIsolationRoom room : rooms.values()) {
            if (!crossThread && hasWork()) break;
            State state = states.computeIfAbsent(room.id(), State::new);
            if (!state.hasWork()) changed |= start(machine, crafter, room, state, limit);
        }

        List<State> running = states.values().stream().filter(s -> s.running).toList();
        if (!running.isEmpty()) {
            long available = behavior.consumeEu(Long.MAX_VALUE, Simulation.SIMULATE);
            for (State state : running) {
                RecipeHolder<MachineRecipe> holder = getRecipe(crafter.getRecipeType(), behavior.getCrafterWorld(), state.recipeId);
                if (holder == null || !holder.value().conditionsMatch(context(machine))) continue;
                long perTick = Math.max(1L, transformed(crafter, holder.value().eu, state.parallel));
                long amount = Math.min(Math.min(perTick, state.totalEnergy - state.usedEnergy), available);
                if (amount <= 0) continue;
                long consumed = behavior.consumeEu(amount, Simulation.ACT);
                available -= consumed;
                state.usedEnergy = satAdd(state.usedEnergy, consumed);
                lastEuPerTick = satAdd(lastEuPerTick, consumed);
                changed |= consumed > 0;
                if (state.usedEnergy >= state.totalEnergy) {
                    finish(crafter, holder.value(), state);
                    changed = true;
                }
            }
        }
        changed |= flushOutputs(inventory);
        return finishTick(machine, rooms, changed);
    }

    private boolean finishTick(MachineBlockEntity machine, Map<String, ThreadIsolationRoom> rooms, boolean changed) {
        if (rooms.isEmpty() && !hasWork()) states.clear();
        else states.entrySet().removeIf(e -> !rooms.containsKey(e.getKey()) && !e.getValue().hasWork());
        if (changed) machine.setChanged();
        return lastEuPerTick > 0;
    }

    private boolean start(MachineBlockEntity machine, MultipliedCrafterComponent crafter, ThreadIsolationRoom room, State state, int limit) {
        ModularCrafterAccessBehavior behavior = crafter.getBehavior();
        RecipeHolder<MachineRecipe> selected = null;
        if (state.recipeId != null && state.efficiencyTicks > 0) selected = getRecipe(crafter.getRecipeType(), behavior.getCrafterWorld(), state.recipeId);
        if (selected == null) {
            List<ConfigurableItemStack> items = room.hasMapStorage() ? room.inputStorage().createMiView().itemInputs() : room.itemInputs();
            Collection<RecipeHolder<MachineRecipe>> candidates = new ArrayList<>();
            MachineRecipeType type = crafter.getRecipeType();
            if (type != null) {
                candidates.addAll(type.getFluidOnlyRecipes(behavior.getCrafterWorld()));
                for (ConfigurableItemStack item : items) if (!item.isEmpty()) candidates.addAll(type.getMatchingRecipes(behavior.getCrafterWorld(), item.getResource().getItem()));
            }
            for (RecipeHolder<MachineRecipe> candidate : candidates.stream().sorted(Comparator.comparing(h -> h.id().toString())).toList()) {
                if (!behavior.isRecipeBanned(candidate.value().eu) && candidate.value().conditionsMatch(context(machine)) && canRun(crafter, room, candidate.value(), limit)) { selected = candidate; break; }
            }
        } else if (!canRun(crafter, room, selected.value(), limit)) selected = null;
        if (selected == null) return false;
        int parallel = findParallel(crafter, room, selected.value(), limit);
        if (parallel <= 0 || !takeInputs(crafter, room, selected.value(), parallel)) return false;
        state.recipeId = selected.id(); state.parallel = parallel; state.running = true; state.outputsReady = false;
        state.usedEnergy = 0; state.totalEnergy = transformed(crafter, selected.value().getTotalEu(), parallel);
        state.efficiencyTicks = Math.max(1, state.efficiencyTicks); state.heldItems = maxItems(selected.value(), parallel); state.heldFluids = maxFluids(selected.value(), parallel, behavior.getMaxFluidOutputs());
        return true;
    }

    private static boolean canRun(MultipliedCrafterComponent crafter, ThreadIsolationRoom room, MachineRecipe recipe, int limit) {
        return findParallel(crafter, room, recipe, limit) > 0;
    }

    private static int findParallel(MultipliedCrafterComponent crafter, ThreadIsolationRoom room, MachineRecipe recipe, int limit) {
        int low = 1, high = Math.max(1, limit), best = 0;
        while (low <= high) { int mid = (low + high) >>> 1; if (canTake(crafter, room, recipe, mid) && canOutputs(crafter.getInventory(), recipe, mid, crafter.getBehavior().getMaxFluidOutputs())) { best = mid; low = mid + 1; } else high = mid - 1; }
        return best;
    }

    private static boolean canTake(MultipliedCrafterComponent crafter, ThreadIsolationRoom room, MachineRecipe recipe, int parallel) {
        CrafterComponent.Inventory inventory = (CrafterComponent.Inventory)crafter.getInventory();
        if (room.hasMapStorage()) {
            RoomInputStorage.MiInputView view = room.inputStorage().createMiView();
            return takeItems(crafter.getBehavior(), view.itemInputs(), recipe, parallel, true) && takeFluids(crafter.getBehavior(), view.fluidInputs(), recipe, parallel, true) && catalysts(room, recipe);
        }
        return CrafterComponentHelper.takeItemInputs(recipe, true, crafter.getBehavior(), inventory, parallel) && CrafterComponentHelper.takeFluidInputs(recipe, true, crafter.getBehavior(), inventory, parallel) && catalysts(room, recipe);
    }

    private static boolean takeInputs(MultipliedCrafterComponent crafter, ThreadIsolationRoom room, MachineRecipe recipe, int parallel) {
        if (!canTake(crafter, room, recipe, parallel)) return false;
        if (!room.hasMapStorage()) return CrafterComponentHelper.takeItemInputs(recipe, false, crafter.getBehavior(), (CrafterComponent.Inventory)crafter.getInventory(), parallel) && CrafterComponentHelper.takeFluidInputs(recipe, false, crafter.getBehavior(), (CrafterComponent.Inventory)crafter.getInventory(), parallel);
        RoomInputStorage storage = room.inputStorage(); RoomInputStorage.MiInputView view = storage.createMiView();
        if (!takeItems(crafter.getBehavior(), view.itemInputs(), recipe, parallel, false) || !takeFluids(crafter.getBehavior(), view.fluidInputs(), recipe, parallel, false)) return false;
        Map<AEKey, Long> consumed = view.consumedAmounts();
        return storage.extractAll(consumed, Actionable.SIMULATE) && storage.extractAll(consumed, Actionable.MODULATE);
    }

    private static boolean takeItems(ModularCrafterAccessBehavior behavior, List<ConfigurableItemStack> stacks, MachineRecipe recipe, int parallel, boolean simulate) {
        List<ConfigurableItemStack> working = simulate ? ConfigurableItemStack.copyList(stacks) : stacks;
        for (ItemInput input : recipe.itemInputs) { if (input.probability() == 0) continue; long need = input.amount() * (long)parallel; for (ConfigurableItemStack stack : working) if (stack.getAmount() > 0 && stack.getResource().test(input.ingredient())) { long used = Math.min(need, stack.getAmount()); stack.decrement(used); need -= used; if (need == 0) break; } if (need > 0) return false; } return true;
    }

    private static boolean takeFluids(ModularCrafterAccessBehavior behavior, List<ConfigurableFluidStack> stacks, MachineRecipe recipe, int parallel, boolean simulate) {
        List<ConfigurableFluidStack> working = simulate ? ConfigurableFluidStack.copyList(stacks) : stacks;
        for (FluidInput input : recipe.fluidInputs) { if (input.probability() == 0) continue; long need = input.amount() * (long)parallel; for (ConfigurableFluidStack stack : working) if (stack.getAmount() > 0 && input.fluid().test(stack.toStack())) { long used = Math.min(need, stack.getAmount()); stack.decrement(used); need -= used; if (need == 0) break; } if (need > 0) return false; } return true;
    }

    private static boolean catalysts(ThreadIsolationRoom room, MachineRecipe recipe) {
        for (ItemInput input : recipe.itemInputs) if (input.probability() == 0 && !hasCatalyst(room, input)) return false;
        for (FluidInput input : recipe.fluidInputs) if (input.probability() == 0 && !hasCatalyst(room, input)) return false;
        return true;
    }
    private static boolean hasCatalyst(ThreadIsolationRoom room, ItemInput input) { if (!room.hasCatalystStorage()) return false; for (var e : room.catalystStorage().getAvailableStacks()) if (e.getKey() instanceof AEItemKey item && input.ingredient().test(item.toStack()) && e.getLongValue() >= input.amount()) return true; return false; }
    private static boolean hasCatalyst(ThreadIsolationRoom room, FluidInput input) { if (!room.hasCatalystStorage()) return false; for (var e : room.catalystStorage().getAvailableStacks()) if (e.getKey() instanceof AEFluidKey fluid && input.fluid().test(fluid.toStack(1)) && e.getLongValue() >= input.amount()) return true; return false; }

    private static boolean canOutputs(aztech.modern_industrialization.api.machine.component.InventoryAccess raw, MachineRecipe recipe, int parallel, int maxFluids) {
        CrafterComponent.Inventory inv = (CrafterComponent.Inventory)raw;
        ArrayList<ConfigurableItemStack> items = ConfigurableItemStack.copyList(inv.getItemOutputs()); ArrayList<ConfigurableFluidStack> fluids = ConfigurableFluidStack.copyList(inv.getFluidOutputs());
        return insertItems(items, maxItems(recipe, parallel)) && insertFluids(fluids, maxFluids(recipe, parallel, maxFluids));
    }
    private static boolean insertItems(List<ConfigurableItemStack> into, List<ConfigurableItemStack> outputs) { for (ConfigurableItemStack out : outputs) { long need=out.getAmount(); for(ConfigurableItemStack slot:into) if(slot.isResourceAllowedByLock(out.getResource()) && (slot.isEmpty() || slot.getResource().equals(out.getResource()))) { long n=Math.min(need,slot.getRemainingCapacityFor(out.getResource())); if(slot.isEmpty()&&n>0)slot.setKey(out.getResource()); slot.increment(n); need-=n; if(need==0)break; } if(need>0)return false; } return true; }
    private static boolean insertFluids(List<ConfigurableFluidStack> into, List<ConfigurableFluidStack> outputs) { for(ConfigurableFluidStack out:outputs){long need=out.getAmount();for(ConfigurableFluidStack slot:into)if(slot.isResourceAllowedByLock(out.getResource())&&(slot.isEmpty()||slot.getResource().equals(out.getResource()))){long n=Math.min(need,slot.getRemainingSpace());if(slot.isEmpty()&&n>0){slot.setKey(out.getResource());slot.setCapacity(Math.max(slot.getCapacity(),n));}slot.increment(n);need-=n;if(need==0)break;}if(need>0)return false;}return true; }

    private void finish(MultipliedCrafterComponent crafter, MachineRecipe recipe, State state) { state.heldItems = rollItems(recipe, state.parallel); state.heldFluids = rollFluids(recipe, state.parallel, crafter.getBehavior().getMaxFluidOutputs()); state.running=false;state.outputsReady=true;state.usedEnergy=0;state.totalEnergy=0; }
    private boolean flushOutputs(CrafterComponent.Inventory inv) { boolean changed=false; for(State s:states.values())if(s.outputsReady){changed|=insertItems(inv.getItemOutputs(),s.heldItems);changed|=insertFluids(inv.getFluidOutputs(),s.heldFluids);s.heldItems.removeIf(AbstractConfigurableStack::isEmpty);s.heldFluids.removeIf(AbstractConfigurableStack::isEmpty);if(s.heldItems.isEmpty()&&s.heldFluids.isEmpty()){s.outputsReady=false;changed=true;}}return changed; }
    private static List<ConfigurableItemStack> maxItems(MachineRecipe r,int p){ArrayList<ConfigurableItemStack> x=new ArrayList<>();for(ItemOutput o:r.itemOutputs)addItem(x,o.variant(),o.amount()*(long)p);return x;}
    private static List<ConfigurableFluidStack> maxFluids(MachineRecipe r,int p,int max){ArrayList<ConfigurableFluidStack>x=new ArrayList<>();for(int i=0;i<Math.min(max,r.fluidOutputs.size());i++){FluidOutput o=r.fluidOutputs.get(i);addFluid(x,o.fluid(),o.amount()*(long)p);}return x;}
    private static List<ConfigurableItemStack> rollItems(MachineRecipe r,int p){ArrayList<ConfigurableItemStack>x=new ArrayList<>();for(ItemOutput o:r.itemOutputs){long n=o.amount()*(long)occurrences(p,o.probability());if(n>0)addItem(x,o.variant(),n);}return x;}
    private static List<ConfigurableFluidStack> rollFluids(MachineRecipe r,int p,int max){ArrayList<ConfigurableFluidStack>x=new ArrayList<>();for(int i=0;i<Math.min(max,r.fluidOutputs.size());i++){FluidOutput o=r.fluidOutputs.get(i);long n=o.amount()*(long)occurrences(p,o.probability());if(n>0)addFluid(x,o.fluid(),n);}return x;}
    private static int occurrences(int count,float probability){int n=0;for(int i=0;i<count;i++)if(ThreadLocalRandom.current().nextFloat()<=probability)n++;return n;}
    private static void addItem(List<ConfigurableItemStack>x,ItemVariant k,long n){for(ConfigurableItemStack s:x)if(s.getResource().equals(k)){s.setAmount(s.getAmount()+n);return;}ConfigurableItemStack s=new ConfigurableItemStack();s.setKey(k);s.setAmount(n);x.add(s);}
    private static void addFluid(List<ConfigurableFluidStack>x,Fluid f,long n){ConfigurableFluidStack s=new ConfigurableFluidStack(n);s.setKey(FluidVariant.of(f));s.setAmount(n);x.add(s);}
    private static long transformed(MultipliedCrafterComponent crafter,long eu,int parallel){aeind.mixin.MultipliedCrafterComponentAccessor access=(aeind.mixin.MultipliedCrafterComponentAccessor)(Object)crafter;int old=access.aeind$getRecipeMultiplier();access.aeind$setRecipeMultiplier(parallel);try{return Math.max(1,crafter.transformEuCost(eu,0));}finally{access.aeind$setRecipeMultiplier(old);}}
    private static RecipeHolder<MachineRecipe> getRecipe(MachineRecipeType type,ServerLevel world,ResourceLocation id){return type==null||id==null?null:type.getRecipe(world,id);}
    private static Context context(MachineBlockEntity machine){return ()->machine;}
    private static long satAdd(long a,long b){return Long.MAX_VALUE-a<b?Long.MAX_VALUE:a+b;}

    public void writeNbt(CompoundTag tag, HolderLookup.Provider lookup){ListTag list=new ListTag();for(State s:states.values()){CompoundTag n=new CompoundTag();n.putString("room",s.roomId);if(s.recipeId!=null)n.putString("recipe",s.recipeId.toString());n.putInt("parallel",s.parallel);n.putLong("used",s.usedEnergy);n.putLong("total",s.totalEnergy);n.putInt("efficiency",s.efficiencyTicks);n.putBoolean("running",s.running);n.putBoolean("outputs",s.outputsReady);ListTag items=new ListTag();for(ConfigurableItemStack stack:s.heldItems)items.add(stack.toNbt(lookup));ListTag fluids=new ListTag();for(ConfigurableFluidStack stack:s.heldFluids)fluids.add(stack.toNbt(lookup));n.put("items",items);n.put("fluids",fluids);list.add(n);}tag.put("threads",list);}
    public void readNbt(CompoundTag tag, HolderLookup.Provider lookup){states.clear();ListTag list=tag.getList("threads",10);for(int i=0;i<list.size();i++){CompoundTag n=list.getCompound(i);String room=n.getString("room");if(room.isEmpty())continue;State s=new State(room);s.recipeId=n.contains("recipe")?ResourceLocation.tryParse(n.getString("recipe")):null;s.parallel=Math.max(1,n.getInt("parallel"));s.usedEnergy=Math.max(0,n.getLong("used"));s.totalEnergy=Math.max(0,n.getLong("total"));s.efficiencyTicks=Math.max(0,n.getInt("efficiency"));s.running=n.getBoolean("running");s.outputsReady=n.getBoolean("outputs");ListTag items=n.getList("items",10);for(int j=0;j<items.size();j++)s.heldItems.add(new ConfigurableItemStack(items.getCompound(j),lookup));ListTag fluids=n.getList("fluids",10);for(int j=0;j<fluids.size();j++)s.heldFluids.add(new ConfigurableFluidStack(fluids.getCompound(j),lookup));states.put(room,s);}}
    private static final class State { final String roomId; ResourceLocation recipeId; int parallel=1; long usedEnergy,totalEnergy;int efficiencyTicks;boolean running,outputsReady;List<ConfigurableItemStack> heldItems=new ArrayList<>();List<ConfigurableFluidStack> heldFluids=new ArrayList<>();State(String id){roomId=id;}boolean hasWork(){return running||outputsReady;} }
}
