/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.loot.BlockLootSubProvider
 *  net.minecraft.world.flag.FeatureFlags
 *  net.minecraft.world.level.block.Block
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootTableProvider
extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
    }

    protected void generate() {
        this.dropSelf((Block)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
        if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            this.dropSelf((Block)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
        }
        this.dropSelf((Block)ModBlocks.ME_OUTPUT_HATCH.get());
    }

    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(deferredHolder -> (Block)deferredHolder.get()).toList();
    }
}

