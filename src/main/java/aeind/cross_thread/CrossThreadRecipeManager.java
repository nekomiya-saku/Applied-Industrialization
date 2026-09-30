package aeind.cross_thread;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import aztech.modern_industrialization.inventory.AbstractConfigurableStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.CrafterComponent.Behavior;
import aztech.modern_industrialization.machines.components.CrafterComponent.Inventory;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.FluidInput;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.FluidOutput;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.ItemInput;
import aztech.modern_industrialization.machines.recipe.MachineRecipe.ItemOutput;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition.Context;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import aeind.compat.MIParallelHatchCompat;
import aeind.isolation.RoomInputStorage;
import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationRoom;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;

public final class CrossThreadRecipeManager {
   public static final String NBT_KEY = "aeindCrossThreadRecipes";
   private static final int NBT_VERSION = 2;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, CrossThreadRecipeManager.RecipeThreadState> states = new LinkedHashMap<>();
   private final Map<String, Integer> reportedParallelByRoom = new LinkedHashMap<>();
   private int fairnessCursor;

   public boolean hasWork() {
      return this.states.values().stream().anyMatch(CrossThreadRecipeManager.RecipeThreadState::hasWork);
   }

   public int getActiveThreadCount() {
      return (int)this.states.values().stream().filter(CrossThreadRecipeManager.RecipeThreadState::isRunning).count();
   }

   public int getTotalActiveParallel() {
      long var1 = 0L;

      for (CrossThreadRecipeManager.RecipeThreadState var4 : this.states.values()) {
         if (var4.isRunning()) {
            var1 = saturatedAdd(var1, var4.parallel);
         }
      }

      return (int)Math.min(var1, Integer.MAX_VALUE);
   }

   public int getMaxActiveParallel() {
      return this.states.values().stream().filter(CrossThreadRecipeManager.RecipeThreadState::isRunning).mapToInt(var0 -> var0.parallel).max().orElse(0);
   }

   public double getMaxActiveEnergyFactor() {
      return this.states
         .values()
         .stream()
         .filter(CrossThreadRecipeManager.RecipeThreadState::isRunning)
         .mapToDouble(var0 -> var0.energyFactor)
         .max()
         .orElse(1.0);
   }

   public CrossThreadRecipeManager.ProgressSnapshot getProgressSnapshot() {
      List<CrossThreadRecipeManager.ThreadProgress> var1 = this.states
         .values()
         .stream()
         .filter(CrossThreadRecipeManager.RecipeThreadState::hasWork)
         .sorted(Comparator.comparing(var0 -> var0.roomId))
         .map(var0 -> {
            double var1x;
            if (var0.outputsReady) {
               var1x = 1.0;
            } else if (var0.totalEnergy > 0L) {
               var1x = (double)var0.usedEnergy / var0.totalEnergy;
            } else {
               var1x = 0.0;
            }

            float var3 = (float)Math.max(0.0, Math.min(1.0, var1x));
            return new CrossThreadRecipeManager.ThreadProgress(var3, Math.max(1, var0.parallel), var0.outputsReady);
         })
         .toList();
      return new CrossThreadRecipeManager.ProgressSnapshot(var1);
   }

