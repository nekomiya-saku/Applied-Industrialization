package aeind.mixin;

import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiblockMachineBER.class)
public abstract class MultiblockMachineBERMixin {
   @Inject(method = "getHatchType", at = @At("HEAD"), cancellable = true)
   private static void aeind$recognizeCustomHatch(ItemStack stack, CallbackInfoReturnable<HatchType> cir) {
      if (!(stack.getItem() instanceof BlockItem blockItem)) {
         return;
      }

      var block = blockItem.getBlock();
      if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("aeind") || !(block instanceof EntityBlock entityBlock)) {
         return;
      }

      BlockEntity blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
      if (blockEntity instanceof HatchBlockEntity hatch) {
         cir.setReturnValue(hatch.getHatchType());
      }
   }
}
