package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/** A persistent, long-count buffer for outputs waiting to enter the AE network. */
public final class AEKeyLongStorage {
    private final Map<AEKey, Long> contents = new LinkedHashMap<>();
    private final Runnable onChanged;
    private long revision;

    public AEKeyLongStorage(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    public int size() {
        return this.contents.size();
    }

    public long getAmount(AEKey key) {
        return key == null ? 0L : this.contents.getOrDefault(key, 0L);
    }

    public Map<AEKey, Long> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.contents));
    }

    /** Monotonically changes whenever the contents are modulated. */
    public long revision() {
        return this.revision;
    }

    public long insert(AEKey key, long amount, Actionable mode) {
        if (key == null || amount <= 0L) {
            return 0L;
        }
        long stored = this.contents.getOrDefault(key, 0L);
        long inserted = Math.min(amount, Long.MAX_VALUE - stored);
        if (inserted > 0L && mode == Actionable.MODULATE) {
            this.contents.put(key, stored + inserted);
            ++this.revision;
            this.onChanged.run();
        }
        return inserted;
    }

    public long extract(AEKey key, long amount, Actionable mode) {
        if (key == null || amount <= 0L) {
            return 0L;
        }
        long stored = this.contents.getOrDefault(key, 0L);
        long extracted = Math.min(amount, stored);
        if (extracted > 0L && mode == Actionable.MODULATE) {
            long remaining = stored - extracted;
            if (remaining == 0L) {
                this.contents.remove(key);
            } else {
                this.contents.put(key, remaining);
            }
            ++this.revision;
            this.onChanged.run();
        }
        return extracted;
    }

    /**
     * Atomically extracts all requested amounts. No entry is changed unless every
     * positive request can be satisfied in full.
     */
    public boolean extractAll(Map<AEKey, Long> requested, Actionable mode) {
        if (requested == null || requested.isEmpty()) {
            return true;
        }
        for (Map.Entry<AEKey, Long> entry : requested.entrySet()) {
            AEKey key = entry.getKey();
            long amount = entry.getValue() == null ? 0L : entry.getValue();
            if (key == null || amount < 0L || this.getAmount(key) < amount) {
                return false;
            }
        }
        if (mode != Actionable.MODULATE) {
            return true;
        }

        boolean changed = false;
        for (Map.Entry<AEKey, Long> entry : requested.entrySet()) {
            long amount = entry.getValue() == null ? 0L : entry.getValue();
            if (amount <= 0L) {
                continue;
            }
            AEKey key = entry.getKey();
            long remaining = this.contents.get(key) - amount;
            if (remaining == 0L) {
                this.contents.remove(key);
            } else {
                this.contents.put(key, remaining);
            }
            changed = true;
        }
        if (changed) {
            ++this.revision;
            this.onChanged.run();
        }
        return true;
    }

    public void clear() {
        if (!this.contents.isEmpty()) {
            this.contents.clear();
            ++this.revision;
            this.onChanged.run();
        }
    }

    public void getAvailableStacks(KeyCounter output) {
        for (Map.Entry<AEKey, Long> entry : this.contents.entrySet()) {
            output.add(entry.getKey(), entry.getValue());
        }
    }

    public boolean flushTo(MEStorage target, IActionSource source) {
        return this.flushTo(target, source, key -> true, Integer.MAX_VALUE);
    }

    public boolean flushTo(MEStorage target, IActionSource source, Predicate<AEKey> filter) {
        return this.flushTo(target, source, filter, Integer.MAX_VALUE);
    }

    public boolean flushTo(MEStorage target, IActionSource source, Predicate<AEKey> filter, int maxEntries) {
        int attempts = Math.min(maxEntries, this.contents.size());
        if (attempts <= 0) {
            return false;
        }
        boolean changed = false;
        for (int processed = 0; processed < attempts; ++processed) {
            Iterator<Map.Entry<AEKey, Long>> iterator = this.contents.entrySet().iterator();
            Map.Entry<AEKey, Long> entry = iterator.next();
            AEKey key = entry.getKey();
            long stored = entry.getValue();
            iterator.remove();
            if (!filter.test(entry.getKey())) {
                this.contents.put(key, stored);
                continue;
            }
            long inserted = target.insert(key, stored, Actionable.MODULATE, source);
            if (inserted <= 0L) {
                this.contents.put(key, stored);
                continue;
            }
            changed = true;
            if (inserted < stored) {
                this.contents.put(key, stored - inserted);
            }
        }
        if (changed) {
            this.onChanged.run();
        }
        return changed;
    }

    public ListTag writeNbt(HolderLookup.Provider provider) {
        ListTag entries = new ListTag();
        for (Map.Entry<AEKey, Long> entry : this.contents.entrySet()) {
            entries.add(GenericStack.writeTag(provider, new GenericStack(entry.getKey(), entry.getValue())));
        }
        return entries;
    }

    public void readNbt(ListTag entries, HolderLookup.Provider provider) {
        this.contents.clear();
        for (int i = 0; i < entries.size(); ++i) {
            GenericStack stack = GenericStack.readTag(provider, entries.getCompound(i));
            if (stack == null || stack.amount() <= 0L) {
                continue;
            }
            long stored = this.contents.getOrDefault(stack.what(), 0L);
            this.contents.put(stack.what(), saturatedAdd(stored, stack.amount()));
        }
        ++this.revision;
    }

    public void addItemDrops(List<ItemStack> drops) {
        for (Map.Entry<AEKey, Long> entry : this.contents.entrySet()) {
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                continue;
            }
            long remaining = entry.getValue();
            int maxStackSize = Math.max(1, itemKey.toStack(1).getMaxStackSize());
            while (remaining > 0L) {
                int count = (int) Math.min(remaining, maxStackSize);
                drops.add(itemKey.toStack(count));
                remaining -= count;
            }
        }
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
