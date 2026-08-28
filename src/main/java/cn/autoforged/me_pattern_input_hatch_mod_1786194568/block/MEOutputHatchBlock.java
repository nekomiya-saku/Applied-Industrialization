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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEOutputHatchBlockEntity(pos, state);
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity blockEntity;
        if (!state.is(newState.getBlock()) && (blockEntity = level.getBlockEntity(pos)) instanceof MEOutputHatchBlockEntity) {
            MEOutputHatchBlockEntity blockEntity2 = (MEOutputHatchBlockEntity)blockEntity;
            NonNullList drops = NonNullList.create();
            blockEntity2.addOutputDrops((List<ItemStack>)drops);
            Containers.dropContents((Level)level, (BlockPos)pos, (NonNullList)drops);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return MEOutputHatchBlock.createTickerHelper(type, (BlockEntityType)((BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get()), MEOutputHatchBlockEntity::serverTick);
    }
}

