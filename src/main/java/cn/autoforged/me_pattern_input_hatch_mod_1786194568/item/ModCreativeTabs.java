/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
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
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> APPLIED_INDUSTRIALIZATION_TAB = CREATIVE_TABS.register("applied_industrialization_tab", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.aeind")).icon(() -> new ItemStack((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get())).withTabsBefore(new ResourceKey[]{CreativeModeTabs.FUNCTIONAL_BLOCKS}).displayItems((itemDisplayParameters, output) -> {
        output.accept((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
        output.accept((ItemLike)ModBlocks.ME_OUTPUT_HATCH.get());
        output.accept((ItemLike)ModBlocks.THREAD_WAREHOUSE.get());
        output.accept((ItemLike)ModBlocks.CROSS_THREAD_PARALLEL_WAREHOUSE.get());
        if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            output.accept((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
        }
    }).build());

    public static void register(IEventBus iEventBus) {
        CREATIVE_TABS.register(iEventBus);
    }
}

