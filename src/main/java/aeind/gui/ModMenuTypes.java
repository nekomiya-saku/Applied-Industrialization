package aeind.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
   public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, "aeind");
   public static final DeferredHolder<MenuType<?>, MenuType<HatchPatternProviderMenu>> ADVANCED_PATTERN_INPUT_HATCH = MENU_TYPES.register(
      "advanced_pattern_input_hatch",
      () -> MenuTypeBuilder.create(HatchPatternProviderMenu::new, PatternProviderLogicHost.class)
         .buildUnregistered(ResourceLocation.fromNamespaceAndPath("aeind", "advanced_pattern_input_hatch"))
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ExtendedPatternProviderMenu>> ADVANCED_EXTENDED_PATTERN_INPUT_HATCH = ModList.get()
         .isLoaded("extendedae")
      ? MENU_TYPES.register(
         "advanced_extended_pattern_input_hatch",
         () -> MenuTypeBuilder.create(ExtendedPatternProviderMenu::new, PatternProviderLogicHost.class)
            .buildUnregistered(ResourceLocation.fromNamespaceAndPath("aeind", "advanced_extended_pattern_input_hatch"))
      )
      : null;

   public static void register(IEventBus var0) {
      MENU_TYPES.register(var0);
   }
}
