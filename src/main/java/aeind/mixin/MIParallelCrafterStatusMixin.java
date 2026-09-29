package aeind.mixin;

import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition.Context;
import aeind.cross_thread.CrossThreadControllerAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrafterComponent.class, priority = 900)
public abstract class MIParallelCrafterStatusMixin {
   @Shadow
   @Final
   private Context conditionContext;

   @Inject(method = "miParallelHatch$getActiveParallelRecipes", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void aeind$crossThreadActiveParallel(CallbackInfoReturnable<Integer> var1) {
      if (this.conditionContext.getBlockEntity() instanceof CrossThreadControllerAccess var2) {
         int var4 = var2.aeind$getCrossThreadManager().getMaxActiveParallel();
         if (var4 > 0) {
            var1.setReturnValue(var4);
         }
      }
   }

   @Inject(method = "miParallelHatch$getCurrentEnergyFactor", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void aeind$crossThreadEnergyFactor(CallbackInfoReturnable<Double> var1) {
      if (this.conditionContext.getBlockEntity() instanceof CrossThreadControllerAccess var2 && var2.aeind$getCrossThreadManager().getActiveThreadCount() > 0) {
         var1.setReturnValue(var2.aeind$getCrossThreadManager().getMaxActiveEnergyFactor());
      }
   }
}
