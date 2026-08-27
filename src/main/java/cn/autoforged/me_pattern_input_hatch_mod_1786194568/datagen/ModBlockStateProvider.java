/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider
extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, "aeind", existingFileHelper);
    }

    protected void registerStatesAndModels() {
        this.simpleBlockWithItem((Block)ModBlocks.ME_PATTERN_INPUT_HATCH.get(), this.cubeAll((Block)ModBlocks.ME_PATTERN_INPUT_HATCH.get()));
        this.simpleBlockWithItem((Block)ModBlocks.ME_OUTPUT_HATCH.get(), this.cubeAll((Block)ModBlocks.ME_OUTPUT_HATCH.get()));
    }
}

