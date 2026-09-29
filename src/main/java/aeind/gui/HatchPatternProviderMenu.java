/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.helpers.patternprovider.PatternProviderLogicHost
 *  appeng.menu.implementations.PatternProviderMenu
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package aeind.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import aeind.blockentity.PatternInputHatchHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class HatchPatternProviderMenu
extends PatternProviderMenu {
    private final PatternInputHatchHost hatch;
    private final PatternProviderLogicHost providerHost;

    public HatchPatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int n, Inventory inventory, PatternProviderLogicHost patternProviderLogicHost) {
        super(menuType, n, inventory, patternProviderLogicHost);
        this.providerHost = patternProviderLogicHost;
        this.hatch = (PatternInputHatchHost)patternProviderLogicHost;
    }

    public void returnMaterial() {
        if (this.hatch != null) {
            this.hatch.returnAllBufferToNetwork();
        }
    }

    public Component getProviderTitle() {
        return Component.translatable((String)"block.aeind.advanced_pattern_input_hatch");
    }
}

