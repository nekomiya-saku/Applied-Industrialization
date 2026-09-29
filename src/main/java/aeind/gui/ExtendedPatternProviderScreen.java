package aeind.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import aeind.network.ServerboundReturnMaterialPayload;
import java.util.Objects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedPatternProviderScreen extends PatternProviderScreen<ExtendedPatternProviderMenu> {
   private static final int RETURN_LABEL_LEFT = 8;
   private static final int RETURN_LABEL_TOP = 116;
   private static final int BUTTON_WIDTH = 9;
   private static final int BUTTON_HEIGHT = 9;
   private static final int BUTTON_GAP = 4;
   private Button returnMaterialButton;

   public ExtendedPatternProviderScreen(ExtendedPatternProviderMenu var1, Inventory var2, Component var3, ScreenStyle var4) {
      super(var1, var2, var3, var4);
      this.setTextContent("dialog_title", Component.translatable("block.aeind.advanced_extended_pattern_input_hatch"));
   }

   @Override
   protected void init() {
      super.init();
      this.returnMaterialButton = Button.builder(
            Component.translatable("button.aeind.return_material.label"), var0 -> PacketDistributor.sendToServer(new ServerboundReturnMaterialPayload())
         )
         .bounds(0, 0, 9, 9)
         .build();
      this.returnMaterialButton.setTooltip(Tooltip.create(Component.translatable("button.aeind.return_material.tooltip")));
      MutableComponent var1 = Component.translatable("gui.ae2.ReturnInventory");
      int var2 = this.leftPos + 8 + this.font.width(var1);
      Objects.requireNonNull(this.font);
      int var3 = this.topPos + 116 + 0;
      this.returnMaterialButton.setX(var2 + 4);
      this.returnMaterialButton.setY(var3);
      this.addRenderableWidget(this.returnMaterialButton);
   }
}
