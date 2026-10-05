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

public final class CrossThreadProgressGuiClient extends GuiComponentClient<Unit, CrossThreadProgressGui.Data> {
   private static final int MAX_VISIBLE_THREADS = 2;
   private static final int BAR_WIDTH = 42;
   private static final int BAR_HEIGHT = 6;

   public CrossThreadProgressGuiClient(Unit var1, CrossThreadProgressGui.Data var2) {
      super(var1, var2);
   }

   @Override
   public ClientComponentRenderer createRenderer(MachineScreen var1) {
      return new CrossThreadProgressGuiClient.Renderer();
   }

   private final class Renderer implements ClientComponentRenderer {
      @Override
      public void renderBackground(GuiGraphics var1, int var2, int var3) {
         if (!CrossThreadProgressGuiClient.this.data.threads().isEmpty()) {
            Font var4 = Minecraft.getInstance().font;
            int var5 = CrossThreadProgressGuiClient.this.data.threads().stream().mapToInt(CrossThreadProgressGui.ThreadData::parallel).sum();
            MutableComponent var6 = Component.translatable("gui.aeind.cross_thread.title", CrossThreadProgressGuiClient.this.data.threads().size(), var5, CrossThreadProgressGuiClient.this.data.euPerTick());
            var1.drawString(var4, var6, var2 + 10, var3 + 58, 16777215, false);
            int var7 = Math.min(2, CrossThreadProgressGuiClient.this.data.threads().size());

            for (int var8 = 0; var8 < var7; var8++) {
               CrossThreadProgressGui.ThreadData var9 = CrossThreadProgressGuiClient.this.data.threads().get(var8);
               int var10 = var3 + 69 + var8 * 10;
               this.drawProgressBar(var1, var2 + 10, var10, var9.progress(), var9.outputsReady());
               var1.drawString(
                  var4,
                  Component.translatable("gui.aeind.cross_thread.thread", var8 + 1, Math.round(var9.progress() * 100.0F), var9.parallel()),
                  var2 + 56,
                  var10 - 1,
                  16777215,
                  false
               );
            }

            if (CrossThreadProgressGuiClient.this.data.threads().size() > var7) {
               var1.drawString(
                  var4,
                  Component.translatable("gui.aeind.cross_thread.more", CrossThreadProgressGuiClient.this.data.threads().size() - var7),
                  var2 + 10,
                  var3 + 89,
                  11184810,
                  false
               );
            }
         }
      }

      private void drawProgressBar(GuiGraphics var1, int var2, int var3, float var4, boolean var5) {
         int var6 = Math.max(0, Math.min(100, Math.round(var4 * 100.0F)));
         var1.fill(var2, var3, var2 + 42, var3 + 6, -14671840);
         int var7 = Math.round(40 * var6 / 100.0F);
         if (var7 > 0) {
            int var8 = var5 ? -7697782 : -11155480;
            var1.fill(var2 + 1, var3 + 1, var2 + 1 + var7, var3 + 6 - 1, var8);
         }
      }

      @Override
      public boolean renderTooltip(MachineScreen var1, Font var2, GuiGraphics var3, int var4, int var5, int var6, int var7) {
         if (!CrossThreadProgressGuiClient.this.data.threads().isEmpty() && RenderHelper.isPointWithinRectangle(5, 16, 166, 83, var6 - var4, var7 - var5)) {
            int var8 = CrossThreadProgressGuiClient.this.data.threads().stream().mapToInt(CrossThreadProgressGui.ThreadData::parallel).sum();
            ArrayList<Component> var9 = new ArrayList<>();
            var9.add(
               Component.translatable("gui.aeind.cross_thread.title", CrossThreadProgressGuiClient.this.data.threads().size(), var8, CrossThreadProgressGuiClient.this.data.euPerTick())
                  .withStyle(ChatFormatting.AQUA)
            );

            for (int var10 = 0; var10 < CrossThreadProgressGuiClient.this.data.threads().size(); var10++) {
               CrossThreadProgressGui.ThreadData var11 = CrossThreadProgressGuiClient.this.data.threads().get(var10);
               MutableComponent var12 = var11.outputsReady()
                  ? Component.translatable("gui.aeind.cross_thread.waiting_output")
                  : Component.translatable("gui.aeind.cross_thread.running");
               var9.add(
                  Component.translatable("gui.aeind.cross_thread.thread_tooltip", var10 + 1, Math.round(var11.progress() * 100.0F), var11.parallel(), var12)
               );
            }

            var3.renderTooltip(var2, var9, Optional.empty(), var6, var7);
            return true;
         } else {
            return false;
         }
      }
   }
}
