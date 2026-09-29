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
    public static void gatherData(GatherDataEvent gatherDataEvent) {
        DataGenerator dataGenerator = gatherDataEvent.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();
        CompletableFuture completableFuture = gatherDataEvent.getLookupProvider();
        dataGenerator.addProvider(gatherDataEvent.includeClient(), (DataProvider)new ModBlockStateProvider(packOutput, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeClient(), (DataProvider)new ModItemModelProvider(packOutput, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeServer(), (DataProvider)new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), completableFuture));
        ModBlockTagsProvider modBlockTagsProvider = new ModBlockTagsProvider(packOutput, completableFuture, existingFileHelper);
        dataGenerator.addProvider(gatherDataEvent.includeServer(), (DataProvider)modBlockTagsProvider);
        dataGenerator.addProvider(gatherDataEvent.includeServer(), (DataProvider)new ModItemTagsProvider(packOutput, completableFuture, modBlockTagsProvider.contentsGetter(), existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeServer(), (DataProvider)new ModRecipeProvider(packOutput, completableFuture));
    }
}

