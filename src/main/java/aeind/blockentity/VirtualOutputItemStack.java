package aeind.blockentity;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A blank MI output endpoint. It accepts one output at a time and immediately
 * moves the amount to the hatch's long-count buffer, so output types do not
 * consume persistent MI slots.
 */
public final class VirtualOutputItemStack extends ConfigurableItemStack {
    public static final long CAPACITY = Integer.MAX_VALUE;
    private VirtualOutputSink sink;
    private ItemVariant pendingKey;
    private long pendingAmount;

    public VirtualOutputItemStack() {
        this(null);
    }

    private VirtualOutputItemStack(VirtualOutputSink sink) {
        super();
        this.sink = sink;
        this.pipesExtract = false;
    }

    public VirtualOutputItemStack detached() {
        return new VirtualOutputItemStack(null);
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
    public ItemVariant getResource() {
        return ItemVariant.blank();
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
    public long getRemainingCapacityFor(ItemVariant key) {
        return key == null || key.isBlank() ? 0L : CAPACITY;
    }

    @Override
    public long getTotalCapacityFor(Item item) {
        return CAPACITY;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    @Override
    public void setAmount(long amount) {
        pendingAmount = Math.max(0L, amount);
        if (pendingAmount == 0L) {
            pendingKey = null;
        } else if (pendingKey != null) {
            flushPending();
        }
    }

    @Override
    public void setKey(ItemVariant key) {
        if (key == null || key.isBlank()) {
            empty();
            return;
        }
        pendingKey = key;
        if (pendingAmount > 0L) {
            flushPending();
        }
    }

    @Override
    public void increment(long amount) {
        if (amount > 0L && pendingKey != null) {
            pendingAmount = saturatedAdd(pendingAmount, amount);
            flushPending();
        }
    }

    @Override
    public void decrement(long amount) {}

    @Override
    public void empty() {
        pendingKey = null;
        pendingAmount = 0L;
    }

    @Override
    public void enableMachineLock(Item item) {}

    @Override
    public void disableMachineLock() {
        empty();
    }

    private void flushPending() {
        if (sink != null && pendingKey != null && pendingAmount > 0L) {
            sink.acceptItemOutput(pendingKey, pendingAmount);
        }
        pendingKey = null;
        pendingAmount = 0L;
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