   public boolean tick(MachineBlockEntity var1, CrafterComponent var2, ThreadIsolationAccess var3, boolean var4) {
      Behavior var5 = var2.getBehavior();
      LinkedHashMap<String, ThreadIsolationRoom> var6 = new LinkedHashMap<>();

      for (ThreadIsolationRoom var8 : var3.aeind$isolationRooms()) {
         var6.putIfAbsent(var8.id(), var8);
      }

      boolean var29 = this.flushCompletedOutputs(var2.getInventory());
      if (var5.isEnabled()) {
         int var31 = var4 ? MIParallelHatchCompat.getParallelLimit(var1) : 1;
         int var9 = var4 ? Math.max(Math.max(1, var3.aeind$maxParallelPerThread()), var31) : 1;

         for (ThreadIsolationRoom var11 : var6.values()) {
            if (!var4 && this.hasWork()) {
               break;
            }
            CrossThreadRecipeManager.RecipeThreadState var12 = this.states.computeIfAbsent(var11.id(), CrossThreadRecipeManager.RecipeThreadState::new);
            if (!var12.hasWork()) {
               boolean var13 = this.tryStart(var1, var2, var11, var12, var9, var4 && var31 > 1);
               var29 |= var13;
               if (!var13 && var12.efficiencyTicks > 0) {
                  var12.efficiencyTicks--;
                  if (var12.efficiencyTicks == 0) {
                     var12.recipeId = null;
                  }

                  var29 = true;
               }
            }
         }
      }

      List<CrossThreadRecipeManager.RecipeThreadState> var32 = this.states.values().stream().filter(CrossThreadRecipeManager.RecipeThreadState::isRunning).toList();
      boolean var33 = false;
      if (!var32.isEmpty() && var5.isEnabled()) {
         long[] var34 = new long[var32.size()];
         long var35 = 0L;

         for (int var36 = 0; var36 < var32.size(); var36++) {
            CrossThreadRecipeManager.RecipeThreadState var14 = var32.get(var36);
            RecipeHolder<MachineRecipe> var15 = getRecipe(var5, var14.recipeId);
            if (var15 != null && var15.value().conditionsMatch(conditionContext(var1))) {
               long var16 = getRecipeMaxEu(var5, var15.value(), var14.efficiencyTicks);
               long var18 = var14.usedEnergy >= var14.totalEnergy ? 0L : var14.totalEnergy - var14.usedEnergy;
               var34[var36] = Math.min(MIParallelHatchCompat.scaleEnergy(var16, var14.parallel, var14.energyFactor), var18);
               var35 = saturatedAdd(var35, var34[var36]);
            }
         }

         long var37 = var5.consumeEu(var35, Simulation.SIMULATE);
         int var38 = var32.size();
         int var39 = Math.floorMod(this.fairnessCursor++, var38);
         int var17 = var38;

         for (int var40 = 0; var40 < var38; var40++) {
            int var19 = (var39 + var40) % var38;
            CrossThreadRecipeManager.RecipeThreadState var20 = var32.get(var19);
            long var21 = var34[var19];
            long var23 = var17 == 0 ? 0L : divideCeil(var37, var17);
            long var25 = Math.min(var21, var23);
            long var27 = var25 == 0L ? 0L : var5.consumeEu(var25, Simulation.ACT);
            var37 -= var27;
            var17--;
            var20.usedEnergy = saturatedAdd(var20.usedEnergy, var27);
            var33 |= var27 > 0L;
            if (var27 < var21 && var20.efficiencyTicks > 0) {
               var20.efficiencyTicks--;
            }

            if (var27 > 0L) {
               var29 = true;
            }

            if (var20.usedEnergy >= var20.totalEnergy) {
               this.finishRecipe(var1, var2, var20);
               var29 = true;
            }
         }
      }

      var29 |= this.flushCompletedOutputs(var2.getInventory());
      if (!var4 && !this.hasWork()) {
         this.states.clear();
      } else {
         this.states.entrySet().removeIf(var1x -> !var6.containsKey(var1x.getKey()) && !var1x.getValue().hasWork());
      }

      if (var29) {
         var1.setChanged();
      }

      return var33;
   }

   private boolean tryStart(
      MachineBlockEntity var1, CrafterComponent var2, ThreadIsolationRoom var3, CrossThreadRecipeManager.RecipeThreadState var4, int var5, boolean var6
   ) {
      Behavior var7 = var2.getBehavior();
      Context var8 = conditionContext(var1);
      if (var4.recipeId != null && var4.efficiencyTicks > 0) {
          RecipeHolder<MachineRecipe> var9 = getRecipe(var7, var4.recipeId);
         if (var9 != null) {
             int var10 = this.findParallel(var2, var3, var9.value(), var5);
             if (var10 > 0 && var9.value().conditionsMatch(var8)) {
               return this.startRecipe(var1, var2, var3, var4, var9, var10, var6);
            }
         }
      }

      List<ConfigurableItemStack> recipeItems = var3.hasMapStorage()
         ? var3.inputStorage().createMiView().itemInputs()
         : var3.itemInputs();
      Collection<RecipeHolder<MachineRecipe>> var15 = CrafterComponent.getRecipes(var7.getCrafterWorld(), var7.recipeType(), recipeItems);
      ArrayList<RecipeHolder<MachineRecipe>> var16 = new ArrayList<>(var15);
      var16.sort(Comparator.comparing(var0 -> var0.id().toString()));

      for (RecipeHolder<MachineRecipe> var12 : var16) {
         MachineRecipe var13 = var12.value();
         if (!var7.banRecipe(var13) && var13.conditionsMatch(var8)) {
            int var14 = this.findParallel(var2, var3, var13, var5);
            if (var14 > 0) {
               return this.startRecipe(var1, var2, var3, var4, var12, var14, var6);
            }
         }
      }

      return false;
   }

