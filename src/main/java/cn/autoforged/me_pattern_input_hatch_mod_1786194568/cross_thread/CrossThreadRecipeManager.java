/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.inventory.AbstractConfigurableStack
 *  aztech.modern_industrialization.inventory.ConfigurableFluidStack
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.components.CrafterComponent
 *  aztech.modern_industrialization.machines.components.CrafterComponent$Behavior
 *  aztech.modern_industrialization.machines.components.CrafterComponent$Inventory
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe$FluidInput
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe$FluidOutput
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe$ItemInput
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe$ItemOutput
 *  aztech.modern_industrialization.machines.recipe.MachineRecipeType
 *  aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition$Context
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant
 *  aztech.modern_industrialization.util.Simulation
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread;

import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.util.Simulation;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat.MIParallelHatchCompat;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public final class CrossThreadRecipeManager {
    public static final String NBT_KEY = "aeindCrossThreadRecipes";
    private static final int NBT_VERSION = 2;
    private final Map<String, RecipeThreadState> states = new LinkedHashMap<String, RecipeThreadState>();
    private int fairnessCursor;

    public boolean hasWork() {
        return this.states.values().stream().anyMatch(RecipeThreadState::hasWork);
    }

    public int getActiveThreadCount() {
        return (int)this.states.values().stream().filter(RecipeThreadState::isRunning).count();
    }

    public int getTotalActiveParallel() {
        return this.states.values().stream().filter(RecipeThreadState::isRunning).mapToInt(recipeThreadState -> recipeThreadState.parallel).sum();
    }

    public int getMaxActiveParallel() {
        return this.states.values().stream().filter(RecipeThreadState::isRunning).mapToInt(recipeThreadState -> recipeThreadState.parallel).max().orElse(0);
    }

    public double getMaxActiveEnergyFactor() {
        return this.states.values().stream().filter(RecipeThreadState::isRunning).mapToDouble(recipeThreadState -> recipeThreadState.energyFactor).max().orElse(1.0);
    }

    public ProgressSnapshot getProgressSnapshot() {
        List<ThreadProgress> list = this.states.values().stream().filter(RecipeThreadState::hasWork).sorted(Comparator.comparing(recipeThreadState -> recipeThreadState.roomId)).map(recipeThreadState -> {
            double d = recipeThreadState.outputsReady ? 1.0 : (recipeThreadState.totalEnergy > 0L ? (double)recipeThreadState.usedEnergy / (double)recipeThreadState.totalEnergy : 0.0);
            float f = (float)Math.max(0.0, Math.min(1.0, d));
            return new ThreadProgress(f, Math.max(1, recipeThreadState.parallel), recipeThreadState.outputsReady);
        }).toList();
        return new ProgressSnapshot(list);
    }

    public boolean tick(MachineBlockEntity machineBlockEntity, CrafterComponent crafterComponent, ThreadIsolationAccess threadIsolationAccess, boolean bl) {
        boolean bl2;
        int n;
        int n2;
        CrafterComponent.Behavior behavior = crafterComponent.getBehavior();
        LinkedHashMap<String, ThreadIsolationRoom> linkedHashMap = new LinkedHashMap<String, ThreadIsolationRoom>();
        for (ThreadIsolationRoom threadIsolationRoom : threadIsolationAccess.aeind$isolationRooms()) {
            linkedHashMap.putIfAbsent(threadIsolationRoom.id(), threadIsolationRoom);
        }
        boolean n3 = this.flushCompletedOutputs(crafterComponent.getInventory());
        if (bl && behavior.isEnabled()) {
            int n4 = MIParallelHatchCompat.getParallelLimit(machineBlockEntity);
            n2 = Math.max(Math.max(1, threadIsolationAccess.aeind$maxParallelPerThread()), n4);
            for (ThreadIsolationRoom threadIsolationRoom : linkedHashMap.values()) {
                RecipeThreadState recipeThreadState = this.states.computeIfAbsent(threadIsolationRoom.id(), RecipeThreadState::new);
                if (recipeThreadState.hasWork()) continue;
                n = this.tryStart(machineBlockEntity, crafterComponent, threadIsolationRoom, recipeThreadState, n2, n4 > 1) ? 1 : 0;
                n5 |= n;
                if (n != 0 || recipeThreadState.efficiencyTicks <= 0) continue;
                --recipeThreadState.efficiencyTicks;
                if (recipeThreadState.efficiencyTicks == 0) {
                    recipeThreadState.recipeId = null;
                }
                int n5 = 1;
            }
        }
        List<RecipeThreadState> list = this.states.values().stream().filter(RecipeThreadState::isRunning).toList();
        n2 = 0;
        if (!list.isEmpty() && behavior.isEnabled()) {
            Object object = new long[list.size()];
            long l = 0L;
            for (n = 0; n < list.size(); ++n) {
                RecipeThreadState recipeThreadState = list.get(n);
                RecipeHolder<MachineRecipe> recipeHolder = CrossThreadRecipeManager.getRecipe(behavior, recipeThreadState.recipeId);
                if (recipeHolder == null || !((MachineRecipe)recipeHolder.value()).conditionsMatch(CrossThreadRecipeManager.conditionContext(machineBlockEntity))) continue;
                long l2 = CrossThreadRecipeManager.getRecipeMaxEu(behavior, (MachineRecipe)recipeHolder.value(), recipeThreadState.efficiencyTicks);
                long l3 = recipeThreadState.usedEnergy >= recipeThreadState.totalEnergy ? 0L : recipeThreadState.totalEnergy - recipeThreadState.usedEnergy;
                object[n] = Math.min(MIParallelHatchCompat.scaleEnergy(l2, recipeThreadState.parallel, recipeThreadState.energyFactor), l3);
                l = CrossThreadRecipeManager.saturatedAdd(l, (long)object[n]);
            }
            long l4 = behavior.consumeEu(l, Simulation.SIMULATE);
            int n6 = list.size();
            int n7 = Math.floorMod(this.fairnessCursor++, n6);
            int n8 = n6;
            for (int i = 0; i < n6; ++i) {
                int n9 = (n7 + i) % n6;
                RecipeThreadState recipeThreadState = list.get(n9);
                Object object2 = object[n9];
                long l5 = n8 == 0 ? 0L : CrossThreadRecipeManager.divideCeil(l4, n8);
                long l6 = Math.min((long)object2, l5);
                long l7 = l6 == 0L ? 0L : behavior.consumeEu(l6, Simulation.ACT);
                l4 -= l7;
                --n8;
                recipeThreadState.usedEnergy = CrossThreadRecipeManager.saturatedAdd(recipeThreadState.usedEnergy, l7);
                n2 |= l7 > 0L ? 1 : 0;
                if (l7 < object2 && recipeThreadState.efficiencyTicks > 0) {
                    --recipeThreadState.efficiencyTicks;
                }
                if (l7 > 0L) {
                    bl2 = true;
                }
                if (recipeThreadState.usedEnergy < recipeThreadState.totalEnergy) continue;
                this.finishRecipe(machineBlockEntity, crafterComponent, recipeThreadState);
                bl2 = true;
            }
        }
        int n10 = bl2 | this.flushCompletedOutputs(crafterComponent.getInventory());
        if (!bl && !this.hasWork()) {
            this.states.clear();
        } else {
            this.states.entrySet().removeIf(entry -> !linkedHashMap.containsKey(entry.getKey()) && !((RecipeThreadState)entry.getValue()).hasWork());
        }
        if (n10 != 0) {
            machineBlockEntity.setChanged();
        }
        return n2 != 0;
    }

    private boolean tryStart(MachineBlockEntity machineBlockEntity, CrafterComponent crafterComponent, ThreadIsolationRoom threadIsolationRoom, RecipeThreadState recipeThreadState, int n, boolean bl) {
        int n2;
        Object object;
        CrafterComponent.Behavior behavior = crafterComponent.getBehavior();
        MachineProcessCondition.Context context = CrossThreadRecipeManager.conditionContext(machineBlockEntity);
        if (recipeThreadState.recipeId != null && recipeThreadState.efficiencyTicks > 0 && (object = CrossThreadRecipeManager.getRecipe(behavior, recipeThreadState.recipeId)) != null && (n2 = this.findParallel(crafterComponent, threadIsolationRoom, (MachineRecipe)object.value(), n)) > 0 && ((MachineRecipe)object.value()).conditionsMatch(context)) {
            return this.startRecipe(machineBlockEntity, crafterComponent, threadIsolationRoom, recipeThreadState, (RecipeHolder<MachineRecipe>)object, n2, bl);
        }
        object = CrafterComponent.getRecipes((ServerLevel)behavior.getCrafterWorld(), (MachineRecipeType)behavior.recipeType(), threadIsolationRoom.itemInputs());
        ArrayList<MachineRecipe> arrayList = new ArrayList<MachineRecipe>((Collection<MachineRecipe>)object);
        arrayList.sort(Comparator.comparing(recipeHolder -> recipeHolder.id().toString()));
        for (RecipeHolder recipeHolder2 : arrayList) {
            int n3;
            MachineRecipe machineRecipe = (MachineRecipe)recipeHolder2.value();
            if (behavior.banRecipe(machineRecipe) || !machineRecipe.conditionsMatch(context) || (n3 = this.findParallel(crafterComponent, threadIsolationRoom, machineRecipe, n)) <= 0) continue;
            return this.startRecipe(machineBlockEntity, crafterComponent, threadIsolationRoom, recipeThreadState, (RecipeHolder<MachineRecipe>)recipeHolder2, n3, bl);
        }
        return false;
    }

    private int findParallel(CrafterComponent crafterComponent, ThreadIsolationRoom threadIsolationRoom, MachineRecipe machineRecipe, int n) {
        int n2 = Math.min(Math.max(1, n), 1024);
        int n3 = 1;
        int n4 = n2;
        int n5 = 0;
        while (n3 <= n4) {
            boolean bl;
            int n6 = n3 + (n4 - n3) / 2;
            boolean bl2 = bl = CrossThreadRecipeManager.canTakeInputs(crafterComponent.getBehavior(), threadIsolationRoom, machineRecipe, n6) && this.canReserveOutputs(crafterComponent.getInventory(), machineRecipe, n6, crafterComponent.getBehavior().getMaxFluidOutputs());
            if (bl) {
                n5 = n6;
                n3 = n6 + 1;
                continue;
            }
            n4 = n6 - 1;
        }
        return n5;
    }

    private boolean startRecipe(MachineBlockEntity machineBlockEntity, CrafterComponent crafterComponent, ThreadIsolationRoom threadIsolationRoom, RecipeThreadState recipeThreadState, RecipeHolder<MachineRecipe> recipeHolder, int n, boolean bl) {
        MachineRecipe machineRecipe = (MachineRecipe)recipeHolder.value();
        if (!CrossThreadRecipeManager.takeInputs(crafterComponent.getBehavior(), threadIsolationRoom, machineRecipe, n)) {
            return false;
        }
        recipeThreadState.recipeId = recipeHolder.id();
        recipeThreadState.parallel = n;
        recipeThreadState.energyFactor = bl ? MIParallelHatchCompat.getEnergyFactor(machineBlockEntity, n) : (double)n;
        recipeThreadState.usedEnergy = 0L;
        recipeThreadState.totalEnergy = MIParallelHatchCompat.scaleEnergy(machineRecipe.getTotalEu(), n, recipeThreadState.energyFactor);
        recipeThreadState.maxEfficiencyTicks = CrossThreadRecipeManager.getRecipeMaxEfficiencyTicks(crafterComponent.getBehavior(), machineRecipe);
        recipeThreadState.running = true;
        recipeThreadState.outputsReady = false;
        recipeThreadState.heldItemOutputs = CrossThreadRecipeManager.maximumItemOutputs(machineRecipe, n);
        recipeThreadState.heldFluidOutputs = CrossThreadRecipeManager.maximumFluidOutputs(machineRecipe, n, crafterComponent.getBehavior().getMaxFluidOutputs());
        return true;
    }

    private void finishRecipe(MachineBlockEntity machineBlockEntity, CrafterComponent crafterComponent, RecipeThreadState recipeThreadState) {
        RecipeHolder<MachineRecipe> recipeHolder = CrossThreadRecipeManager.getRecipe(crafterComponent.getBehavior(), recipeThreadState.recipeId);
        if (recipeHolder == null) {
            recipeThreadState.usedEnergy = recipeThreadState.totalEnergy;
            return;
        }
        recipeThreadState.heldItemOutputs = CrossThreadRecipeManager.rollItemOutputs(crafterComponent.getBehavior(), (MachineRecipe)recipeHolder.value(), recipeThreadState.parallel);
        recipeThreadState.heldFluidOutputs = CrossThreadRecipeManager.rollFluidOutputs(crafterComponent.getBehavior(), (MachineRecipe)recipeHolder.value(), recipeThreadState.parallel);
        recipeThreadState.running = false;
        recipeThreadState.outputsReady = true;
        recipeThreadState.usedEnergy = 0L;
        recipeThreadState.totalEnergy = 0L;
        if (recipeThreadState.efficiencyTicks < recipeThreadState.maxEfficiencyTicks) {
            ++recipeThreadState.efficiencyTicks;
        }
        crafterComponent.getBehavior().onCraft();
        machineBlockEntity.setChanged();
    }

    private boolean flushCompletedOutputs(CrafterComponent.Inventory inventory) {
        boolean bl = false;
        for (RecipeThreadState recipeThreadState : this.states.values()) {
            if (!recipeThreadState.outputsReady) continue;
            bl |= CrossThreadRecipeManager.flushItems(inventory.getItemOutputs(), recipeThreadState.heldItemOutputs);
            bl |= CrossThreadRecipeManager.flushFluids(inventory.getFluidOutputs(), recipeThreadState.heldFluidOutputs);
            recipeThreadState.heldItemOutputs.removeIf(AbstractConfigurableStack::isEmpty);
            recipeThreadState.heldFluidOutputs.removeIf(AbstractConfigurableStack::isEmpty);
            if (!recipeThreadState.heldItemOutputs.isEmpty() || !recipeThreadState.heldFluidOutputs.isEmpty()) continue;
            recipeThreadState.outputsReady = false;
            bl = true;
        }
        return bl;
    }

    private boolean canReserveOutputs(CrafterComponent.Inventory inventory, MachineRecipe machineRecipe, int n, int n2) {
        ArrayList arrayList = ConfigurableItemStack.copyList((List)inventory.getItemOutputs());
        ArrayList arrayList2 = ConfigurableFluidStack.copyList((List)inventory.getFluidOutputs());
        for (RecipeThreadState recipeThreadState : this.states.values()) {
            if (!recipeThreadState.hasWork()) continue;
            if (!CrossThreadRecipeManager.insertAllItems(arrayList, CrossThreadRecipeManager.copyItems(recipeThreadState.heldItemOutputs))) {
                return false;
            }
            if (CrossThreadRecipeManager.insertAllFluids(arrayList2, CrossThreadRecipeManager.copyFluids(recipeThreadState.heldFluidOutputs))) continue;
            return false;
        }
        return CrossThreadRecipeManager.insertAllItems(arrayList, CrossThreadRecipeManager.maximumItemOutputs(machineRecipe, n)) && CrossThreadRecipeManager.insertAllFluids(arrayList2, CrossThreadRecipeManager.maximumFluidOutputs(machineRecipe, n, n2));
    }

    private static boolean canTakeInputs(CrafterComponent.Behavior behavior, ThreadIsolationRoom threadIsolationRoom, MachineRecipe machineRecipe, int n) {
        ArrayList arrayList = ConfigurableItemStack.copyList(threadIsolationRoom.itemInputs());
        ArrayList arrayList2 = ConfigurableFluidStack.copyList(threadIsolationRoom.fluidInputs());
        for (int i = 0; i < n; ++i) {
            if (CrossThreadRecipeManager.takeItemInputs(null, arrayList, machineRecipe, true) && CrossThreadRecipeManager.takeFluidInputs(behavior, arrayList2, machineRecipe, true)) continue;
            return false;
        }
        return true;
    }

    private static boolean takeInputs(CrafterComponent.Behavior behavior, ThreadIsolationRoom threadIsolationRoom, MachineRecipe machineRecipe, int n) {
        if (!CrossThreadRecipeManager.canTakeInputs(behavior, threadIsolationRoom, machineRecipe, n)) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            CrossThreadRecipeManager.takeItemInputs(behavior, threadIsolationRoom.itemInputs(), machineRecipe, false);
            CrossThreadRecipeManager.takeFluidInputs(behavior, threadIsolationRoom.fluidInputs(), machineRecipe, false);
        }
        return true;
    }

    private static boolean takeItemInputs(CrafterComponent.Behavior behavior, List<ConfigurableItemStack> list, MachineRecipe machineRecipe, boolean bl) {
        for (MachineRecipe.ItemInput itemInput : machineRecipe.itemInputs) {
            if (!bl && itemInput.probability() < 1.0f && ThreadLocalRandom.current().nextFloat() >= itemInput.probability()) continue;
            int n = itemInput.amount();
            for (ConfigurableItemStack configurableItemStack : list) {
                if (configurableItemStack.getAmount() <= 0L || !((ItemVariant)configurableItemStack.getResource()).test((Predicate)itemInput.ingredient())) continue;
                int n2 = (int)Math.min(configurableItemStack.getAmount(), (long)n);
                if (n2 > 0) {
                    if (!bl) {
                        behavior.getStatsOrDummy().addUsedItems((ItemLike)((ItemVariant)configurableItemStack.getResource()).getItem(), (long)n2);
                    }
                    configurableItemStack.decrement((long)n2);
                    n -= n2;
                }
                if (n != 0) continue;
                break;
            }
            if (n <= 0) continue;
            return false;
        }
        return true;
    }

    private static boolean takeFluidInputs(CrafterComponent.Behavior behavior, List<ConfigurableFluidStack> list, MachineRecipe machineRecipe, boolean bl) {
        boolean[] blArray = behavior != null && behavior.oneFluidInputPerStack() ? new boolean[list.size()] : null;
        for (MachineRecipe.FluidInput fluidInput : machineRecipe.fluidInputs) {
            if (!bl && fluidInput.probability() < 1.0f && ThreadLocalRandom.current().nextFloat() >= fluidInput.probability()) continue;
            long l = fluidInput.amount();
            for (int i = 0; i < list.size(); ++i) {
                ConfigurableFluidStack configurableFluidStack;
                if (blArray != null && blArray[i] || (configurableFluidStack = list.get(i)).getAmount() <= 0L || !fluidInput.fluid().test(configurableFluidStack.toStack())) continue;
                long l2 = Math.min(configurableFluidStack.getAmount(), l);
                if (l2 > 0L) {
                    if (!bl) {
                        behavior.getStatsOrDummy().addUsedFluids(((FluidVariant)configurableFluidStack.getResource()).getFluid(), l2);
                    }
                    configurableFluidStack.decrement(l2);
                    if (blArray != null) {
                        blArray[i] = true;
                    }
                    l -= l2;
                }
                if (l == 0L) break;
            }
            if (l <= 0L) continue;
            return false;
        }
        return true;
    }

    private static List<ConfigurableItemStack> maximumItemOutputs(MachineRecipe machineRecipe, int n) {
        ArrayList<ConfigurableItemStack> arrayList = new ArrayList<ConfigurableItemStack>();
        for (MachineRecipe.ItemOutput itemOutput : machineRecipe.itemOutputs) {
            CrossThreadRecipeManager.addItem(arrayList, itemOutput.variant(), CrossThreadRecipeManager.saturatedMultiply(itemOutput.amount(), n));
        }
        return arrayList;
    }

    private static List<ConfigurableFluidStack> maximumFluidOutputs(MachineRecipe machineRecipe, int n, int n2) {
        ArrayList<ConfigurableFluidStack> arrayList = new ArrayList<ConfigurableFluidStack>();
        for (int i = 0; i < Math.min(machineRecipe.fluidOutputs.size(), n2); ++i) {
            MachineRecipe.FluidOutput fluidOutput = (MachineRecipe.FluidOutput)machineRecipe.fluidOutputs.get(i);
            CrossThreadRecipeManager.addFluid(arrayList, fluidOutput.fluid(), CrossThreadRecipeManager.saturatedMultiply(fluidOutput.amount(), n));
        }
        return arrayList;
    }

    private static List<ConfigurableItemStack> rollItemOutputs(CrafterComponent.Behavior behavior, MachineRecipe machineRecipe, int n) {
        ArrayList<ConfigurableItemStack> arrayList = new ArrayList<ConfigurableItemStack>();
        for (int i = 0; i < n; ++i) {
            for (MachineRecipe.ItemOutput itemOutput : machineRecipe.itemOutputs) {
                if (!(itemOutput.probability() >= 1.0f) && !(ThreadLocalRandom.current().nextFloat() <= itemOutput.probability())) continue;
                CrossThreadRecipeManager.addItem(arrayList, itemOutput.variant(), itemOutput.amount());
                behavior.getStatsOrDummy().addProducedItems((Level)behavior.getCrafterWorld(), (ItemLike)itemOutput.variant().getItem(), (long)itemOutput.amount());
            }
        }
        return arrayList;
    }

    private static List<ConfigurableFluidStack> rollFluidOutputs(CrafterComponent.Behavior behavior, MachineRecipe machineRecipe, int n) {
        ArrayList<ConfigurableFluidStack> arrayList = new ArrayList<ConfigurableFluidStack>();
        int n2 = behavior.getMaxFluidOutputs();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < Math.min(machineRecipe.fluidOutputs.size(), n2); ++j) {
                MachineRecipe.FluidOutput fluidOutput = (MachineRecipe.FluidOutput)machineRecipe.fluidOutputs.get(j);
                if (!(fluidOutput.probability() >= 1.0f) && !(ThreadLocalRandom.current().nextFloat() <= fluidOutput.probability())) continue;
                CrossThreadRecipeManager.addFluid(arrayList, fluidOutput.fluid(), fluidOutput.amount());
                behavior.getStatsOrDummy().addProducedFluids(fluidOutput.fluid(), fluidOutput.amount());
            }
        }
        return arrayList;
    }

    private static void addItem(List<ConfigurableItemStack> list, ItemVariant itemVariant, long l) {
        if (l <= 0L) {
            return;
        }
        for (ConfigurableItemStack configurableItemStack : list) {
            if (!((ItemVariant)configurableItemStack.getResource()).equals((Object)itemVariant)) continue;
            configurableItemStack.increment(l);
            return;
        }
        ConfigurableItemStack configurableItemStack = new ConfigurableItemStack();
        configurableItemStack.setKey(itemVariant);
        configurableItemStack.setAmount(l);
        list.add(configurableItemStack);
    }

    private static void addFluid(List<ConfigurableFluidStack> list, Fluid fluid, long l) {
        if (l <= 0L) {
            return;
        }
        FluidVariant fluidVariant = FluidVariant.of((Fluid)fluid);
        for (ConfigurableFluidStack configurableFluidStack : list) {
            if (!((FluidVariant)configurableFluidStack.getResource()).equals((Object)fluidVariant)) continue;
            long l2 = CrossThreadRecipeManager.saturatedAdd(configurableFluidStack.getAmount(), l);
            configurableFluidStack.setCapacity(l2);
            configurableFluidStack.setAmount(l2);
            return;
        }
        ConfigurableFluidStack configurableFluidStack = new ConfigurableFluidStack(l);
        configurableFluidStack.setKey((TransferVariant)fluidVariant);
        configurableFluidStack.setAmount(l);
        list.add(configurableFluidStack);
    }

    private static boolean insertAllItems(List<ConfigurableItemStack> list, List<ConfigurableItemStack> list2) {
        for (ConfigurableItemStack configurableItemStack : list2) {
            long l = configurableItemStack.getAmount();
            block1: for (int i = 0; i < 2 && l > 0L; ++i) {
                for (ConfigurableItemStack configurableItemStack2 : list) {
                    boolean bl = ((ItemVariant)configurableItemStack2.getResource()).equals((Object)configurableItemStack.getResource());
                    boolean bl2 = configurableItemStack2.isEmpty();
                    if (i == 0 && !bl || i == 1 && !bl2 || !configurableItemStack2.isResourceAllowedByLock((TransferVariant)((ItemVariant)configurableItemStack.getResource()))) continue;
                    long l2 = configurableItemStack2.getCapacity();
                    long l3 = Math.min(l, Math.max(0L, l2 - configurableItemStack2.getAmount()));
                    if (l3 > 0L) {
                        if (bl2) {
                            configurableItemStack2.setKey((ItemVariant)configurableItemStack.getResource());
                        }
                        configurableItemStack2.increment(l3);
                        l -= l3;
                    }
                    if (l != 0L) continue;
                    continue block1;
                }
            }
            configurableItemStack.setAmount(l);
            if (l <= 0L) continue;
            return false;
        }
        return true;
    }

    private static boolean insertAllFluids(List<ConfigurableFluidStack> list, List<ConfigurableFluidStack> list2) {
        for (ConfigurableFluidStack configurableFluidStack : list2) {
            long l = configurableFluidStack.getAmount();
            block1: for (int i = 0; i < 2 && l > 0L; ++i) {
                for (ConfigurableFluidStack configurableFluidStack2 : list) {
                    boolean bl = ((FluidVariant)configurableFluidStack2.getResource()).equals((Object)configurableFluidStack.getResource());
                    boolean bl2 = configurableFluidStack2.isEmpty();
                    if (i == 0 && !bl || i == 1 && !bl2 || !configurableFluidStack2.isResourceAllowedByLock((TransferVariant)((FluidVariant)configurableFluidStack.getResource()))) continue;
                    long l2 = Math.min(l, configurableFluidStack2.getRemainingSpace());
                    if (l2 > 0L) {
                        if (bl2) {
                            configurableFluidStack2.setKey((TransferVariant)((FluidVariant)configurableFluidStack.getResource()));
                        }
                        configurableFluidStack2.increment(l2);
                        l -= l2;
                    }
                    if (l != 0L) continue;
                    continue block1;
                }
            }
            configurableFluidStack.setAmount(l);
            if (l <= 0L) continue;
            return false;
        }
        return true;
    }

    private static boolean flushItems(List<ConfigurableItemStack> list, List<ConfigurableItemStack> list2) {
        long l = list2.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
        CrossThreadRecipeManager.insertAllItems(list, list2);
        long l2 = list2.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
        return l != l2;
    }

    private static boolean flushFluids(List<ConfigurableFluidStack> list, List<ConfigurableFluidStack> list2) {
        long l = list2.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
        CrossThreadRecipeManager.insertAllFluids(list, list2);
        long l2 = list2.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
        return l != l2;
    }

    private static List<ConfigurableItemStack> copyItems(List<ConfigurableItemStack> list) {
        return ConfigurableItemStack.copyList(list);
    }

    private static List<ConfigurableFluidStack> copyFluids(List<ConfigurableFluidStack> list) {
        return ConfigurableFluidStack.copyList(list);
    }

    private static RecipeHolder<MachineRecipe> getRecipe(CrafterComponent.Behavior behavior, ResourceLocation resourceLocation) {
        return resourceLocation == null ? null : behavior.recipeType().getRecipe(behavior.getCrafterWorld(), resourceLocation);
    }

    private static MachineProcessCondition.Context conditionContext(MachineBlockEntity machineBlockEntity) {
        return () -> machineBlockEntity;
    }

    private static long getRecipeMaxEu(CrafterComponent.Behavior behavior, MachineRecipe machineRecipe, int n) {
        long l = machineRecipe.getTotalEu();
        long l2 = Math.max(behavior.getBaseRecipeEu(), (long)machineRecipe.eu);
        long l3 = l2 + (long)n * l / 600L;
        return Math.min(l, Math.min(l3, behavior.getMaxRecipeEu()));
    }

    private static int getRecipeMaxEfficiencyTicks(CrafterComponent.Behavior behavior, MachineRecipe machineRecipe) {
        long l = Math.min(behavior.getMaxRecipeEu(), machineRecipe.getTotalEu());
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            if (CrossThreadRecipeManager.getRecipeMaxEu(behavior, machineRecipe, i) != l) continue;
            return i;
        }
        return 0;
    }

    private static long saturatedMultiply(long l, long l2) {
        if (l == 0L || l2 == 0L) {
            return 0L;
        }
        if (l > Long.MAX_VALUE / l2) {
            return Long.MAX_VALUE;
        }
        return l * l2;
    }

    private static long saturatedAdd(long l, long l2) {
        if (Long.MAX_VALUE - l < l2) {
            return Long.MAX_VALUE;
        }
        return l + l2;
    }

    private static long divideCeil(long l, int n) {
        if (l == 0L) {
            return 0L;
        }
        return 1L + (l - 1L) / (long)n;
    }

    public void writeNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.putInt("version", 2);
        compoundTag2.putInt("fairnessCursor", this.fairnessCursor);
        ListTag listTag = new ListTag();
        for (RecipeThreadState recipeThreadState : this.states.values()) {
            if (recipeThreadState.recipeId == null && !recipeThreadState.hasWork() && recipeThreadState.efficiencyTicks == 0) continue;
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.putString("room", recipeThreadState.roomId);
            if (recipeThreadState.recipeId != null) {
                compoundTag3.putString("recipe", recipeThreadState.recipeId.toString());
            }
            compoundTag3.putInt("parallel", recipeThreadState.parallel);
            compoundTag3.putDouble("energyFactor", recipeThreadState.energyFactor);
            compoundTag3.putLong("usedEnergy", recipeThreadState.usedEnergy);
            compoundTag3.putLong("totalEnergy", recipeThreadState.totalEnergy);
            compoundTag3.putInt("efficiencyTicks", recipeThreadState.efficiencyTicks);
            compoundTag3.putInt("maxEfficiencyTicks", recipeThreadState.maxEfficiencyTicks);
            compoundTag3.putBoolean("running", recipeThreadState.running);
            compoundTag3.putBoolean("outputsReady", recipeThreadState.outputsReady);
            compoundTag3.put("itemOutputs", (Tag)CrossThreadRecipeManager.writeItems(recipeThreadState.heldItemOutputs, provider));
            compoundTag3.put("fluidOutputs", (Tag)CrossThreadRecipeManager.writeFluids(recipeThreadState.heldFluidOutputs, provider));
            listTag.add((Object)compoundTag3);
        }
        compoundTag2.put("threads", (Tag)listTag);
        compoundTag.put(NBT_KEY, (Tag)compoundTag2);
    }

    public void readNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.states.clear();
        if (!compoundTag.contains(NBT_KEY, 10)) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.getCompound(NBT_KEY);
        this.fairnessCursor = compoundTag2.getInt("fairnessCursor");
        ListTag listTag = compoundTag2.getList("threads", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag3 = listTag.getCompound(i);
            String string = compoundTag3.getString("room");
            if (string.isEmpty()) continue;
            RecipeThreadState recipeThreadState = new RecipeThreadState(string);
            recipeThreadState.recipeId = compoundTag3.contains("recipe") ? ResourceLocation.tryParse((String)compoundTag3.getString("recipe")) : null;
            recipeThreadState.parallel = Math.max(1, compoundTag3.getInt("parallel"));
            double d = compoundTag3.contains("energyFactor", 6) ? compoundTag3.getDouble("energyFactor") : (double)recipeThreadState.parallel;
            recipeThreadState.energyFactor = Double.isFinite(d) && d >= 1.0 ? d : (double)recipeThreadState.parallel;
            recipeThreadState.usedEnergy = Math.max(0L, compoundTag3.getLong("usedEnergy"));
            recipeThreadState.totalEnergy = Math.max(0L, compoundTag3.getLong("totalEnergy"));
            recipeThreadState.efficiencyTicks = Math.max(0, compoundTag3.getInt("efficiencyTicks"));
            recipeThreadState.maxEfficiencyTicks = Math.max(0, compoundTag3.getInt("maxEfficiencyTicks"));
            recipeThreadState.running = compoundTag3.getBoolean("running");
            recipeThreadState.outputsReady = compoundTag3.getBoolean("outputsReady");
            recipeThreadState.heldItemOutputs = CrossThreadRecipeManager.readItems(compoundTag3.getList("itemOutputs", 10), provider);
            recipeThreadState.heldFluidOutputs = CrossThreadRecipeManager.readFluids(compoundTag3.getList("fluidOutputs", 10), provider);
            this.states.put(string, recipeThreadState);
        }
    }

    private static ListTag writeItems(List<ConfigurableItemStack> list, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (ConfigurableItemStack configurableItemStack : list) {
            listTag.add((Object)configurableItemStack.toNbt(provider));
        }
        return listTag;
    }

    private static ListTag writeFluids(List<ConfigurableFluidStack> list, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (ConfigurableFluidStack configurableFluidStack : list) {
            listTag.add((Object)configurableFluidStack.toNbt(provider));
        }
        return listTag;
    }

    private static List<ConfigurableItemStack> readItems(ListTag listTag, HolderLookup.Provider provider) {
        ArrayList<ConfigurableItemStack> arrayList = new ArrayList<ConfigurableItemStack>();
        for (int i = 0; i < listTag.size(); ++i) {
            arrayList.add(new ConfigurableItemStack(listTag.getCompound(i), provider));
        }
        return arrayList;
    }

    private static List<ConfigurableFluidStack> readFluids(ListTag listTag, HolderLookup.Provider provider) {
        ArrayList<ConfigurableFluidStack> arrayList = new ArrayList<ConfigurableFluidStack>();
        for (int i = 0; i < listTag.size(); ++i) {
            arrayList.add(new ConfigurableFluidStack(listTag.getCompound(i), provider));
        }
        return arrayList;
    }

    public record ProgressSnapshot(List<ThreadProgress> threads) {
    }

    private static final class RecipeThreadState {
        private final String roomId;
        private ResourceLocation recipeId;
        private int parallel = 1;
        private double energyFactor = 1.0;
        private long usedEnergy;
        private long totalEnergy;
        private int efficiencyTicks;
        private int maxEfficiencyTicks;
        private boolean running;
        private boolean outputsReady;
        private List<ConfigurableItemStack> heldItemOutputs = new ArrayList<ConfigurableItemStack>();
        private List<ConfigurableFluidStack> heldFluidOutputs = new ArrayList<ConfigurableFluidStack>();

        private RecipeThreadState(String string) {
            this.roomId = string;
        }

        private boolean isRunning() {
            return this.running;
        }

        private boolean hasWork() {
            return this.running || this.outputsReady;
        }
    }

    public record ThreadProgress(float progress, int parallel, boolean outputsReady) {
    }
}
