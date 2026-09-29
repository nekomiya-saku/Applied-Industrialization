/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
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

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ThreadWarehouseBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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

public class ThreadWarehouseBlock
extends BaseEntityBlock {
    public static final MapCodec<ThreadWarehouseBlock> CODEC = ThreadWarehouseBlock.simpleCodec(ThreadWarehouseBlock::new);

    public ThreadWarehouseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public MapCodec<? extends ThreadWarehouseBlock> codec() {
        return CODEC;
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ThreadWarehouseBlockEntity(blockPos, blockState);
    }

    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        Object object;
        if (!blockState.is(blockState2.getBlock()) && (object = level.getBlockEntity(blockPos)) instanceof ThreadWarehouseBlockEntity) {
            ThreadWarehouseBlockEntity threadWarehouseBlockEntity = (ThreadWarehouseBlockEntity)object;
            for (ConfigurableItemStack configurableItemStack : threadWarehouseBlockEntity.getInventory().getItemStacks()) {
                if (configurableItemStack.isEmpty()) continue;
                Containers.dropItemStack((Level)level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (ItemStack)configurableItemStack.toStack());
            }
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return null;
    }
}

