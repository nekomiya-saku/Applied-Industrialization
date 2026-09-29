/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.networking.security.IActionSource
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.api.stacks.KeyCounter
 *  appeng.api.storage.MEStorage
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  net.neoforged.neoforge.items.IItemHandler
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;

public class MEOutputHatchStorage
implements MEStorage {
    private final MEOutputHatchBlockEntity host;

    public MEOutputHatchStorage(MEOutputHatchBlockEntity mEOutputHatchBlockEntity) {
        this.host = mEOutputHatchBlockEntity;
    }

    public long insert(AEKey aEKey, long l, Actionable actionable, IActionSource iActionSource) {
        return 0L;
    }

    public long extract(AEKey aEKey, long l, Actionable actionable, IActionSource iActionSource) {
        this.host.collectMachineOutputs();
        return this.host.getOutputBuffer().extract(aEKey, l, actionable);
    }

    public void getAvailableStacks(KeyCounter keyCounter) {
        this.host.collectMachineOutputs();
        this.host.getOutputBuffer().getAvailableStacks(keyCounter);
    }

    public Component getDescription() {
        return Component.literal((String)"ME Output Hatch Buffer");
    }
}
