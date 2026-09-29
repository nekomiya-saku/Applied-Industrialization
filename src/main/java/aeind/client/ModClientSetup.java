package aeind.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import aztech.modern_industrialization.client.machines.GuiComponentsClient;
import aeind.cross_thread.CrossThreadProgressGui;
import aeind.gui.ExtendedPatternProviderMenu;
import aeind.gui.ExtendedPatternProviderScreen;
import aeind.gui.HatchPatternProviderMenu;
import aeind.gui.HatchPatternProviderScreen;
import aeind.gui.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "aeind", value = Dist.CLIENT)
public class ModClientSetup {
   @SubscribeEvent
   public static void onRegisterMenuScreens(RegisterMenuScreensEvent var0) {
      var0.register(ModMenuTypes.ADVANCED_PATTERN_INPUT_HATCH.get(), ModClientSetup::createHatchScreen);
      if (ModList.get().isLoaded("extendedae") && ModMenuTypes.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
         var0.register(ModMenuTypes.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), ModClientSetup::createExtendedHatchScreen);
      }
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent var0) {
      GuiComponentsClient.register(CrossThreadProgressGui.TYPE, CrossThreadProgressGuiClient::new);
   }

   private static HatchPatternProviderScreen createHatchScreen(HatchPatternProviderMenu menu, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.network.chat.Component title) {
      ScreenStyle style = StyleManager.loadStyleDoc("/screens/pattern_provider.json");
      return new HatchPatternProviderScreen(menu, inventory, title, style);
   }

   private static ExtendedPatternProviderScreen createExtendedHatchScreen(ExtendedPatternProviderMenu menu, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.network.chat.Component title) {
      ScreenStyle style = StyleManager.loadStyleDoc("/screens/ex_pattern_provider.json");
      return new ExtendedPatternProviderScreen(menu, inventory, title, style);
   }
}
