/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.item;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"aeind");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ME_PATTERN_INPUT_HATCH_TAB = CREATIVE_TABS.register("me_pattern_input_hatch_tab", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.aeind")).icon(() -> new ItemStack((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get())).withTabsBefore(new ResourceKey[]{CreativeModeTabs.FUNCTIONAL_BLOCKS}).displayItems((params, output) -> {
        output.accept((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get());
        output.accept((ItemLike)ModBlocks.ME_OUTPUT_HATCH.get());
    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}

