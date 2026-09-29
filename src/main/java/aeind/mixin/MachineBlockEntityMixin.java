package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aeind.cross_thread.CrossThreadControllerAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MachineBlockEntity.class)
public abstract class MachineBlockEntityMixin {
   @Inject(method = "saveAdditional", at = @At("TAIL"))
   private void aeind$saveCrossThreadRecipes(CompoundTag var1, HolderLookup.Provider var2, CallbackInfo var3) {
      if (this instanceof CrossThreadControllerAccess var4) {
         var4.aeind$getCrossThreadManager().writeNbt(var1, var2);
      }
   }

   @Inject(method = "loadAdditional", at = @At("TAIL"))
   private void aeind$loadCrossThreadRecipes(CompoundTag var1, HolderLookup.Provider var2, CallbackInfo var3) {
      if (this instanceof CrossThreadControllerAccess var4) {
         var4.aeind$getCrossThreadManager().readNbt(var1, var2);
      }
   }
}
