/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.helpers.patternprovider.PatternProviderLogicHost
 *  appeng.menu.implementations.MenuTypeBuilder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.MenuTypeBuilder;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.HatchPatternProviderMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create((ResourceKey)Registries.MENU, (String)"aeind");
    public static final DeferredHolder<MenuType<?>, MenuType<HatchPatternProviderMenu>> ME_PATTERN_INPUT_HATCH = MENU_TYPES.register("me_pattern_input_hatch", () -> MenuTypeBuilder.create(HatchPatternProviderMenu::new, PatternProviderLogicHost.class).buildUnregistered(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"me_pattern_input_hatch")));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedPatternProviderMenu>> EXTENDED_PATTERN_INPUT_HATCH = ModList.get().isLoaded("extendedae")
            ? MENU_TYPES.register("extended_pattern_input_hatch", () -> MenuTypeBuilder.create(ExtendedPatternProviderMenu::new, PatternProviderLogicHost.class).buildUnregistered(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"extended_pattern_input_hatch")))
            : null;

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
