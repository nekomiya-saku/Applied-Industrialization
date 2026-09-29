/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.client.gui.style.ScreenStyle
 *  appeng.client.gui.style.StyleManager
 *  aztech.modern_industrialization.client.machines.GuiComponentsClient
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 *  net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
 */
package aeind.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import aztech.modern_industrialization.client.machines.GuiComponentsClient;
import aeind.client.CrossThreadProgressGuiClient;
import aeind.cross_thread.CrossThreadProgressGui;
import aeind.gui.ExtendedPatternProviderMenu;
import aeind.gui.ExtendedPatternProviderScreen;
import aeind.gui.HatchPatternProviderMenu;
import aeind.gui.HatchPatternProviderScreen;
import aeind.gui.ModMenuTypes;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid="aeind", bus=EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class ModClientSetup {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent registerMenuScreensEvent) {
        registerMenuScreensEvent.register((MenuType)ModMenuTypes.ADVANCED_PATTERN_INPUT_HATCH.get(), (abstractContainerMenu, inventory, component) -> {
            ScreenStyle screenStyle = StyleManager.loadStyleDoc((String)"/screens/pattern_provider.json");
            return new HatchPatternProviderScreen((HatchPatternProviderMenu)abstractContainerMenu, inventory, component, screenStyle);
        });
        if (ModList.get().isLoaded("extendedae") && ModMenuTypes.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            registerMenuScreensEvent.register((MenuType)ModMenuTypes.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (abstractContainerMenu, inventory, component) -> {
                ScreenStyle screenStyle = StyleManager.loadStyleDoc((String)"/screens/ex_pattern_provider.json");
                return new ExtendedPatternProviderScreen((ExtendedPatternProviderMenu)abstractContainerMenu, inventory, component, screenStyle);
            });
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent fMLClientSetupEvent) {
        GuiComponentsClient.register(CrossThreadProgressGui.TYPE, CrossThreadProgressGuiClient::new);
    }
}

