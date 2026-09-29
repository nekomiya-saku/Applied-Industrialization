/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package aeind.blockentity;

import aeind.blockentity.ModBlockEntities;
import aeind.blockentity.ThreadWarehouseBlockEntity;
import aeind.isolation.CrossThreadParallelHatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrossThreadParallelWarehouseBlockEntity
extends ThreadWarehouseBlockEntity
implements CrossThreadParallelHatch {
    public CrossThreadParallelWarehouseBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState, (BlockEntityType)ModBlockEntities.CROSS_THREAD_PARALLEL_WAREHOUSE.get(), "cross_thread_parallel_warehouse");
    }
}

