/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.block;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ThreadWarehouseBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.CrossThreadParallelWarehouseBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrossThreadParallelWarehouseBlock
extends ThreadWarehouseBlock {
    public static final MapCodec<CrossThreadParallelWarehouseBlock> CODEC = CrossThreadParallelWarehouseBlock.simpleCodec(CrossThreadParallelWarehouseBlock::new);

    public CrossThreadParallelWarehouseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        list.add((Component)Component.translatable((String)"tooltip.aeind.cross_thread_parallel_warehouse").withStyle(ChatFormatting.AQUA));
    }

    public MapCodec<CrossThreadParallelWarehouseBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CrossThreadParallelWarehouseBlockEntity(blockPos, blockState);
    }
}

