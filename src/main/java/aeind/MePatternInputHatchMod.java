package aeind;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import aeind.block.ModBlocks;
import aeind.blockentity.ModBlockEntities;
import aeind.gui.ModMenuTypes;
import aeind.item.ModCreativeTabs;
import aeind.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("aeind")
public class MePatternInputHatchMod {
   public static final String MODID = "aeind";
   private static boolean upgradesRegistered = false;

   public MePatternInputHatchMod(IEventBus var1, ModContainer var2) {
      ModBlocks.register(var1);
      ModItems.ITEMS.register(var1);
      ModBlockEntities.register(var1);
      ModMenuTypes.register(var1);
      ModCreativeTabs.register(var1);
      var1.addListener(MePatternInputHatchMod::registerUpgrades);
   }

   private static void registerUpgrades(RegisterEvent var0) {
      if (!upgradesRegistered && var0.getRegistryKey().equals(Registries.ITEM)) {
         upgradesRegistered = true;
         Upgrades.add(AEItems.REDSTONE_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 1);
         Upgrades.add(AEItems.CAPACITY_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 2);
         Upgrades.add(AEItems.CRAFTING_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 1);
         Upgrades.add(AEItems.REDSTONE_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 1);
         Upgrades.add(AEItems.CAPACITY_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 2);
         Upgrades.add(AEItems.CRAFTING_CARD, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), 1);
         if (ModList.get().isLoaded("extendedae") && ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            Upgrades.add(AEItems.REDSTONE_CARD, ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), 1);
            Upgrades.add(AEItems.CAPACITY_CARD, ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), 2);
            Upgrades.add(AEItems.CRAFTING_CARD, ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), 1);
         }
      }
   }
}
