/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aeind.cross_thread.CrossThreadControllerAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MachineBlockEntity.class})
public abstract class MachineBlockEntityMixin {
    @Inject(method={"saveAdditional"}, at={@At(value="TAIL")})
    private void aeind$saveCrossThreadRecipes(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo callbackInfo) {
        MachineBlockEntityMixin machineBlockEntityMixin = this;
        if (machineBlockEntityMixin instanceof CrossThreadControllerAccess) {
            CrossThreadControllerAccess crossThreadControllerAccess = (CrossThreadControllerAccess)((Object)machineBlockEntityMixin);
            crossThreadControllerAccess.aeind$getCrossThreadManager().writeNbt(compoundTag, provider);
        }
    }

    @Inject(method={"loadAdditional"}, at={@At(value="TAIL")})
    private void aeind$loadCrossThreadRecipes(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo callbackInfo) {
        MachineBlockEntityMixin machineBlockEntityMixin = this;
        if (machineBlockEntityMixin instanceof CrossThreadControllerAccess) {
            CrossThreadControllerAccess crossThreadControllerAccess = (CrossThreadControllerAccess)((Object)machineBlockEntityMixin);
            crossThreadControllerAccess.aeind$getCrossThreadManager().readNbt(compoundTag, provider);
        }
    }
}

