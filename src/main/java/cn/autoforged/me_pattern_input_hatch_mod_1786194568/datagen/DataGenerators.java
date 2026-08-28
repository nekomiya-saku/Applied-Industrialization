/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.data.DataProvider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.loot.LootTableProvider
 *  net.minecraft.data.loot.LootTableProvider$SubProviderEntry
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 *  net.neoforged.neoforge.data.event.GatherDataEvent
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModBlockLootTableProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModBlockStateProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModBlockTagsProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModItemModelProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModItemTagsProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen.ModRecipeProvider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid="aeind")
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture lookupProvider = event.getLookupProvider();
        generator.addProvider(event.includeClient(), (DataProvider)new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), (DataProvider)new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), (DataProvider)new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), (DataProvider)blockTags);
        generator.addProvider(event.includeServer(), (DataProvider)new ModItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), (DataProvider)new ModRecipeProvider(packOutput, lookupProvider));
    }
}

