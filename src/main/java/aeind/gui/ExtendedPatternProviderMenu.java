package aeind.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import aeind.isolation.CatalystStorageHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExtendedPatternProviderMenu extends PatternProviderMenu {
   private final ExtendedPatternInputHatchBlockEntity hatch;

   public ExtendedPatternProviderMenu(MenuType<? extends PatternProviderMenu> var1, int var2, Inventory var3, PatternProviderLogicHost var4) {
      super(var1, var2, var3, var4);
      this.hatch = (ExtendedPatternInputHatchBlockEntity)var4;
      ConfigMenuInventory catalyst = ((CatalystStorageHost)var4).aeind$catalystStorage().createMenuWrapper();
      for (int slot = 0; slot < catalyst.size(); slot++) {
         this.addSlot(new AppEngSlot(catalyst, slot), ModMenuTypes.CATALYST);
      }
   }

   public void returnMaterial() {
      if (this.hatch != null) {
         this.hatch.returnAllBufferToNetwork();
      }
   }
}
