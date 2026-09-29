/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.components.OverdriveComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
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

@Mixin(value={OverdriveComponent.class})
public abstract class OverdriveComponentMixin
implements OverdriveComponentAccess {
    @Shadow
    private ItemStack overdriveModule;

    @Override
    public void aeind$clear() {
        this.overdriveModule = ItemStack.EMPTY;
    }

    @Inject(method={"onUse"}, at={@At(value="HEAD")}, cancellable=true)
    private void aeind$blockUse(MachineBlockEntity machineBlockEntity, Player player, InteractionHand interactionHand, CallbackInfoReturnable<ItemInteractionResult> callbackInfoReturnable) {
        if (OverdriveBlocker.isBlocked(machineBlockEntity)) {
            callbackInfoReturnable.setReturnValue((Object)ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }
    }

    @Inject(method={"setStackServer"}, at={@At(value="HEAD")}, cancellable=true)
    private void aeind$blockSlotWrite(MachineBlockEntity machineBlockEntity, ItemStack itemStack, CallbackInfo callbackInfo) {
        if (OverdriveBlocker.isBlocked(machineBlockEntity)) {
            this.aeind$clear();
            machineBlockEntity.setChanged();
            callbackInfo.cancel();
        }
    }
}

