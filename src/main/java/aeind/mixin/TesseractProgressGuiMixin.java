package aeind.mixin;

import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadProgressGui;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.multiblock.multiplied.AbstractMultipliedCraftingMultiblockBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMultipliedCraftingMultiblockBlockEntity.class)
public abstract class TesseractProgressGuiMixin {
    @Unique private boolean aeind$guiRegistered;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void aeind$registerProgressGui(CallbackInfo ci) {
        if (!aeind$guiRegistered && this instanceof CrossThreadControllerAccess access) {
            ((MachineBlockEntity)(Object)this).guiComponents.register(new CrossThreadProgressGui(access.aeind$getCrossThreadManager()));
            aeind$guiRegistered = true;
        }
    }
}
