package aeind.mixin;

import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadProgressGui;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.cross_thread.CrossThreadTickBridge;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import net.swedz.tesseract.neoforge.compat.mi.machine.blockentity.powerless.PowerlessMultiblockMachineBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PowerlessMultiblockMachineBlockEntity.class)
public abstract class TesseractPowerlessMultiblockMachineBlockEntityMixin implements CrossThreadControllerAccess {
    @Shadow @Final protected CrafterComponent crafter;
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
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/components/CrafterComponent;tickRecipe()Z", remap = false)
    )
    private boolean aeind$tickIsolatedPowerless(CrafterComponent component) {
        return CrossThreadTickBridge.tickNormal(
                aeind$crossThreadManager,
                (MachineBlockEntity) (Object) this,
                component
        );
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/components/CrafterComponent;tryContinueRecipe()Z", remap = false)
    )
    private boolean aeind$resumeIsolatedPowerless(CrafterComponent component) {
        return CrossThreadTickBridge.resumeNormal(aeind$crossThreadManager, component);
    }
}
