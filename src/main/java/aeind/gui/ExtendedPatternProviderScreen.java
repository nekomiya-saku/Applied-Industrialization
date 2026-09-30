package aeind.gui;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import aeind.network.ServerboundReturnMaterialPayload;
import java.util.Objects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import appeng.menu.SlotSemantics;
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
      SlotPosition catalystPosition = new SlotPosition();
      catalystPosition.setLeft(8);
      catalystPosition.setTop(127);
      catalystPosition.setGrid(SlotGridLayout.HORIZONTAL);
      var4.getSlots().put(ModMenuTypes.CATALYST.id(), catalystPosition);
      this.setTextContent("dialog_title", Component.translatable("block.aeind.advanced_extended_pattern_input_hatch"));
   }

   @Override
   protected void init() {
      super.init();
      this.setSlotsHidden(SlotSemantics.STORAGE, true);
      this.setTextContent("interface_stored_items", Component.translatable("gui.aeind.catalysts"));
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
