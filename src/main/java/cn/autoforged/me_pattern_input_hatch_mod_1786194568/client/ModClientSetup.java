/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.HatchPatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.HatchPatternProviderScreen;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.ModMenuTypes;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid="aeind", bus=EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class ModClientSetup {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register((MenuType)ModMenuTypes.ME_PATTERN_INPUT_HATCH.get(), (menu, playerInventory, title) -> {
            ScreenStyle style = StyleManager.loadStyleDoc((String)"/screens/pattern_provider.json");
            return new HatchPatternProviderScreen((HatchPatternProviderMenu)menu, playerInventory, title, style);
        });
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent fMLClientSetupEvent) {
    }
}