   private int findParallel(CrafterComponent var1, ThreadIsolationRoom var2, MachineRecipe var3, int var4) {
      int var5 = Math.max(1, var4);
      long var6 = 1L;
      long var7 = var5;
      int var8 = 0;

      while (var6 <= var7) {
         int var9 = (int)(var6 + (var7 - var6) / 2L);
         boolean var10 = canTakeInputs(var1.getBehavior(), var2, var3, var9)
            && this.canReserveOutputs(var1.getInventory(), var3, var9, var1.getBehavior().getMaxFluidOutputs());
         if (var10) {
            var8 = var9;
            var6 = (long)var9 + 1L;
         } else {
            var7 = (long)var9 - 1L;
         }
      }

      return var8;
   }

   private boolean startRecipe(
      MachineBlockEntity var1,
      CrafterComponent var2,
      ThreadIsolationRoom var3,
      CrossThreadRecipeManager.RecipeThreadState var4,
      RecipeHolder<MachineRecipe> var5,
      int var6,
      boolean var7
   ) {
      MachineRecipe var8 = (MachineRecipe)var5.value();
      if (!takeInputs(var2.getBehavior(), var3, var8, var6)) {
         return false;
      }

      var4.recipeId = var5.id();
      var4.parallel = var6;
      Integer var9 = this.reportedParallelByRoom.put(var3.id(), var6);
      if (var9 == null || var9 != var6) {
         LOGGER.info("Cross-thread room {} selected {} parallel for recipe {}", var3.id(), var6, var5.id());
      }
      var4.energyFactor = var7 ? MIParallelHatchCompat.getEnergyFactor(var1, var6) : var6;
      var4.usedEnergy = 0L;
      var4.totalEnergy = MIParallelHatchCompat.scaleEnergy(var8.getTotalEu(), var6, var4.energyFactor);
      var4.maxEfficiencyTicks = getRecipeMaxEfficiencyTicks(var2.getBehavior(), var8);
      var4.running = true;
      var4.outputsReady = false;
      var4.heldItemOutputs = maximumItemOutputs(var8, var6);
      var4.heldFluidOutputs = maximumFluidOutputs(var8, var6, var2.getBehavior().getMaxFluidOutputs());
      return true;
   }

   private void finishRecipe(MachineBlockEntity var1, CrafterComponent var2, CrossThreadRecipeManager.RecipeThreadState var3) {
      RecipeHolder var4 = getRecipe(var2.getBehavior(), var3.recipeId);
      if (var4 == null) {
         var3.usedEnergy = var3.totalEnergy;
      } else {
         var3.heldItemOutputs = rollItemOutputs(var2.getBehavior(), (MachineRecipe)var4.value(), var3.parallel);
         var3.heldFluidOutputs = rollFluidOutputs(var2.getBehavior(), (MachineRecipe)var4.value(), var3.parallel);
         var3.running = false;
         var3.outputsReady = true;
         var3.usedEnergy = 0L;
         var3.totalEnergy = 0L;
         if (var3.efficiencyTicks < var3.maxEfficiencyTicks) {
            var3.efficiencyTicks++;
         }

         var2.getBehavior().onCraft();
         var1.setChanged();
      }
   }

   private boolean flushCompletedOutputs(Inventory var1) {
      boolean var2 = false;

      for (CrossThreadRecipeManager.RecipeThreadState var4 : this.states.values()) {
         if (var4.outputsReady) {
            var2 |= flushItems(var1.getItemOutputs(), var4.heldItemOutputs);
            var2 |= flushFluids(var1.getFluidOutputs(), var4.heldFluidOutputs);
            var4.heldItemOutputs.removeIf(AbstractConfigurableStack::isEmpty);
            var4.heldFluidOutputs.removeIf(AbstractConfigurableStack::isEmpty);
            if (var4.heldItemOutputs.isEmpty() && var4.heldFluidOutputs.isEmpty()) {
               var4.outputsReady = false;
               var2 = true;
            }
         }
      }

      return var2;
   }

