/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity
 *  aztech.modern_industrialization.machines.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity
 *  aztech.modern_industrialization.machines.gui.GuiComponentServer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread.CrossThreadControllerAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread.CrossThreadProgressGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractElectricCraftingMultiblockBlockEntity.class, SteamCraftingMultiblockBlockEntity.class})
public abstract class CraftingMultiblockProgressGuiMixin {
    @Unique
    private boolean aeind$crossThreadProgressGuiRegistered;

    @Inject(method={"<init>"}, at={@At(value="RETURN")}, require=0, remap=false)
    private void aeind$registerCrossThreadProgressGui(CallbackInfo callbackInfo) {
        if (this.aeind$crossThreadProgressGuiRegistered) {
            return;
        }
        CraftingMultiblockProgressGuiMixin craftingMultiblockProgressGuiMixin = this;
        if (!(craftingMultiblockProgressGuiMixin instanceof CrossThreadControllerAccess)) {
            return;
        }
        CrossThreadControllerAccess crossThreadControllerAccess = (CrossThreadControllerAccess)((Object)craftingMultiblockProgressGuiMixin);
        craftingMultiblockProgressGuiMixin = (MachineBlockEntity)this;
        ((MachineBlockEntity)craftingMultiblockProgressGuiMixin).guiComponents.register((Object[])new GuiComponentServer[]{new CrossThreadProgressGui(crossThreadControllerAccess.aeind$getCrossThreadManager())});
        this.aeind$crossThreadProgressGuiRegistered = true;
    }
}

