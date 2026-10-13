package aeind.mixin;

import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.cross_thread.CrossThreadProgressGui;
import aeind.cross_thread.CrossThreadRecipeManager;
import aeind.cross_thread.CrossThreadTickBridge;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = {
                "icu.kudikan.modern_mekanistics.machine.blockentity.multiblock.AbstractChemicalCraftingMultiblockBlockEntity",
                "icu.kudikan.modern_mekanistics.machine.blockentity.multiblock.tiered.TieredChemicalCraftingMultiblockBlockEntity"
        },
        remap = false
)
public abstract class ModernMekanisticsChemicalMultiblockMixin implements CrossThreadControllerAccess {
    @Unique private final CrossThreadRecipeManager aeind$crossThreadManager = new CrossThreadRecipeManager();
    @Unique private boolean aeind$crossThreadProgressGuiRegistered;

    @Override
    public CrossThreadRecipeManager aeind$getCrossThreadManager() {
        return this.aeind$crossThreadManager;
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 0, remap = false)
    private void aeind$registerCrossThreadProgressGui(CallbackInfo callbackInfo) {
        if (!this.aeind$crossThreadProgressGuiRegistered) {
            ((MachineBlockEntity) (Object) this).guiComponents.register(new CrossThreadProgressGui(this.aeind$crossThreadManager));
            this.aeind$crossThreadProgressGuiRegistered = true;
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Licu/kudikan/modern_mekanistics/machine/component/ChemicalCrafterComponent;tickRecipe()Z", remap = false),
            remap = false
    )
    private boolean aeind$tickChemicalThreads(@Coerce Object component) {
        return CrossThreadTickBridge.tickModernMekChemical(
                this.aeind$crossThreadManager,
                (MachineBlockEntity) (Object) this,
                component);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Licu/kudikan/modern_mekanistics/machine/component/ChemicalCrafterComponent;tryContinueRecipe()Z", remap = false),
            remap = false
    )
    private boolean aeind$resumeChemicalThreads(@Coerce Object component) {
        return CrossThreadTickBridge.resumeModernMekChemical(this.aeind$crossThreadManager, component);
    }
}
