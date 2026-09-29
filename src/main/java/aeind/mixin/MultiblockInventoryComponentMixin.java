package aeind.mixin;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aeind.isolation.CrossThreadParallelHatch;
import aeind.isolation.IsolatedInputProvider;
import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationHatch;
import aeind.isolation.ThreadIsolationRoom;
import aeind.isolation.ThreadIsolationState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiblockInventoryComponent.class)
public abstract class MultiblockInventoryComponentMixin implements ThreadIsolationAccess {
   private boolean aeind$isolationEnabled;
   private boolean aeind$crossThreadEnabled;
   private boolean aeind$overdriveBlocked;
   private int aeind$maxParallelPerThread = 1;
   private List<ThreadIsolationRoom> aeind$rooms = List.of();

   @Inject(method = "rebuild", at = @At("RETURN"))
   private void aeind$rebuildRooms(ShapeMatcher var1, CallbackInfo var2) {
      List<HatchBlockEntity> var3 = var1.getMatchedHatches();
      boolean var4 = var3.stream().anyMatch(var0 -> var0 instanceof ThreadIsolationHatch);
      boolean var5 = var3.stream().anyMatch(var0 -> var0 instanceof IsolatedInputProvider);
      this.aeind$crossThreadEnabled = var3.stream().anyMatch(var0 -> var0 instanceof CrossThreadParallelHatch);
      this.aeind$overdriveBlocked = var4 || var5 || this.aeind$crossThreadEnabled;
      this.aeind$maxParallelPerThread = var3.stream()
         .filter(CrossThreadParallelHatch.class::isInstance)
         .map(CrossThreadParallelHatch.class::cast)
         .mapToInt(CrossThreadParallelHatch::aeind$maxParallelPerThread)
         .max()
         .orElse(1);
      this.aeind$isolationEnabled = var4 || var5;
      if (!this.aeind$isolationEnabled) {
         this.aeind$rooms = List.of();
         this.aeind$overdriveBlocked = false;
      } else {
         ArrayList<ThreadIsolationRoom> var6 = new ArrayList<>(var3.size());
         ArrayList<ConfigurableItemStack> var7 = new ArrayList<>();
         ArrayList<ConfigurableFluidStack> var8 = new ArrayList<>();

         for (HatchBlockEntity var10 : var3) {
            if (var10 instanceof IsolatedInputProvider var13) {
               var6.addAll(var13.aeind$isolatedInputRooms());
            } else {
               ArrayList<ConfigurableItemStack> var11 = new ArrayList<>();
               ArrayList<ConfigurableFluidStack> var12 = new ArrayList<>();
               var10.appendItemInputs(var11);
               var10.appendFluidInputs(var12);
               if (var4) {
                  if (!var11.isEmpty() || !var12.isEmpty()) {
                     var6.add(new ThreadIsolationRoom("hatch:" + var10.getBlockPos().asLong(), var11, var12));
                  }
               } else {
                  var7.addAll(var11);
                  var8.addAll(var12);
               }
            }
         }

         if (!var4 && (!var7.isEmpty() || !var8.isEmpty())) {
            var6.add(new ThreadIsolationRoom("ordinary", var7, var8));
         }

         this.aeind$rooms = List.copyOf(var6);
      }
   }

   @Inject(method = "getItemInputs", at = @At("RETURN"), cancellable = true)
   private void aeind$activeItemInputs(CallbackInfoReturnable<List<ConfigurableItemStack>> var1) {
      ThreadIsolationRoom var2 = ThreadIsolationState.get();
      if (var2 != null) {
         var1.setReturnValue(var2.itemInputs());
      }
   }

   @Inject(method = "getFluidInputs", at = @At("RETURN"), cancellable = true)
   private void aeind$activeFluidInputs(CallbackInfoReturnable<List<ConfigurableFluidStack>> var1) {
      ThreadIsolationRoom var2 = ThreadIsolationState.get();
      if (var2 != null) {
         var1.setReturnValue(var2.fluidInputs());
      }
   }

   @Override
   public boolean aeind$isolationEnabled() {
      return this.aeind$isolationEnabled;
   }

   @Override
   public List<ThreadIsolationRoom> aeind$isolationRooms() {
      return Collections.unmodifiableList(this.aeind$rooms);
   }

   @Override
   public boolean aeind$crossThreadEnabled() {
      return this.aeind$crossThreadEnabled;
   }

   @Override
   public int aeind$maxParallelPerThread() {
      return this.aeind$maxParallelPerThread;
   }

   @Override
   public boolean aeind$overdriveBlocked() {
      return this.aeind$overdriveBlocked;
   }
}
