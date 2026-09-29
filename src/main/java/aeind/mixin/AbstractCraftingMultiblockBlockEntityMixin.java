package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.isolation.OverdriveBlocker;
import aeind.isolation.ThreadIsolationAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCraftingMultiblockBlockEntity.class)
public abstract class AbstractCraftingMultiblockBlockEntityMixin implements CrossThreadControllerAccess {
   @Unique
   private final CrossThreadRecipeManager aeind$crossThreadManager = new CrossThreadRecipeManager();

   @Override
   public CrossThreadRecipeManager aeind$getCrossThreadManager() {
      return this.aeind$crossThreadManager;
   }

   @Redirect(
      method = "tick",
      at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/components/CrafterComponent;tickRecipe()Z", remap = false)
   )
   private boolean aeind$tickCrossThreadRecipes(CrafterComponent var1) {
      if (var1.getInventory() instanceof ThreadIsolationAccess var2) {
         if (var2.aeind$crossThreadEnabled()) {
            CrafterComponentAccessor var4 = (CrafterComponentAccessor)var1;
            if (!this.aeind$crossThreadManager.hasWork() && var4.aeind$getUsedEnergy() > 0L) {
               return var1.tickRecipe();
            }

            if (var4.aeind$getUsedEnergy() == 0L) {
               aeind$resetIdleLegacyCrafter(var4);
            }

            return this.aeind$crossThreadManager.tick((MachineBlockEntity)(Object)this, var1, var2, true);
         } else {
            return this.aeind$crossThreadManager.hasWork()
               ? this.aeind$crossThreadManager.tick((MachineBlockEntity)(Object)this, var1, var2, false)
               : var1.tickRecipe();
         }
      } else {
         return var1.tickRecipe();
      }
   }

   @Inject(method = "onRematch", at = @At("TAIL"))
   private void aeind$clearOverdriveWhenIsolated(ShapeMatcher var1, CallbackInfo var2) {
      if (var1.isMatchSuccessful()) {
         MachineBlockEntity var3 = (MachineBlockEntity)(Object)this;
         if (OverdriveBlocker.isBlocked(var3) && OverdriveBlocker.clear(var3)) {
            var3.setChanged();
            if (!var3.getLevel().isClientSide()) {
               var3.sync();
            }
         }
      }
   }

   @Unique
   private static void aeind$resetIdleLegacyCrafter(CrafterComponentAccessor var0) {
      var0.aeind$setActiveRecipe(null);
      var0.aeind$setDelayedActiveRecipe(null);
      var0.aeind$setUsedEnergy(0L);
      var0.aeind$setRecipeEnergy(0L);
      var0.aeind$setRecipeMaxEu(0L);
      var0.aeind$setEfficiencyTicks(0);
      var0.aeind$setMaxEfficiencyTicks(0);
   }
}
