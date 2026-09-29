/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.IntrinsicHolderTagsProvider$IntrinsicTagAppender
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.common.data.BlockTagsProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 *  org.jetbrains.annotations.Nullable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModBlockTagsProvider
extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, "aeind", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider provider) {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender intrinsicTagAppender = this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add((Object)((Block)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get())).add((Object)((Block)ModBlocks.ME_OUTPUT_HATCH.get()));
        if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            intrinsicTagAppender.add((Object)((Block)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()));
        }
    }
}

