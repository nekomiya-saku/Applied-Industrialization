/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.block;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEOutputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEPatternInputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.item.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"aeind");
    public static final DeferredBlock<MEPatternInputHatchBlock> ME_PATTERN_INPUT_HATCH = ModBlocks.registerBlock("me_pattern_input_hatch", () -> new MEPatternInputHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> 0)));
    public static final DeferredBlock<MEOutputHatchBlock> ME_OUTPUT_HATCH = ModBlocks.registerBlock("me_output_hatch", () -> new MEOutputHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> 0)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<? extends T> blockSupplier) {
        DeferredBlock deferredBlock = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.registerSimpleBlockItem((Holder)deferredBlock);
        return deferredBlock;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

