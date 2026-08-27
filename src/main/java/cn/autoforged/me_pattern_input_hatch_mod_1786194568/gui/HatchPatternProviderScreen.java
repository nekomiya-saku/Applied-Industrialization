/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.PatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.HatchPatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.network.ServerboundReturnMaterialPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class HatchPatternProviderScreen
extends PatternProviderScreen<HatchPatternProviderMenu> {
    private Button returnMaterialButton;

    public HatchPatternProviderScreen(HatchPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((PatternProviderMenu)menu, playerInventory, title, style);
        this.setTextContent("dialog_title", (Component)Component.translatable((String)"block.aeind.me_pattern_input_hatch"));
    }

    protected void init() {
        super.init();
        this.returnMaterialButton = Button.builder((Component)Component.translatable((String)"button.aeind.return_material.label"), btn -> PacketDistributor.sendToServer((CustomPacketPayload)new ServerboundReturnMaterialPayload(), (CustomPacketPayload[])new CustomPacketPayload[0])).bounds(0, 0, 20, 16).build();
        this.returnMaterialButton.setTooltip(Tooltip.create((Component)Component.translatable((String)"button.aeind.return_material.tooltip")));
        this.returnMaterialButton.setX(this.leftPos + 156);
        this.returnMaterialButton.setY(this.topPos + 97);
        this.addRenderableWidget((GuiEventListener)this.returnMaterialButton);
    }
}

