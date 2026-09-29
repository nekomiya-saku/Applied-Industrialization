package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OverdriveComponent;
import aeind.isolation.OverdriveBlocker;
import aeind.isolation.OverdriveComponentAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverdriveComponent.class)
public abstract class OverdriveComponentMixin implements OverdriveComponentAccess {
   @Shadow
   private ItemStack overdriveModule;

   @Override
   public void aeind$clear() {
      this.overdriveModule = ItemStack.EMPTY;
   }

   @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
   private void aeind$blockUse(MachineBlockEntity var1, Player var2, InteractionHand var3, CallbackInfoReturnable<ItemInteractionResult> var4) {
      if (OverdriveBlocker.isBlocked(var1)) {
         var4.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
      }
   }

   @Inject(method = "setStackServer", at = @At("HEAD"), cancellable = true)
   private void aeind$blockSlotWrite(MachineBlockEntity var1, ItemStack var2, CallbackInfo var3) {
      if (OverdriveBlocker.isBlocked(var1)) {
         this.aeind$clear();
         var1.setChanged();
         var3.cancel();
      }
   }
}
