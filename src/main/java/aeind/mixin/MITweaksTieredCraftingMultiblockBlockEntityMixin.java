package aeind.mixin;

import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadProgressGui;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.cross_thread.CrossThreadTickBridge;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "net.swedz.mi_tweaks.machine.blockentity.multiblock.tiered.TieredCraftingMultiblockBlockEntity",
        remap = false
)
public abstract class MITweaksTieredCraftingMultiblockBlockEntityMixin implements CrossThreadControllerAccess {
    @Shadow @Final protected MultipliedCrafterComponent crafter;
    @Unique private final CrossThreadRecipeManager aeind$crossThreadManager = new CrossThreadRecipeManager();
    @Unique private boolean aeind$crossThreadProgressGuiRegistered;

    @Override
    public CrossThreadRecipeManager aeind$getCrossThreadManager() {
        return aeind$crossThreadManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 0, remap = false)
    private void aeind$registerCrossThreadProgressGui(CallbackInfo callbackInfo) {
        if (!aeind$crossThreadProgressGuiRegistered) {
            ((MachineBlockEntity) (Object) this).guiComponents.register(new CrossThreadProgressGui(aeind$crossThreadManager));
            aeind$crossThreadProgressGuiRegistered = true;
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/swedz/tesseract/neoforge/compat/mi/component/craft/multiplied/MultipliedCrafterComponent;tickRecipe()Z", remap = false)
    )
    private boolean aeind$tickIsolatedTiered(MultipliedCrafterComponent component) {
        return CrossThreadTickBridge.tickTesseract(
                aeind$crossThreadManager,
                (MachineBlockEntity) (Object) this,
                component
        );
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/swedz/tesseract/neoforge/compat/mi/component/craft/multiplied/MultipliedCrafterComponent;tryContinueRecipe()Z", remap = false)
    )
    private boolean aeind$resumeIsolatedTiered(MultipliedCrafterComponent component) {
        return CrossThreadTickBridge.resumeTesseract(aeind$crossThreadManager, component);
    }
}
