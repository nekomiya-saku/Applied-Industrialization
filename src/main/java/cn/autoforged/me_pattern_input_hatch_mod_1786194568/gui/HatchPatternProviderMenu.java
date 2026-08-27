/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class HatchPatternProviderMenu
extends PatternProviderMenu {
    private final MEPatternInputHatchBlockEntity hatch;

    public HatchPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.hatch = (MEPatternInputHatchBlockEntity)host;
    }

    public void returnMaterial() {
        if (this.hatch != null) {
            this.hatch.returnAllBufferToNetwork();
        }
    }
}

