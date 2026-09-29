package aeind.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExtendedPatternProviderMenu extends PatternProviderMenu {
   private final ExtendedPatternInputHatchBlockEntity hatch;

   public ExtendedPatternProviderMenu(MenuType<? extends PatternProviderMenu> var1, int var2, Inventory var3, PatternProviderLogicHost var4) {
      super(var1, var2, var3, var4);
      this.hatch = (ExtendedPatternInputHatchBlockEntity)var4;
   }

   public void returnMaterial() {
      if (this.hatch != null) {
         this.hatch.returnAllBufferToNetwork();
      }
   }
}
