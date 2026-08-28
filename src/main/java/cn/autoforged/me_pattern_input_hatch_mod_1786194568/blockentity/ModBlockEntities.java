/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"aeind");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEPatternInputHatchBlockEntity>> ME_PATTERN_INPUT_HATCH = BLOCK_ENTITY_TYPES.register("me_pattern_input_hatch", () -> BlockEntityType.Builder.of(MEPatternInputHatchBlockEntity::new, (Block[])new Block[]{(Block)ModBlocks.ME_PATTERN_INPUT_HATCH.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEOutputHatchBlockEntity>> ME_OUTPUT_HATCH = BLOCK_ENTITY_TYPES.register("me_output_hatch", () -> BlockEntityType.Builder.of(MEOutputHatchBlockEntity::new, (Block[])new Block[]{(Block)ModBlocks.ME_OUTPUT_HATCH.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedPatternInputHatchBlockEntity>> EXTENDED_PATTERN_INPUT_HATCH = ModList.get().isLoaded("extendedae")
            ? BLOCK_ENTITY_TYPES.register("extended_pattern_input_hatch", () -> BlockEntityType.Builder.of(ExtendedPatternInputHatchBlockEntity::new, (Block[])new Block[]{(Block)ModBlocks.EXTENDED_PATTERN_INPUT_HATCH.get()}).build(null))
            : null;

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
