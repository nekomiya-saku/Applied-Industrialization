package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.cross_thread.CrossThreadTickBridge;
import aeind.isolation.OverdriveBlocker;
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
      return CrossThreadTickBridge.tickNormal(
            this.aeind$crossThreadManager,
            (MachineBlockEntity) (Object) this,
            var1
      );
   }

   @Redirect(
      method = "tick",
      at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/components/CrafterComponent;tryContinueRecipe()Z", remap = false)
   )
   private boolean aeind$resumeCrossThreadRecipes(CrafterComponent component) {
      return CrossThreadTickBridge.resumeNormal(this.aeind$crossThreadManager, component);
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
}