   private boolean canReserveOutputs(Inventory var1, MachineRecipe var2, int var3, int var4) {
      ArrayList<ConfigurableItemStack> var5 = ConfigurableItemStack.copyList(var1.getItemOutputs());
      ArrayList<ConfigurableFluidStack> var6 = ConfigurableFluidStack.copyList(var1.getFluidOutputs());

      for (CrossThreadRecipeManager.RecipeThreadState var8 : this.states.values()) {
         if (var8.hasWork()) {
            if (!insertAllItems(var5, copyItems(var8.heldItemOutputs))) {
               return false;
            }

            if (!insertAllFluids(var6, copyFluids(var8.heldFluidOutputs))) {
               return false;
            }
         }
      }

      return insertAllItems(var5, maximumItemOutputs(var2, var3)) && insertAllFluids(var6, maximumFluidOutputs(var2, var3, var4));
   }

   private static boolean canTakeInputs(Behavior var0, ThreadIsolationRoom var1, MachineRecipe var2, int var3) {
      List<ConfigurableItemStack> sourceItems;
      List<ConfigurableFluidStack> sourceFluids;
      if (var1.hasMapStorage()) {
         RoomInputStorage.MiInputView view = var1.inputStorage().createMiView();
         sourceItems = view.itemInputs();
         sourceFluids = view.fluidInputs();
      } else {
         sourceItems = var1.itemInputs();
         sourceFluids = var1.fluidInputs();
      }

      ArrayList<ConfigurableItemStack> var4 = ConfigurableItemStack.copyList(sourceItems);
      ArrayList<ConfigurableFluidStack> var5 = ConfigurableFluidStack.copyList(sourceFluids);
      return takeItemInputs(var0, var4, var2, var3, true, false)
         && takeFluidInputs(var0, var5, var2, var3, true, false)
         && canTakeCatalysts(var1, var2);
   }

   private static boolean takeInputs(Behavior var0, ThreadIsolationRoom var1, MachineRecipe var2, int var3) {
      if (!canTakeInputs(var0, var1, var2, var3)) {
         return false;
      }

      if (var1.hasMapStorage()) {
         RoomInputStorage storage = var1.inputStorage();
         RoomInputStorage.MiInputView view = storage.createMiView();
         if (!takeItemInputs(var0, view.itemInputs(), var2, var3, false, false)
            || !takeFluidInputs(var0, view.fluidInputs(), var2, var3, false, false)) {
            return false;
         }

         Map<AEKey, Long> consumed = view.consumedAmounts();
         if (!storage.extractAll(consumed, Actionable.SIMULATE)
            || !storage.extractAll(consumed, Actionable.MODULATE)) {
            return false;
         }
         recordConsumedInputs(var0, consumed);
         return true;
      }

      return takeItemInputs(var0, var1.itemInputs(), var2, var3, false, true)
         && takeFluidInputs(var0, var1.fluidInputs(), var2, var3, false, true);
   }

