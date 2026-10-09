package aeind.block;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import aeind.blockentity.ModBlockEntities;
import aeind.gui.ModMenuTypes;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ExtendedPatternInputHatchBlock extends BaseEntityBlock {
   public static final MapCodec<ExtendedPatternInputHatchBlock> CODEC = simpleCodec(ExtendedPatternInputHatchBlock::new);

   public ExtendedPatternInputHatchBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   @Override
   public MapCodec<ExtendedPatternInputHatchBlock> codec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new ExtendedPatternInputHatchBlockEntity(var1, var2);
   }

   @Override
   protected RenderShape getRenderShape(BlockState var1) {
      return RenderShape.MODEL;
   }

   /** Keep item-driven interactions (including AE2's quartz cutting knife) on the item path. */
   @Override
   protected ItemInteractionResult useItemOn(ItemStack var1, BlockState var2, Level var3, BlockPos var4,
                                             Player var5, InteractionHand var6, BlockHitResult var7) {
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public void appendHoverText(ItemStack var1, Item.TooltipContext var2, List<Component> var3, TooltipFlag var4) {
      var3.add(Component.translatable("tooltip.aeind.pattern_input_hatch.isolation").withStyle(ChatFormatting.AQUA));
      var3.add(Component.translatable("tooltip.aeind.pattern_input_hatch.catalyst").withStyle(ChatFormatting.GREEN));
   }

   @Override
   public void setPlacedBy(Level var1, BlockPos var2, BlockState var3, @Nullable LivingEntity var4, ItemStack var5) {
      super.setPlacedBy(var1, var2, var3, var4, var5);
      Component var6 = var5.get(DataComponents.CUSTOM_NAME);
      if (var6 != null && var1.getBlockEntity(var2) instanceof ExtendedPatternInputHatchBlockEntity var7) {
         var7.setCustomName(var6);
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (!var2.isClientSide && var4 instanceof ServerPlayer var6 && var2.getBlockEntity(var3) instanceof ExtendedPatternInputHatchBlockEntity) {
         MenuOpener.open(ModMenuTypes.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), var6, MenuLocators.forBlockEntity(var2.getBlockEntity(var3)));
      }

      return InteractionResult.sidedSuccess(var2.isClientSide);
   }

   @Override
   protected void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      BlockEntity var6;
      if (!var1.is(var4.getBlock()) && (var6 = var2.getBlockEntity(var3)) instanceof ExtendedPatternInputHatchBlockEntity) {
         ExtendedPatternInputHatchBlockEntity var7 = (ExtendedPatternInputHatchBlockEntity)var6;
         var7.returnAllBufferToNetwork();
         Containers.dropContents(var2, var3, var7.getDropItems());
      }

      super.onRemove(var1, var2, var3, var4, var5);
   }

   @Override
   public boolean onDestroyedByPlayer(BlockState var1, Level var2, BlockPos var3, Player var4, boolean var5, FluidState var6) {
      ExtendedPatternInputHatchBlockEntity var7;
      BlockEntity var8;
      if (!var2.isClientSide
         && (var8 = var2.getBlockEntity(var3)) instanceof ExtendedPatternInputHatchBlockEntity
         && (var7 = (ExtendedPatternInputHatchBlockEntity)var8).hasStoredMaterials()
         && !var4.isShiftKeyDown()) {
         var4.sendSystemMessage(
            Component.literal("你正在尝试破坏ME输入仓，请保证连接ae网络的同时，ae的存储容量能够容下返回的原料。否则游戏很有可能因为大量掉落物崩溃！检查后若要继续破坏，请在潜行时进行破坏").withStyle(ChatFormatting.RED)
         );
         return false;
      } else {
         return super.onDestroyedByPlayer(var1, var2, var3, var4, var5, var6);
      }
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level var1, BlockState var2, BlockEntityType<T> var3) {
      return var1.isClientSide
         ? null
         : createTickerHelper(var3, ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), ExtendedPatternInputHatchBlockEntity::serverTick);
   }
}
