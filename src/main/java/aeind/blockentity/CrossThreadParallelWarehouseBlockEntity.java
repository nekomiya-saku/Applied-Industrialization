package aeind.blockentity;

import aeind.isolation.CrossThreadParallelHatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrossThreadParallelWarehouseBlockEntity extends ThreadWarehouseBlockEntity implements CrossThreadParallelHatch {
   public CrossThreadParallelWarehouseBlockEntity(BlockPos var1, BlockState var2) {
      super(var1, var2, ModBlockEntities.CROSS_THREAD_PARALLEL_WAREHOUSE.get(), "cross_thread_parallel_warehouse");
   }
}
