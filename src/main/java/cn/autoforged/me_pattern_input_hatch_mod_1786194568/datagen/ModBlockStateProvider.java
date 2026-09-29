/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.client.model.generators.BlockStateProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider
extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, "aeind", existingFileHelper);
    }

    protected void registerStatesAndModels() {
        this.simpleBlockWithItem((Block)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), this.cubeAll((Block)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()));
        if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            this.simpleBlockWithItem((Block)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), this.cubeAll((Block)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()));
        }
        this.simpleBlockWithItem((Block)ModBlocks.ME_OUTPUT_HATCH.get(), this.cubeAll((Block)ModBlocks.ME_OUTPUT_HATCH.get()));
    }
}

