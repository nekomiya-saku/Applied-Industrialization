/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

public class HatchPatternLogic
implements ICraftingProvider {
    private final MEPatternInputHatchBlockEntity host;
    private final ItemStackHandler patternInventory = new ItemStackHandler(9){

        public boolean isItemValid(int slot, ItemStack stack) {
            return PatternDetailsHelper.isEncodedPattern((ItemStack)stack);
        }

        protected void onContentsChanged(int slot) {
            HatchPatternLogic.this.host.markDirtyAndSync();
            HatchPatternLogic.this.updatePatterns();
        }
    };
    private final List<IPatternDetails> patterns = new ArrayList<IPatternDetails>();
    private final List<GenericStack> sendList = new ArrayList<GenericStack>();

    public HatchPatternLogic(MEPatternInputHatchBlockEntity host) {
        this.host = host;
    }

    public ItemStackHandler getPatternInventory() {
        return this.patternInventory;
    }

    public List<GenericStack> getSendList() {
        return this.sendList;
    }

    public void updatePatterns() {
        this.patterns.clear();
        for (int i = 0; i < this.patternInventory.getSlots(); ++i) {
            ItemStack stack = this.patternInventory.getStackInSlot(i);
            IPatternDetails details = PatternDetailsHelper.decodePattern((ItemStack)stack, (Level)this.host.getLevel());
            if (details == null) continue;
            this.patterns.add(details);
        }
        ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
    }

    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder) {
        if (!(this.sendList.isEmpty() && this.host.getMainNode().isActive() && this.patterns.contains(details) && this.host.canAcceptOrder())) {
            return false;
        }
        if (!this.canAcceptAllInputs(inputHolder)) {
            return false;
        }
        details.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
            long inserted = this.host.insertBuffer(what, amount, Actionable.MODULATE);
            if (inserted < amount) {
                this.sendList.add(new GenericStack(what, amount - inserted));
            }
        });
        this.host.markDirtyAndSync();
        return true;
    }

    private boolean canAcceptAllInputs(KeyCounter[] inputHolder) {
        for (KeyCounter counter : inputHolder) {
            for (Object2LongMap.Entry entry : counter) {
                AEKey what = (AEKey)entry.getKey();
                long amount = entry.getLongValue();
                if (amount <= 0L || this.host.insertBuffer(what, amount, Actionable.SIMULATE) >= amount) continue;
                return false;
            }
        }
        return true;
    }

    public boolean isBusy() {
        return !this.sendList.isEmpty();
    }

    public boolean flushSendList() {
        if (this.sendList.isEmpty()) {
            return false;
        }
        boolean changed = false;
        ListIterator<GenericStack> it = this.sendList.listIterator();
        while (it.hasNext()) {
            GenericStack stack = it.next();
            long inserted = this.host.insertBuffer(stack.what(), stack.amount(), Actionable.MODULATE);
            if (inserted > 0L) {
                changed = true;
            }
            if (inserted >= stack.amount()) {
                it.remove();
                continue;
            }
            if (inserted <= 0L) continue;
            it.set(new GenericStack(stack.what(), stack.amount() - inserted));
        }
        if (this.sendList.isEmpty()) {
            ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
        }
        return changed;
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("patterns", (Tag)this.patternInventory.serializeNBT(registries));
        ListTag sendListTag = new ListTag();
        for (GenericStack stack : this.sendList) {
            sendListTag.add((Object)GenericStack.writeTag((HolderLookup.Provider)registries, (GenericStack)stack));
        }
        tag.put("sendList", (Tag)sendListTag);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("patterns")) {
            this.patternInventory.deserializeNBT(registries, tag.getCompound("patterns"));
        }
        this.sendList.clear();
        if (tag.contains("sendList")) {
            ListTag sendListTag = tag.getList("sendList", 10);
            for (int i = 0; i < sendListTag.size(); ++i) {
                GenericStack stack = GenericStack.readTag((HolderLookup.Provider)registries, (CompoundTag)sendListTag.getCompound(i));
                if (stack == null) continue;
                this.sendList.add(stack);
            }
        }
    }

    public void addDrops(List<ItemStack> drops) {
        for (int i = 0; i < this.patternInventory.getSlots(); ++i) {
            ItemStack itemStack = this.patternInventory.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            drops.add(itemStack.copy());
        }
        if (this.host.getLevel() != null) {
            for (GenericStack genericStack : this.sendList) {
                genericStack.what().addDrops(genericStack.amount(), drops, this.host.getLevel(), this.host.getBlockPos());
            }
        }
    }
}

