package aeind.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import aeind.blockentity.PatternInputHatchHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class HatchPatternProviderMenu extends PatternProviderMenu {
   private final PatternInputHatchHost hatch;
   private final PatternProviderLogicHost providerHost;

   public HatchPatternProviderMenu(MenuType<? extends PatternProviderMenu> var1, int var2, Inventory var3, PatternProviderLogicHost var4) {
      super(var1, var2, var3, var4);
      this.providerHost = var4;
      this.hatch = (PatternInputHatchHost)var4;
   }

   public void returnMaterial() {
      if (this.hatch != null) {
         this.hatch.returnAllBufferToNetwork();
      }
   }

   public Component getProviderTitle() {
      return Component.translatable("block.aeind.advanced_pattern_input_hatch");
   }
}
