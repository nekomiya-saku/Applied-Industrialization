/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.config.LockCraftingMode
 *  appeng.api.crafting.IPatternDetails
 *  appeng.api.crafting.IPatternDetails$IInput
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.crafting.ICraftingProvider
 *  appeng.api.stacks.AEKey
 *  appeng.api.stacks.GenericStack
 *  appeng.api.stacks.KeyCounter
 *  appeng.helpers.patternprovider.PatternProviderLogic
 *  appeng.helpers.patternprovider.PatternProviderLogicHost
 *  appeng.util.inv.AppEngInternalInventory
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ExtendedPatternInputHatchBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ExtendedHatchPatternProviderLogic
extends PatternProviderLogic {
    private static final String NBT_BUFFER_SEND_LIST = "bufferSendList";
    private final ExtendedPatternInputHatchBlockEntity host;
    private final List<GenericStack> bufferSendList = new ArrayList<GenericStack>();

    public ExtendedHatchPatternProviderLogic(IManagedGridNode mainNode, ExtendedPatternInputHatchBlockEntity host) {
        super(mainNode, (PatternProviderLogicHost)host, 36);
        this.host = host;
    }

    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder) {
        if (!(this.bufferSendList.isEmpty() && this.host.getMainNode().isActive() && this.getAvailablePatterns().contains(details))) {
            return false;
        }
        if (this.getCraftingLockedReason() != LockCraftingMode.NONE || !this.host.canAcceptOrder()) {
            return false;
        }
        if (!this.canAcceptAllInputs(inputHolder)) {
            return false;
        }
        details.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
            long inserted = this.host.insertBuffer(what, amount, Actionable.MODULATE);
            if (inserted < amount) {
                this.bufferSendList.add(new GenericStack(what, amount - inserted));
            }
        });
        this.host.saveChanges();
        return true;
    }

    public boolean isBusy() {
        return !this.bufferSendList.isEmpty();
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

    public boolean flushBufferSendList() {
        if (this.bufferSendList.isEmpty()) {
            return false;
        }
        boolean changed = false;
        ListIterator<GenericStack> it = this.bufferSendList.listIterator();
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
        if (this.bufferSendList.isEmpty()) {
            ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
        }
        return changed;
    }

    public Set<AEKey> getPatternInputKeys() {
        HashSet<AEKey> keys = new HashSet<AEKey>();
        for (IPatternDetails details : this.getAvailablePatterns()) {
            for (IPatternDetails.IInput input : details.getInputs()) {
                for (GenericStack candidate : input.getPossibleInputs()) {
                    keys.add(candidate.what().dropSecondary());
                }
            }
        }
        return keys;
    }

    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);
        this.host.returnUnusedBufferToNetwork();
        this.pruneBufferSendList();
    }

    private void pruneBufferSendList() {
        if (this.bufferSendList.isEmpty()) {
            return;
        }
        Set<AEKey> allowed = this.getPatternInputKeys();
        Iterator<GenericStack> it = this.bufferSendList.iterator();
        while (it.hasNext()) {
            GenericStack stack = it.next();
            if (allowed.contains(stack.what().dropSecondary())) continue;
            it.remove();
            this.host.returnToNetwork(stack.what(), stack.amount());
        }
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        ListTag list = new ListTag();
        for (GenericStack stack : this.bufferSendList) {
            list.add(GenericStack.writeTag(registries, stack));
        }
        tag.put(NBT_BUFFER_SEND_LIST, (Tag)list);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        this.bufferSendList.clear();
        if (tag.contains(NBT_BUFFER_SEND_LIST)) {
            ListTag list = tag.getList(NBT_BUFFER_SEND_LIST, 10);
            for (int i = 0; i < list.size(); ++i) {
                GenericStack stack = GenericStack.readTag((HolderLookup.Provider)registries, (CompoundTag)list.getCompound(i));
                if (stack == null) continue;
                this.bufferSendList.add(stack);
            }
        }
    }
}
