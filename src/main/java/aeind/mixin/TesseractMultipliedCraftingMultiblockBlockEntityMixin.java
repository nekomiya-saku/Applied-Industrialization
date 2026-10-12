package aeind.mixin;

import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.cross_thread.CrossThreadTickBridge;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.multiblock.multiplied.AbstractMultipliedCraftingMultiblockBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMultipliedCraftingMultiblockBlockEntity.class)
public abstract class TesseractMultipliedCraftingMultiblockBlockEntityMixin implements CrossThreadControllerAccess {
    @Shadow @Final protected MultipliedCrafterComponent crafter;
    @Unique private final CrossThreadRecipeManager aeind$crossThreadManager = new CrossThreadRecipeManager();

    @Override
    public CrossThreadRecipeManager aeind$getCrossThreadManager() {
        return aeind$crossThreadManager;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/swedz/tesseract/neoforge/compat/mi/component/craft/multiplied/MultipliedCrafterComponent;tickRecipe()Z", remap = false))
    private boolean aeind$tickIsolatedTesseract(MultipliedCrafterComponent component) {
        return CrossThreadTickBridge.tickTesseract(
                aeind$crossThreadManager,
                (MachineBlockEntity) (Object) this,
                component
        );
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/swedz/tesseract/neoforge/compat/mi/component/craft/multiplied/MultipliedCrafterComponent;tryContinueRecipe()Z", remap = false))
    private boolean aeind$resumeIsolatedTesseract(MultipliedCrafterComponent component) {
        return CrossThreadTickBridge.resumeTesseract(aeind$crossThreadManager, component);
    }
}
