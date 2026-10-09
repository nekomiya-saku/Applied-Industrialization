package aeind.isolation;

import aeind.blockentity.AEKeyLongStorage;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/** Long-count, AE-keyed input storage owned by one isolated recipe room. */
public final class RoomInputStorage {
    private final AEKeyLongStorage storage;

    public RoomInputStorage(Runnable onChanged) {
        this.storage = new AEKeyLongStorage(onChanged);
    }

    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    public long getAmount(AEKey key) {
        return this.storage.getAmount(key);
    }

    public Map<AEKey, Long> snapshot() {
        return this.storage.snapshot();
    }

    public long revision() {
        return this.storage.revision();
    }

    public long insert(AEKey key, long amount, Actionable mode) {
        return this.storage.insert(key, amount, mode);
    }

    public long extract(AEKey key, long amount, Actionable mode) {
        return this.storage.extract(key, amount, mode);
    }

    public boolean extractAll(Map<AEKey, Long> requested, Actionable mode) {
        return this.storage.extractAll(requested, mode);
    }

    public void clear() {
        this.storage.clear();
    }

    public boolean flushTo(MEStorage target, IActionSource source) {
        return this.storage.flushTo(target, source);
    }

    public boolean flushTo(MEStorage target, IActionSource source, Predicate<AEKey> filter) {
        return this.storage.flushTo(target, source, filter);
    }

    public ListTag writeNbt(HolderLookup.Provider provider) {
        return this.storage.writeNbt(provider);
    }

    public void readNbt(ListTag entries, HolderLookup.Provider provider) {
        this.storage.readNbt(entries, provider);
    }

    public void addItemDrops(List<ItemStack> drops) {
        this.storage.addItemDrops(drops);
    }

    public MiInputView createMiView() {
        Map<AEKey, Long> source = this.storage.snapshot();
        List<ConfigurableItemStack> items = new ArrayList<>();
        List<ConfigurableFluidStack> fluids = new ArrayList<>();
        for (Map.Entry<AEKey, Long> entry : source.entrySet()) {
            long amount = entry.getValue();
            if (amount <= 0L) {
                continue;
            }
            if (entry.getKey() instanceof AEItemKey itemKey) {
                ConfigurableItemStack stack = new ConfigurableItemStack();
                stack.setKey(ItemVariant.of(itemKey.toStack(1)));
                stack.setAmount(amount);
                items.add(stack);
            } else if (entry.getKey() instanceof AEFluidKey fluidKey) {
                ConfigurableFluidStack stack = new ConfigurableFluidStack(amount);
                stack.setKey(FluidVariant.of(fluidKey.getFluid()));
                stack.setAmount(amount);
                fluids.add(stack);
            }
        }
        return new MiInputView(items, fluids, source);
    }

    public void importLegacy(
            List<ConfigurableItemStack> itemInputs,
            List<ConfigurableFluidStack> fluidInputs) {
        for (ConfigurableItemStack stack : itemInputs) {
            if (!stack.isEmpty() && stack.getAmount() > 0L) {
                this.storage.insert(AEItemKey.of(stack.toStack()), stack.getAmount(), Actionable.MODULATE);
            }
        }
        for (ConfigurableFluidStack stack : fluidInputs) {
            if (!stack.isEmpty() && stack.getAmount() > 0L) {
                this.storage.insert(
                        AEFluidKey.of(stack.getResource().getFluid()),
                        stack.getAmount(),
                        Actionable.MODULATE);
            }
        }
    }

    public record MiInputView(
            List<ConfigurableItemStack> itemInputs,
            List<ConfigurableFluidStack> fluidInputs,
            Map<AEKey, Long> sourceAmounts) {

        public Map<AEKey, Long> consumedAmounts() {
            Map<AEKey, Long> remaining = new LinkedHashMap<>();
            for (ConfigurableItemStack stack : this.itemInputs) {
                if (!stack.isEmpty() && stack.getAmount() > 0L) {
                    merge(remaining, AEItemKey.of(stack.toStack()), stack.getAmount());
                }
            }
            for (ConfigurableFluidStack stack : this.fluidInputs) {
                if (!stack.isEmpty() && stack.getAmount() > 0L) {
                    merge(remaining, AEFluidKey.of(stack.getResource().getFluid()), stack.getAmount());
                }
            }

            Map<AEKey, Long> consumed = new LinkedHashMap<>();
            for (Map.Entry<AEKey, Long> entry : this.sourceAmounts.entrySet()) {
                long amount = entry.getValue() - remaining.getOrDefault(entry.getKey(), 0L);
                if (amount > 0L) {
                    consumed.put(entry.getKey(), amount);
                }
            }
            return consumed;
        }

        private static void merge(Map<AEKey, Long> target, AEKey key, long amount) {
            if (key == null || amount <= 0L) {
                return;
            }
            target.merge(key, amount, RoomInputStorage::saturatedAdd);
        }
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
