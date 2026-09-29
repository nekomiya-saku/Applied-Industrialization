/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.crafting.IPatternDetails
 *  appeng.api.crafting.PatternDetailsHelper
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.crafting.ICraftingProvider
 *  appeng.api.stacks.AEKey
 *  appeng.api.stacks.GenericStack
 *  appeng.api.stacks.KeyCounter
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.items.ItemStackHandler
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

        public boolean isItemValid(int n, ItemStack itemStack) {
            return PatternDetailsHelper.isEncodedPattern((ItemStack)itemStack);
        }

        protected void onContentsChanged(int n) {
            HatchPatternLogic.this.host.markDirtyAndSync();
            HatchPatternLogic.this.updatePatterns();
        }
    };
    private final List<IPatternDetails> patterns = new ArrayList<IPatternDetails>();
    private final List<GenericStack> sendList = new ArrayList<GenericStack>();

    public HatchPatternLogic(MEPatternInputHatchBlockEntity mEPatternInputHatchBlockEntity) {
        this.host = mEPatternInputHatchBlockEntity;
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
            ItemStack itemStack = this.patternInventory.getStackInSlot(i);
            IPatternDetails iPatternDetails = PatternDetailsHelper.decodePattern((ItemStack)itemStack, (Level)this.host.getLevel());
            if (iPatternDetails == null) continue;
            this.patterns.add(iPatternDetails);
        }
        ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
    }

    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    public boolean pushPattern(IPatternDetails iPatternDetails, KeyCounter[] keyCounterArray) {
        if (!(this.sendList.isEmpty() && this.host.getMainNode().isActive() && this.patterns.contains(iPatternDetails) && this.host.canAcceptOrder())) {
            return false;
        }
        if (!this.canAcceptAllInputs(keyCounterArray)) {
            return false;
        }
        iPatternDetails.pushInputsToExternalInventory(keyCounterArray, (aEKey, l) -> {
            long l2 = this.host.insertBuffer(aEKey, l, Actionable.MODULATE);
            if (l2 < l) {
                this.sendList.add(new GenericStack(aEKey, l - l2));
            }
        });
        this.host.markDirtyAndSync();
        return true;
    }

    private boolean canAcceptAllInputs(KeyCounter[] keyCounterArray) {
        for (KeyCounter keyCounter : keyCounterArray) {
            for (Object2LongMap.Entry entry : keyCounter) {
                AEKey aEKey = (AEKey)entry.getKey();
                long l = entry.getLongValue();
                if (l <= 0L || this.host.insertBuffer(aEKey, l, Actionable.SIMULATE) >= l) continue;
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
        boolean bl = false;
        ListIterator<GenericStack> listIterator = this.sendList.listIterator();
        while (listIterator.hasNext()) {
            GenericStack genericStack = listIterator.next();
            long l = this.host.insertBuffer(genericStack.what(), genericStack.amount(), Actionable.MODULATE);
            if (l > 0L) {
                bl = true;
            }
            if (l >= genericStack.amount()) {
                listIterator.remove();
                continue;
            }
            if (l <= 0L) continue;
            listIterator.set(new GenericStack(genericStack.what(), genericStack.amount() - l));
        }
        if (this.sendList.isEmpty()) {
            ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
        }
        return bl;
    }

    public void writeToNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put("patterns", (Tag)this.patternInventory.serializeNBT(provider));
        ListTag listTag = new ListTag();
        for (GenericStack genericStack : this.sendList) {
            listTag.add((Object)GenericStack.writeTag((HolderLookup.Provider)provider, (GenericStack)genericStack));
        }
        compoundTag.put("sendList", (Tag)listTag);
    }

    public void readFromNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (compoundTag.contains("patterns")) {
            this.patternInventory.deserializeNBT(provider, compoundTag.getCompound("patterns"));
        }
        this.sendList.clear();
        if (compoundTag.contains("sendList")) {
            ListTag listTag = compoundTag.getList("sendList", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                GenericStack genericStack = GenericStack.readTag((HolderLookup.Provider)provider, (CompoundTag)listTag.getCompound(i));
                if (genericStack == null) continue;
                this.sendList.add(genericStack);
            }
        }
    }

    public void addDrops(List<ItemStack> list) {
        for (int i = 0; i < this.patternInventory.getSlots(); ++i) {
            ItemStack itemStack = this.patternInventory.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack.copy());
        }
        if (this.host.getLevel() != null) {
            for (GenericStack genericStack : this.sendList) {
                genericStack.what().addDrops(genericStack.amount(), list, this.host.getLevel(), this.host.getBlockPos());
            }
        }
    }
}

