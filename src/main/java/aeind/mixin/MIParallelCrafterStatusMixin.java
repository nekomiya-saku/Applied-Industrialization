/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.components.CrafterComponent
 *  aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition$Context
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aeind.cross_thread.CrossThreadControllerAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={CrafterComponent.class}, priority=900)
public abstract class MIParallelCrafterStatusMixin {
    @Shadow
    @Final
    private MachineProcessCondition.Context conditionContext;

    @Inject(method={"miParallelHatch$getActiveParallelRecipes"}, at={@At(value="HEAD")}, cancellable=true, require=0, remap=false)
    private void aeind$crossThreadActiveParallel(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        CrossThreadControllerAccess crossThreadControllerAccess;
        int n;
        MachineBlockEntity machineBlockEntity = this.conditionContext.getBlockEntity();
        if (machineBlockEntity instanceof CrossThreadControllerAccess && (n = (crossThreadControllerAccess = (CrossThreadControllerAccess)machineBlockEntity).aeind$getCrossThreadManager().getMaxActiveParallel()) > 0) {
            callbackInfoReturnable.setReturnValue((Object)n);
        }
    }

    @Inject(method={"miParallelHatch$getCurrentEnergyFactor"}, at={@At(value="HEAD")}, cancellable=true, require=0, remap=false)
    private void aeind$crossThreadEnergyFactor(CallbackInfoReturnable<Double> callbackInfoReturnable) {
        CrossThreadControllerAccess crossThreadControllerAccess;
        MachineBlockEntity machineBlockEntity = this.conditionContext.getBlockEntity();
        if (machineBlockEntity instanceof CrossThreadControllerAccess && (crossThreadControllerAccess = (CrossThreadControllerAccess)machineBlockEntity).aeind$getCrossThreadManager().getActiveThreadCount() > 0) {
            callbackInfoReturnable.setReturnValue((Object)crossThreadControllerAccess.aeind$getCrossThreadManager().getMaxActiveEnergyFactor());
        }
    }
}

