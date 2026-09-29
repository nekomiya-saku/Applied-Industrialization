/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Containers
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.block;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MEOutputHatchBlock
extends BaseEntityBlock {
    public static final MapCodec<MEOutputHatchBlock> CODEC = MEOutputHatchBlock.simpleCodec(MEOutputHatchBlock::new);

    public MEOutputHatchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public MapCodec<MEOutputHatchBlock> codec() {
        return CODEC;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MEOutputHatchBlockEntity(blockPos, blockState);
    }

    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        BlockEntity blockEntity;
        if (!blockState.is(blockState2.getBlock()) && (blockEntity = level.getBlockEntity(blockPos)) instanceof MEOutputHatchBlockEntity) {
            MEOutputHatchBlockEntity mEOutputHatchBlockEntity = (MEOutputHatchBlockEntity)blockEntity;
            NonNullList nonNullList = NonNullList.create();
            mEOutputHatchBlockEntity.addOutputDrops((List<ItemStack>)nonNullList);
            Containers.dropContents((Level)level, (BlockPos)blockPos, (NonNullList)nonNullList);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return MEOutputHatchBlock.createTickerHelper(blockEntityType, (BlockEntityType)((BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get()), MEOutputHatchBlockEntity::serverTick);
    }
}

