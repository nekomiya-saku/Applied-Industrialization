package aeind.block;

import aeind.blockentity.CrossThreadParallelWarehouseBlockEntity;
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

public class CrossThreadParallelWarehouseBlock extends ThreadWarehouseBlock {
   public static final MapCodec<CrossThreadParallelWarehouseBlock> CODEC = simpleCodec(CrossThreadParallelWarehouseBlock::new);

   public CrossThreadParallelWarehouseBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   @Override
   public void appendHoverText(ItemStack var1, Item.TooltipContext var2, List<Component> var3, TooltipFlag var4) {
      var3.add(Component.translatable("tooltip.aeind.cross_thread_parallel_warehouse").withStyle(ChatFormatting.AQUA));
   }

   @Override
   public MapCodec<CrossThreadParallelWarehouseBlock> codec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new CrossThreadParallelWarehouseBlockEntity(var1, var2);
   }
}
