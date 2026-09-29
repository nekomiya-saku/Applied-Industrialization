/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.client.gui.implementations.PatternProviderScreen
 *  appeng.client.gui.style.ScreenStyle
 *  appeng.menu.implementations.PatternProviderMenu
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Inventory
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package aeind.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.PatternProviderMenu;
import aeind.gui.ExtendedPatternProviderMenu;
import aeind.network.ServerboundReturnMaterialPayload;
import java.util.Objects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedPatternProviderScreen
extends PatternProviderScreen<ExtendedPatternProviderMenu> {
    private static final int RETURN_LABEL_LEFT = 8;
    private static final int RETURN_LABEL_TOP = 116;
    private static final int BUTTON_WIDTH = 9;
    private static final int BUTTON_HEIGHT = 9;
    private static final int BUTTON_GAP = 4;
    private Button returnMaterialButton;

    public ExtendedPatternProviderScreen(ExtendedPatternProviderMenu extendedPatternProviderMenu, Inventory inventory, Component component, ScreenStyle screenStyle) {
        super((PatternProviderMenu)extendedPatternProviderMenu, inventory, component, screenStyle);
        this.setTextContent("dialog_title", (Component)Component.translatable((String)"block.aeind.advanced_extended_pattern_input_hatch"));
    }

    protected void init() {
        super.init();
        this.returnMaterialButton = Button.builder((Component)Component.translatable((String)"button.aeind.return_material.label"), button -> PacketDistributor.sendToServer((CustomPacketPayload)new ServerboundReturnMaterialPayload(), (CustomPacketPayload[])new CustomPacketPayload[0])).bounds(0, 0, 9, 9).build();
        this.returnMaterialButton.setTooltip(Tooltip.create((Component)Component.translatable((String)"button.aeind.return_material.tooltip")));
        MutableComponent mutableComponent = Component.translatable((String)"gui.ae2.ReturnInventory");
        int n = this.leftPos + 8 + this.font.width((FormattedText)mutableComponent);
        Objects.requireNonNull(this.font);
        int n2 = this.topPos + 116 + 0;
        this.returnMaterialButton.setX(n + 4);
        this.returnMaterialButton.setY(n2);
        this.addRenderableWidget((GuiEventListener)this.returnMaterialButton);
    }
}

