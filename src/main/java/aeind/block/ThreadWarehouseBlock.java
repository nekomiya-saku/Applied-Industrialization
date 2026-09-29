package aeind.block;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aeind.blockentity.ThreadWarehouseBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ThreadWarehouseBlock extends BaseEntityBlock {
   public static final MapCodec<ThreadWarehouseBlock> CODEC = simpleCodec(ThreadWarehouseBlock::new);

   public ThreadWarehouseBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   @Override
   public MapCodec<? extends ThreadWarehouseBlock> codec() {
      return CODEC;
   }

   @Override
   public RenderShape getRenderShape(BlockState var1) {
      return RenderShape.MODEL;
   }

   @Override
   public void appendHoverText(ItemStack var1, Item.TooltipContext var2, List<Component> var3, TooltipFlag var4) {
      var3.add(Component.translatable("tooltip.aeind.thread_warehouse").withStyle(ChatFormatting.AQUA));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new ThreadWarehouseBlockEntity(var1, var2);
   }

   @Override
   protected void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      if (!var1.is(var4.getBlock()) && var2.getBlockEntity(var3) instanceof ThreadWarehouseBlockEntity var6) {
         for (ConfigurableItemStack var8 : var6.getInventory().getItemStacks()) {
            if (!var8.isEmpty()) {
               Containers.dropItemStack(var2, var3.getX(), var3.getY(), var3.getZ(), var8.toStack());
            }
         }
      }

      super.onRemove(var1, var2, var3, var4, var5);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level var1, BlockState var2, BlockEntityType<T> var3) {
      return var1.isClientSide ? null : (level, pos, state, blockEntity) -> {
         if (blockEntity instanceof ThreadWarehouseBlockEntity warehouse) {
            warehouse.tick();
         }
      };
   }
}
