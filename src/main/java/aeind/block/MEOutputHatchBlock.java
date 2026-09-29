package aeind.block;

import aeind.blockentity.MEOutputHatchBlockEntity;
import aeind.blockentity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
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

public class MEOutputHatchBlock extends BaseEntityBlock {
   public static final MapCodec<MEOutputHatchBlock> CODEC = simpleCodec(MEOutputHatchBlock::new);

   public MEOutputHatchBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   @Override
   public MapCodec<MEOutputHatchBlock> codec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new MEOutputHatchBlockEntity(var1, var2);
   }

   @Override
   protected RenderShape getRenderShape(BlockState var1) {
      return RenderShape.MODEL;
   }

   @Override
   protected void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      BlockEntity var6;
      if (!var1.is(var4.getBlock()) && (var6 = var2.getBlockEntity(var3)) instanceof MEOutputHatchBlockEntity) {
         MEOutputHatchBlockEntity var7 = (MEOutputHatchBlockEntity)var6;
         NonNullList<ItemStack> var8 = NonNullList.create();
         var7.addOutputDrops(var8);
         Containers.dropContents(var2, var3, var8);
      }

      super.onRemove(var1, var2, var3, var4, var5);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level var1, BlockState var2, BlockEntityType<T> var3) {
      return var1.isClientSide ? null : createTickerHelper(var3, ModBlockEntities.ME_OUTPUT_HATCH.get(), MEOutputHatchBlockEntity::serverTick);
   }
}
