package aeind.blockentity;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;

/** Fluid counterpart of {@link VirtualOutputItemStack}. */
public final class VirtualOutputFluidStack extends ConfigurableFluidStack {
    public static final long CAPACITY = Long.MAX_VALUE;
    private VirtualOutputSink sink;
    private FluidVariant pendingKey;
    private long pendingAmount;

    public VirtualOutputFluidStack() {
        this(null);
    }

    private VirtualOutputFluidStack(VirtualOutputSink sink) {
        super(CAPACITY);
        this.sink = sink;
        this.pipesExtract = false;
    }

    public VirtualOutputFluidStack detached() {
        return new VirtualOutputFluidStack(null);
    }

    public void bind(VirtualOutputSink sink) {
        this.sink = sink;
    }

    @Override
    public boolean isResourceBlank() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isMachineLocked() {
        return true;
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        return 0L;
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    @Override
    protected long getRemainingCapacityFor(FluidVariant key) {
        return key == null || key.isBlank() ? 0L : CAPACITY;
    }

    @Override
    public long getTotalCapacityFor(Fluid fluid) {
        return CAPACITY;
    }

    @Override
    public long getRemainingSpace() {
        return CAPACITY;
    }

    @Override
    public void setAmount(long amount) {
        pendingAmount = Math.max(0L, amount);
        if (pendingKey != null) {
            flushPending();
        }
    }

    @Override
    public void setKey(FluidVariant key) {
        pendingKey = key == null || key.isBlank() ? null : key;
        if (pendingKey != null && pendingAmount > 0L) {
            flushPending();
        }
    }

    @Override
    public void increment(long amount) {
        if (amount <= 0L || pendingKey == null) {
            return;
        }
        pendingAmount = saturatedAdd(pendingAmount, amount);
        flushPending();
    }

    @Override
    public void decrement(long amount) {}

    @Override
    public void empty() {
        pendingKey = null;
        pendingAmount = 0L;
    }

    @Override
    public void enableMachineLock(Fluid fluid) {}

    @Override
    public void disableMachineLock() {
        empty();
    }

    private void flushPending() {
        if (sink != null && pendingKey != null && pendingAmount > 0L) {
            sink.acceptFluidOutput(pendingKey, pendingAmount);
        }
        pendingKey = null;
        pendingAmount = 0L;
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
