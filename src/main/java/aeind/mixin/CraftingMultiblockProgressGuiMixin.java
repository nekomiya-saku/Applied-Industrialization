package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadProgressGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractElectricCraftingMultiblockBlockEntity.class, SteamCraftingMultiblockBlockEntity.class})
public abstract class CraftingMultiblockProgressGuiMixin {
   @Unique
   private boolean aeind$crossThreadProgressGuiRegistered;

   @Inject(method = "<init>", at = @At("RETURN"), require = 0, remap = false)
   private void aeind$registerCrossThreadProgressGui(CallbackInfo var1) {
      if (!this.aeind$crossThreadProgressGuiRegistered) {
         if (this instanceof CrossThreadControllerAccess var2) {
            MachineBlockEntity var4 = (MachineBlockEntity)(Object)this;
            var4.guiComponents.register(new CrossThreadProgressGui(var2.aeind$getCrossThreadManager()));
            this.aeind$crossThreadProgressGuiRegistered = true;
         }
      }
   }
}