   private static boolean canTakeCatalysts(ThreadIsolationRoom room, MachineRecipe recipe) {
      if (!room.hasCatalystStorage()) {
         for (ItemInput input : recipe.itemInputs) {
            if (input.probability() == 0.0F) return false;
         }
         for (FluidInput input : recipe.fluidInputs) {
            if (input.probability() == 0.0F) return false;
         }
         return true;
      }

      KeyCounter available = room.catalystStorage().getAvailableStacks();
      java.util.Map<AEKey, Long> remaining = new java.util.HashMap<>();
      for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<AEKey> entry : available) {
         remaining.put(entry.getKey(), entry.getLongValue());
      }
      for (ItemInput input : recipe.itemInputs) {
         if (input.probability() != 0.0F) continue;
         long required = input.amount();
         for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<AEKey> entry : available) {
            if (entry.getKey() instanceof AEItemKey item && input.ingredient().test(item.toStack())) {
               long amount = Math.min(required, remaining.getOrDefault(entry.getKey(), 0L));
               required -= amount;
               remaining.put(entry.getKey(), remaining.getOrDefault(entry.getKey(), 0L) - amount);
               if (required == 0L) break;
            }
         }
         if (required > 0L) return false;
      }
      for (FluidInput input : recipe.fluidInputs) {
         if (input.probability() != 0.0F) continue;
         long required = input.amount();
         for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<AEKey> entry : available) {
            if (entry.getKey() instanceof AEFluidKey fluid && input.fluid().test(fluid.toStack(1))) {
               long amount = Math.min(required, remaining.getOrDefault(entry.getKey(), 0L));
               required -= amount;
               remaining.put(entry.getKey(), remaining.getOrDefault(entry.getKey(), 0L) - amount);
               if (required == 0L) break;
            }
         }
         if (required > 0L) return false;
      }
      return true;
   }

   private static boolean takeItemInputs(
      Behavior var0, List<ConfigurableItemStack> var1, MachineRecipe var2, int var3, boolean var4, boolean var5
   ) {
      for (ItemInput var7 : var2.itemInputs) {
         if (var7.probability() == 0.0F) continue;
         long var8 = var4 ? var3 : sampleOccurrences(var3, var7.probability());
         long var10 = saturatedMultiply(var7.amount(), var8);

         for (ConfigurableItemStack var13 : var1) {
            if (var13.getAmount() > 0L && var13.getResource().test(var7.ingredient())) {
               long var14 = Math.min(var13.getAmount(), var10);
               if (var14 > 0L) {
                  if (!var4 && var5) {
                     var0.getStatsOrDummy().addUsedItems(var13.getResource().getItem(), var14);
                  }

                  var13.decrement(var14);
                  var10 -= var14;
               }

               if (var10 == 0L) {
                  break;
               }
            }
         }

         if (var10 > 0L) {
            return false;
         }
      }

      return true;
   }

   private static boolean takeFluidInputs(
      Behavior var0, List<ConfigurableFluidStack> var1, MachineRecipe var2, int var3, boolean var4, boolean var5
   ) {
      boolean[] var6 = var0 != null && var0.oneFluidInputPerStack() ? new boolean[var1.size()] : null;

      for (FluidInput var8 : var2.fluidInputs) {
         if (var8.probability() == 0.0F) continue;
         long var9 = var4 ? var3 : sampleOccurrences(var3, var8.probability());
         long var11 = saturatedMultiply(var8.amount(), var9);

         for (int var13 = 0; var13 < var1.size(); var13++) {
            if (var6 == null || !var6[var13]) {
               ConfigurableFluidStack var14 = var1.get(var13);
               if (var14.getAmount() > 0L && var8.fluid().test(var14.toStack())) {
                  long var15 = Math.min(var14.getAmount(), var11);
                  if (var15 > 0L) {
                     if (!var4 && var5) {
                        var0.getStatsOrDummy().addUsedFluids(var14.getResource().getFluid(), var15);
                     }

                     var14.decrement(var15);
                     if (var6 != null) {
                        var6[var13] = true;
                     }

                     var11 -= var15;
                  }

                  if (var11 == 0L) {
                     break;
                  }
               }
            }
         }

         if (var11 > 0L) {
            return false;
         }
      }

      return true;
   }

   private static long sampleOccurrences(int var0, float var1) {
      if (var0 <= 0 || var1 <= 0.0F) {
         return 0L;
      }
      if (var1 >= 1.0F) {
         return var0;
      }
      if (var0 <= 4096) {
         long var2 = 0L;

         for (int var4 = 0; var4 < var0; var4++) {
            if (ThreadLocalRandom.current().nextFloat() <= var1) {
               var2++;
            }
         }

         return var2;
      }

      double var6 = (double)var0 * var1;
      double var8 = Math.sqrt(var6 * (1.0 - var1));
      long var10 = Math.round(var6 + ThreadLocalRandom.current().nextGaussian() * var8);
      return Math.max(0L, Math.min(var0, var10));
   }

   private static void recordConsumedInputs(Behavior behavior, Map<AEKey, Long> consumed) {
      for (Map.Entry<AEKey, Long> entry : consumed.entrySet()) {
         long amount = entry.getValue();
         if (entry.getKey() instanceof AEItemKey itemKey) {
            while (amount > 0L) {
               int chunk = (int)Math.min(amount, Integer.MAX_VALUE);
               behavior.getStatsOrDummy().addUsedItems(itemKey.toStack().getItem(), chunk);
               amount -= chunk;
            }
         } else if (entry.getKey() instanceof AEFluidKey fluidKey) {
            behavior.getStatsOrDummy().addUsedFluids(fluidKey.getFluid(), amount);
         }
      }
   }

   private static List<ConfigurableItemStack> maximumItemOutputs(MachineRecipe var0, int var1) {
      ArrayList<ConfigurableItemStack> var2 = new ArrayList<>();

      for (ItemOutput var4 : var0.itemOutputs) {
         addItem(var2, var4.variant(), saturatedMultiply(var4.amount(), var1));
      }

      return var2;
   }

   private static List<ConfigurableFluidStack> maximumFluidOutputs(MachineRecipe var0, int var1, int var2) {
      ArrayList<ConfigurableFluidStack> var3 = new ArrayList<>();

      for (int var4 = 0; var4 < Math.min(var0.fluidOutputs.size(), var2); var4++) {
         FluidOutput var5 = var0.fluidOutputs.get(var4);
         addFluid(var3, var5.fluid(), saturatedMultiply(var5.amount(), var1));
      }

      return var3;
   }

   private static List<ConfigurableItemStack> rollItemOutputs(Behavior var0, MachineRecipe var1, int var2) {
      ArrayList<ConfigurableItemStack> var3 = new ArrayList<>();

      for (ItemOutput var5 : var1.itemOutputs) {
         long var6 = saturatedMultiply(var5.amount(), sampleOccurrences(var2, var5.probability()));
         if (var6 > 0L) {
            addItem(var3, var5.variant(), var6);
            var0.getStatsOrDummy().addProducedItems(var0.getCrafterWorld(), var5.variant().getItem(), var6);
         }
      }

      return var3;
   }

   private static List<ConfigurableFluidStack> rollFluidOutputs(Behavior var0, MachineRecipe var1, int var2) {
      ArrayList<ConfigurableFluidStack> var3 = new ArrayList<>();
      int var4 = var0.getMaxFluidOutputs();

      for (int var5 = 0; var5 < Math.min(var1.fluidOutputs.size(), var4); var5++) {
         FluidOutput var6 = var1.fluidOutputs.get(var5);
         long var7 = saturatedMultiply(var6.amount(), sampleOccurrences(var2, var6.probability()));
         if (var7 > 0L) {
            addFluid(var3, var6.fluid(), var7);
            var0.getStatsOrDummy().addProducedFluids(var6.fluid(), var7);
         }
      }

      return var3;
   }

   private static void addItem(List<ConfigurableItemStack> var0, ItemVariant var1, long var2) {
      if (var2 > 0L) {
         for (ConfigurableItemStack var5 : var0) {
            if (var5.getResource().equals(var1)) {
               var5.setAmount(saturatedAdd(var5.getAmount(), var2));
               return;
            }
         }

         ConfigurableItemStack var6 = new ConfigurableItemStack();
         var6.setKey(var1);
         var6.setAmount(var2);
         var0.add(var6);
      }
   }

   private static void addFluid(List<ConfigurableFluidStack> var0, Fluid var1, long var2) {
      if (var2 > 0L) {
         FluidVariant var4 = FluidVariant.of(var1);

         for (ConfigurableFluidStack var6 : var0) {
            if (var6.getResource().equals(var4)) {
               long var7 = saturatedAdd(var6.getAmount(), var2);
               var6.setCapacity(var7);
               var6.setAmount(var7);
               return;
            }
         }

         ConfigurableFluidStack var9 = new ConfigurableFluidStack(var2);
         var9.setKey(var4);
         var9.setAmount(var2);
         var0.add(var9);
      }
   }

   private static boolean insertAllItems(List<ConfigurableItemStack> var0, List<ConfigurableItemStack> var1) {
      for (ConfigurableItemStack var3 : var1) {
         long var4 = var3.getAmount();

         for (int var6 = 0; var6 < 2 && var4 > 0L; var6++) {
            for (ConfigurableItemStack var8 : var0) {
               boolean var9 = var8.getResource().equals(var3.getResource());
               boolean var10 = var8.isEmpty();
               if ((var6 != 0 || var9) && (var6 != 1 || var10) && var8.isResourceAllowedByLock(var3.getResource())) {
                  long var13 = Math.min(var4, Math.max(0L, var8.getRemainingCapacityFor(var3.getResource())));
                  if (var13 > 0L) {
                     if (var10) {
                        var8.setKey(var3.getResource());
                     }

                     var8.increment(var13);
                     var4 -= var13;
                  }

                  if (var4 == 0L) {
                     break;
                  }
               }
            }
         }

         var3.setAmount(var4);
         if (var4 > 0L) {
            return false;
         }
      }

      return true;
   }

   private static boolean insertAllFluids(List<ConfigurableFluidStack> var0, List<ConfigurableFluidStack> var1) {
      for (ConfigurableFluidStack var3 : var1) {
         long var4 = var3.getAmount();

         for (int var6 = 0; var6 < 2 && var4 > 0L; var6++) {
            for (ConfigurableFluidStack var8 : var0) {
               boolean var9 = var8.getResource().equals(var3.getResource());
               boolean var10 = var8.isEmpty();
               if ((var6 != 0 || var9) && (var6 != 1 || var10) && var8.isResourceAllowedByLock(var3.getResource())) {
                  long var11 = Math.min(var4, var8.getRemainingSpace());
                  if (var11 > 0L) {
                     if (var10) {
                        var8.setKey(var3.getResource());
                     }

                     var8.increment(var11);
                     var4 -= var11;
                  }

                  if (var4 == 0L) {
                     break;
                  }
               }
            }
         }

         var3.setAmount(var4);
         if (var4 > 0L) {
            return false;
         }
      }

      return true;
   }

   private static boolean flushItems(List<ConfigurableItemStack> var0, List<ConfigurableItemStack> var1) {
      long var2 = var1.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
      insertAllItems(var0, var1);
      long var4 = var1.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
      return var2 != var4;
   }

   private static boolean flushFluids(List<ConfigurableFluidStack> var0, List<ConfigurableFluidStack> var1) {
      long var2 = var1.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
      insertAllFluids(var0, var1);
      long var4 = var1.stream().mapToLong(AbstractConfigurableStack::getAmount).sum();
      return var2 != var4;
   }

   private static List<ConfigurableItemStack> copyItems(List<ConfigurableItemStack> var0) {
      return ConfigurableItemStack.copyList(var0);
   }

   private static List<ConfigurableFluidStack> copyFluids(List<ConfigurableFluidStack> var0) {
      return ConfigurableFluidStack.copyList(var0);
   }

   private static RecipeHolder<MachineRecipe> getRecipe(Behavior var0, ResourceLocation var1) {
      return var1 == null ? null : var0.recipeType().getRecipe(var0.getCrafterWorld(), var1);
   }

   private static Context conditionContext(MachineBlockEntity var0) {
      return () -> var0;
   }

   private static long getRecipeMaxEu(Behavior var0, MachineRecipe var1, int var2) {
      long var3 = var1.getTotalEu();
      long var5 = Math.max(var0.getBaseRecipeEu(), var1.eu);
      long var7 = var5 + var2 * var3 / 600L;
      return Math.min(var3, Math.min(var7, var0.getMaxRecipeEu()));
   }

   private static int getRecipeMaxEfficiencyTicks(Behavior var0, MachineRecipe var1) {
      long var2 = Math.min(var0.getMaxRecipeEu(), var1.getTotalEu());

      for (int var4 = 0; var4 < Integer.MAX_VALUE; var4++) {
         if (getRecipeMaxEu(var0, var1, var4) == var2) {
            return var4;
         }
      }

      return 0;
   }

   private static long saturatedMultiply(long var0, long var2) {
      if (var0 == 0L || var2 == 0L) {
         return 0L;
      } else {
         return var0 > Long.MAX_VALUE / var2 ? Long.MAX_VALUE : var0 * var2;
      }
   }

   private static long saturatedAdd(long var0, long var2) {
      return Long.MAX_VALUE - var0 < var2 ? Long.MAX_VALUE : var0 + var2;
   }

   private static long divideCeil(long var0, int var2) {
      return var0 == 0L ? 0L : 1L + (var0 - 1L) / var2;
   }

   public void writeNbt(CompoundTag var1, HolderLookup.Provider var2) {
      CompoundTag var3 = new CompoundTag();
      var3.putInt("version", 2);
      var3.putInt("fairnessCursor", this.fairnessCursor);
      ListTag var4 = new ListTag();

      for (CrossThreadRecipeManager.RecipeThreadState var6 : this.states.values()) {
         if (var6.recipeId != null || var6.hasWork() || var6.efficiencyTicks != 0) {
            CompoundTag var7 = new CompoundTag();
            var7.putString("room", var6.roomId);
            if (var6.recipeId != null) {
               var7.putString("recipe", var6.recipeId.toString());
            }

            var7.putInt("parallel", var6.parallel);
            var7.putDouble("energyFactor", var6.energyFactor);
            var7.putLong("usedEnergy", var6.usedEnergy);
            var7.putLong("totalEnergy", var6.totalEnergy);
            var7.putInt("efficiencyTicks", var6.efficiencyTicks);
            var7.putInt("maxEfficiencyTicks", var6.maxEfficiencyTicks);
            var7.putBoolean("running", var6.running);
            var7.putBoolean("outputsReady", var6.outputsReady);
            var7.put("itemOutputs", writeItems(var6.heldItemOutputs, var2));
            var7.put("fluidOutputs", writeFluids(var6.heldFluidOutputs, var2));
            var4.add(var7);
         }
      }

      var3.put("threads", var4);
      var1.put("aeindCrossThreadRecipes", var3);
   }

   public void readNbt(CompoundTag var1, HolderLookup.Provider var2) {
      this.states.clear();
      if (var1.contains("aeindCrossThreadRecipes", 10)) {
         CompoundTag var3 = var1.getCompound("aeindCrossThreadRecipes");
         this.fairnessCursor = var3.getInt("fairnessCursor");
         ListTag var4 = var3.getList("threads", 10);

         for (int var5 = 0; var5 < var4.size(); var5++) {
            CompoundTag var6 = var4.getCompound(var5);
            String var7 = var6.getString("room");
            if (!var7.isEmpty()) {
               CrossThreadRecipeManager.RecipeThreadState var8 = new CrossThreadRecipeManager.RecipeThreadState(var7);
               var8.recipeId = var6.contains("recipe") ? ResourceLocation.tryParse(var6.getString("recipe")) : null;
               var8.parallel = Math.max(1, var6.getInt("parallel"));
               double var9 = var6.contains("energyFactor", 6) ? var6.getDouble("energyFactor") : var8.parallel;
               var8.energyFactor = Double.isFinite(var9) && var9 >= 1.0 ? var9 : var8.parallel;
               var8.usedEnergy = Math.max(0L, var6.getLong("usedEnergy"));
               var8.totalEnergy = Math.max(0L, var6.getLong("totalEnergy"));
               var8.efficiencyTicks = Math.max(0, var6.getInt("efficiencyTicks"));
               var8.maxEfficiencyTicks = Math.max(0, var6.getInt("maxEfficiencyTicks"));
               var8.running = var6.getBoolean("running");
               var8.outputsReady = var6.getBoolean("outputsReady");
               var8.heldItemOutputs = readItems(var6.getList("itemOutputs", 10), var2);
               var8.heldFluidOutputs = readFluids(var6.getList("fluidOutputs", 10), var2);
               this.states.put(var7, var8);
            }
         }
      }
   }

   private static ListTag writeItems(List<ConfigurableItemStack> var0, HolderLookup.Provider var1) {
      ListTag var2 = new ListTag();

      for (ConfigurableItemStack var4 : var0) {
         var2.add(var4.toNbt(var1));
      }

      return var2;
   }

   private static ListTag writeFluids(List<ConfigurableFluidStack> var0, HolderLookup.Provider var1) {
      ListTag var2 = new ListTag();

      for (ConfigurableFluidStack var4 : var0) {
         var2.add(var4.toNbt(var1));
      }

      return var2;
   }

   private static List<ConfigurableItemStack> readItems(ListTag var0, HolderLookup.Provider var1) {
      ArrayList<ConfigurableItemStack> var2 = new ArrayList<>();

      for (int var3 = 0; var3 < var0.size(); var3++) {
         var2.add(new ConfigurableItemStack(var0.getCompound(var3), var1));
      }

      return var2;
   }

   private static List<ConfigurableFluidStack> readFluids(ListTag var0, HolderLookup.Provider var1) {
      ArrayList<ConfigurableFluidStack> var2 = new ArrayList<>();

      for (int var3 = 0; var3 < var0.size(); var3++) {
         var2.add(new ConfigurableFluidStack(var0.getCompound(var3), var1));
      }

      return var2;
   }

   public record ProgressSnapshot(List<CrossThreadRecipeManager.ThreadProgress> threads) {
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
      private List<ConfigurableItemStack> heldItemOutputs = new ArrayList<>();
      private List<ConfigurableFluidStack> heldFluidOutputs = new ArrayList<>();

      private RecipeThreadState(String var1) {
         this.roomId = var1;
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
