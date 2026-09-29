/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.helpers.patternprovider.PatternProviderLogicHost
 *  appeng.menu.implementations.PatternProviderMenu
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ExtendedPatternInputHatchBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExtendedPatternProviderMenu
extends PatternProviderMenu {
    private final ExtendedPatternInputHatchBlockEntity hatch;

    public ExtendedPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int n, Inventory inventory, PatternProviderLogicHost patternProviderLogicHost) {
        super(menuType, n, inventory, patternProviderLogicHost);
        this.hatch = (ExtendedPatternInputHatchBlockEntity)patternProviderLogicHost;
    }

    public void returnMaterial() {
        if (this.hatch != null) {
            this.hatch.returnAllBufferToNetwork();
        }
    }
}

