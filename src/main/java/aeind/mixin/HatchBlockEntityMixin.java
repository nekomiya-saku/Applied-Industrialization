package aeind.mixin;

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HatchBlockEntity.class)
public abstract class HatchBlockEntityMixin {
   @Inject(method = "link", at = @At("TAIL"))
   private void aeind$syncLinkedCasing(MachineCasing casing, CallbackInfo ci) {
      this.aeind$syncCustomHatchCasing();
   }

   @Inject(method = "unlink", at = @At("TAIL"))
   private void aeind$syncUnlinkedCasing(CallbackInfo ci) {
      this.aeind$syncCustomHatchCasing();
   }

   @Unique
   private void aeind$syncCustomHatchCasing() {
      HatchBlockEntity hatch = (HatchBlockEntity)(Object)this;
      if (hatch.getLevel() == null || hatch.getLevel().isClientSide()) {
         return;
      }

      if (BuiltInRegistries.BLOCK.getKey(hatch.getBlockState().getBlock()).getNamespace().equals("aeind")) {
         hatch.setChanged();
         hatch.sync();
      }
   }
}
