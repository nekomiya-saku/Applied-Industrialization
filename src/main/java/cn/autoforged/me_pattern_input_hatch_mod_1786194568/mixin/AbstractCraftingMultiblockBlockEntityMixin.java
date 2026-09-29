/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity
 *  aztech.modern_industrialization.machines.components.CrafterComponent
 *  aztech.modern_industrialization.machines.multiblocks.ShapeMatcher
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread.CrossThreadControllerAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread.CrossThreadRecipeManager;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.OverdriveBlocker;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin.CrafterComponentAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractCraftingMultiblockBlockEntity.class})
public abstract class AbstractCraftingMultiblockBlockEntityMixin
implements CrossThreadControllerAccess {
    @Unique
    private final CrossThreadRecipeManager aeind$crossThreadManager = new CrossThreadRecipeManager();

    @Override
    public CrossThreadRecipeManager aeind$getCrossThreadManager() {
        return this.aeind$crossThreadManager;
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Laztech/modern_industrialization/machines/components/CrafterComponent;tickRecipe()Z", remap=false))
    private boolean aeind$tickCrossThreadRecipes(CrafterComponent crafterComponent) {
        Object object = crafterComponent.getInventory();
        if (!(object instanceof ThreadIsolationAccess)) {
            return crafterComponent.tickRecipe();
        }
        ThreadIsolationAccess threadIsolationAccess = (ThreadIsolationAccess)object;
        if (threadIsolationAccess.aeind$crossThreadEnabled()) {
            object = (CrafterComponentAccessor)crafterComponent;
            if (!this.aeind$crossThreadManager.hasWork() && object.aeind$getUsedEnergy() > 0L) {
                return crafterComponent.tickRecipe();
            }
            if (object.aeind$getUsedEnergy() == 0L) {
                AbstractCraftingMultiblockBlockEntityMixin.aeind$resetIdleLegacyCrafter((CrafterComponentAccessor)object);
            }
            return this.aeind$crossThreadManager.tick((MachineBlockEntity)this, crafterComponent, threadIsolationAccess, true);
        }
        if (this.aeind$crossThreadManager.hasWork()) {
            return this.aeind$crossThreadManager.tick((MachineBlockEntity)this, crafterComponent, threadIsolationAccess, false);
        }
        return crafterComponent.tickRecipe();
    }

    @Inject(method={"onRematch"}, at={@At(value="TAIL")})
    private void aeind$clearOverdriveWhenIsolated(ShapeMatcher shapeMatcher, CallbackInfo callbackInfo) {
        MachineBlockEntity machineBlockEntity;
        if (shapeMatcher.isMatchSuccessful() && OverdriveBlocker.isBlocked(machineBlockEntity = (MachineBlockEntity)this) && OverdriveBlocker.clear(machineBlockEntity)) {
            machineBlockEntity.setChanged();
            if (!machineBlockEntity.getLevel().isClientSide()) {
                machineBlockEntity.sync();
            }
        }
    }

    @Unique
    private static void aeind$resetIdleLegacyCrafter(CrafterComponentAccessor crafterComponentAccessor) {
        crafterComponentAccessor.aeind$setActiveRecipe(null);
        crafterComponentAccessor.aeind$setDelayedActiveRecipe(null);
        crafterComponentAccessor.aeind$setUsedEnergy(0L);
        crafterComponentAccessor.aeind$setRecipeEnergy(0L);
        crafterComponentAccessor.aeind$setRecipeMaxEu(0L);
        crafterComponentAccessor.aeind$setEfficiencyTicks(0);
        crafterComponentAccessor.aeind$setMaxEfficiencyTicks(0);
    }
}

