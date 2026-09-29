/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.client.machines.gui.ClientComponentRenderer
 *  aztech.modern_industrialization.client.machines.gui.GuiComponentClient
 *  aztech.modern_industrialization.client.machines.gui.MachineScreen
 *  aztech.modern_industrialization.client.util.RenderHelper
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Unit
 */
package aeind.client;

import aztech.modern_industrialization.client.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.client.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.client.util.RenderHelper;
import aeind.cross_thread.CrossThreadProgressGui;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Unit;

public final class CrossThreadProgressGuiClient
extends GuiComponentClient<Unit, CrossThreadProgressGui.Data> {
    private static final int MAX_VISIBLE_THREADS = 2;
    private static final int BAR_WIDTH = 42;
    private static final int BAR_HEIGHT = 6;

    public CrossThreadProgressGuiClient(Unit unit, CrossThreadProgressGui.Data data) {
        super((Object)unit, (Object)data);
    }

    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    private final class Renderer
    implements ClientComponentRenderer {
        private Renderer() {
        }

        public void renderBackground(GuiGraphics guiGraphics, int n, int n2) {
            if (((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().isEmpty()) {
                return;
            }
            Font font = Minecraft.getInstance().font;
            int n3 = ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().stream().mapToInt(CrossThreadProgressGui.ThreadData::parallel).sum();
            MutableComponent mutableComponent = Component.translatable((String)"gui.aeind.cross_thread.title", (Object[])new Object[]{((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size(), n3});
            guiGraphics.drawString(font, (Component)mutableComponent, n + 10, n2 + 58, 0xFFFFFF, false);
            int n4 = Math.min(2, ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size());
            for (int i = 0; i < n4; ++i) {
                CrossThreadProgressGui.ThreadData threadData = ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().get(i);
                int n5 = n2 + 69 + i * 10;
                this.drawProgressBar(guiGraphics, n + 10, n5, threadData.progress(), threadData.outputsReady());
                guiGraphics.drawString(font, (Component)Component.translatable((String)"gui.aeind.cross_thread.thread", (Object[])new Object[]{i + 1, Math.round(threadData.progress() * 100.0f), threadData.parallel()}), n + 56, n5 - 1, 0xFFFFFF, false);
            }
            if (((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size() > n4) {
                guiGraphics.drawString(font, (Component)Component.translatable((String)"gui.aeind.cross_thread.more", (Object[])new Object[]{((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size() - n4}), n + 10, n2 + 89, 0xAAAAAA, false);
            }
        }

        private void drawProgressBar(GuiGraphics guiGraphics, int n, int n2, float f, boolean bl) {
            int n3 = Math.max(0, Math.min(100, Math.round(f * 100.0f)));
            guiGraphics.fill(n, n2, n + 42, n2 + 6, -14671840);
            int n4 = Math.round((float)(40 * n3) / 100.0f);
            if (n4 > 0) {
                int n5 = bl ? -7697782 : -11155480;
                guiGraphics.fill(n + 1, n2 + 1, n + 1 + n4, n2 + 6 - 1, n5);
            }
        }

        public boolean renderTooltip(MachineScreen machineScreen, Font font, GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
            if (((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().isEmpty() || !RenderHelper.isPointWithinRectangle((int)5, (int)16, (int)166, (int)83, (double)(n3 - n), (double)(n4 - n2))) {
                return false;
            }
            int n5 = ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().stream().mapToInt(CrossThreadProgressGui.ThreadData::parallel).sum();
            ArrayList<MutableComponent> arrayList = new ArrayList<MutableComponent>();
            arrayList.add(Component.translatable((String)"gui.aeind.cross_thread.title", (Object[])new Object[]{((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size(), n5}).withStyle(ChatFormatting.AQUA));
            for (int i = 0; i < ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().size(); ++i) {
                CrossThreadProgressGui.ThreadData threadData = ((CrossThreadProgressGui.Data)CrossThreadProgressGuiClient.this.data).threads().get(i);
                MutableComponent mutableComponent = threadData.outputsReady() ? Component.translatable((String)"gui.aeind.cross_thread.waiting_output") : Component.translatable((String)"gui.aeind.cross_thread.running");
                arrayList.add(Component.translatable((String)"gui.aeind.cross_thread.thread_tooltip", (Object[])new Object[]{i + 1, Math.round(threadData.progress() * 100.0f), threadData.parallel(), mutableComponent}));
            }
            guiGraphics.renderTooltip(font, arrayList, Optional.empty(), n3, n4);
            return true;
        }
    }
}

