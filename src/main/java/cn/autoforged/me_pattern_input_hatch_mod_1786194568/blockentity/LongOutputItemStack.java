package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.world.item.Item;

/** MI output slot whose transient capacity is large enough for parallel recipes. */
public final class LongOutputItemStack extends ConfigurableItemStack {
    private static final long CAPACITY = Integer.MAX_VALUE;

    public LongOutputItemStack() {
        super();
        this.pipesExtract = true;
    }

    public LongOutputItemStack(ConfigurableItemStack other) {
        super(other);
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    @Override
    public long getRemainingCapacityFor(ItemVariant key) {
        return Math.max(0L, CAPACITY - this.getAmount());
    }

    @Override
    public long getTotalCapacityFor(Item item) {
        return CAPACITY;
    }

}
