/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 */
package aeind.block;

import aeind.block.AdvancedPatternInputHatchBlock;
import aeind.block.CrossThreadParallelWarehouseBlock;
import aeind.block.ExtendedPatternInputHatchBlock;
import aeind.block.MEOutputHatchBlock;
import aeind.block.ThreadWarehouseBlock;
import aeind.item.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"aeind");
    public static final DeferredBlock<AdvancedPatternInputHatchBlock> ADVANCED_PATTERN_INPUT_HATCH = ModBlocks.registerBlock("advanced_pattern_input_hatch", () -> new AdvancedPatternInputHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(blockState -> 0)));
    public static final DeferredBlock<MEOutputHatchBlock> ME_OUTPUT_HATCH = ModBlocks.registerBlock("me_output_hatch", () -> new MEOutputHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(blockState -> 0)));
    public static final DeferredBlock<ThreadWarehouseBlock> THREAD_WAREHOUSE = ModBlocks.registerBlock("thread_warehouse", () -> new ThreadWarehouseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<CrossThreadParallelWarehouseBlock> CROSS_THREAD_PARALLEL_WAREHOUSE = ModBlocks.registerBlock("cross_thread_parallel_warehouse", () -> new CrossThreadParallelWarehouseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredBlock<ExtendedPatternInputHatchBlock> ADVANCED_EXTENDED_PATTERN_INPUT_HATCH = ModList.get().isLoaded("extendedae") ? ModBlocks.registerBlock("advanced_extended_pattern_input_hatch", () -> new ExtendedPatternInputHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(blockState -> 0))) : null;

    private static <T extends Block> DeferredBlock<T> registerBlock(String string, Supplier<? extends T> supplier) {
        DeferredBlock deferredBlock = BLOCKS.register(string, supplier);
        ModItems.ITEMS.registerSimpleBlockItem((Holder)deferredBlock);
        return deferredBlock;
    }

    public static void register(IEventBus iEventBus) {
        BLOCKS.register(iEventBus);
    }
}

