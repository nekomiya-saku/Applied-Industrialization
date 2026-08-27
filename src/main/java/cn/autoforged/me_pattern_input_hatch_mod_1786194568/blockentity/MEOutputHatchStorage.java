/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class MEOutputHatchStorage
implements MEStorage {
    private final MEOutputHatchBlockEntity host;

    public MEOutputHatchStorage(MEOutputHatchBlockEntity host) {
        this.host = host;
    }

    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0L;
    }

    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0L) {
            return 0L;
        }
        if (what instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)what;
            long extracted = 0L;
            boolean simulate = mode != Actionable.MODULATE;
            IItemHandler handler = this.host.getBufferInventory();
            for (int slot = 0; slot < handler.getSlots() && extracted < amount; ++slot) {
                ItemStack current = handler.getStackInSlot(slot);
                if (current.isEmpty() || !itemKey.matches(current)) continue;
                int take = (int)Math.min(amount - extracted, (long)current.getCount());
                ItemStack got = handler.extractItem(slot, take, simulate);
                extracted += (long)got.getCount();
            }
            return extracted;
        }
        if (what instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)what;
            long capped = Math.min(amount, Integer.MAX_VALUE);
            FluidStack drained = this.host.getFluidHandler().drain(fluidKey.toStack((int)capped), mode == Actionable.MODULATE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            return drained.getAmount();
        }
        return 0L;
    }

    public void getAvailableStacks(KeyCounter out) {
        IItemHandler handler = this.host.getBufferInventory();
        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            out.add((AEKey)AEItemKey.of((ItemStack)stack), (long)stack.getCount());
        }
        IFluidHandler fluidHandler = this.host.getFluidHandler();
        for (int i = 0; i < fluidHandler.getTanks(); ++i) {
            FluidStack fluid = fluidHandler.getFluidInTank(i);
            if (fluid.isEmpty()) continue;
            out.add((AEKey)AEFluidKey.of((FluidStack)fluid), (long)fluid.getAmount());
        }
    }

    public Component getDescription() {
        return Component.literal((String)"ME Output Hatch Buffer");
    }
}

