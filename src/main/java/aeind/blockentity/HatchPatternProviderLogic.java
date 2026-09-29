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
package aeind.blockentity;

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
import aeind.blockentity.PatternInputHatchHost;
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

public class HatchPatternProviderLogic
extends PatternProviderLogic {
    private static final String NBT_BUFFER_SEND_LIST = "bufferSendList";
    protected final PatternInputHatchHost host;
    private final List<GenericStack> bufferSendList = new ArrayList<GenericStack>();

    public HatchPatternProviderLogic(IManagedGridNode iManagedGridNode, PatternInputHatchHost patternInputHatchHost) {
        super(iManagedGridNode, (PatternProviderLogicHost)patternInputHatchHost, 9);
        this.host = patternInputHatchHost;
    }

    public boolean pushPattern(IPatternDetails iPatternDetails, KeyCounter[] keyCounterArray) {
        if (!(this.bufferSendList.isEmpty() && this.host.getMainNode().isActive() && this.getAvailablePatterns().contains(iPatternDetails))) {
            return false;
        }
        if (this.getCraftingLockedReason() != LockCraftingMode.NONE || !this.host.canAcceptOrder()) {
            return false;
        }
        if (!this.canAcceptAllInputs(keyCounterArray)) {
            return false;
        }
        iPatternDetails.pushInputsToExternalInventory(keyCounterArray, (aEKey, l) -> {
            long l2 = this.host.insertBuffer(aEKey, l, Actionable.MODULATE);
            if (l2 < l) {
                this.bufferSendList.add(new GenericStack(aEKey, l - l2));
            }
        });
        this.host.saveChanges();
        return true;
    }

    public boolean isBusy() {
        return !this.bufferSendList.isEmpty();
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

    public boolean flushBufferSendList() {
        if (this.bufferSendList.isEmpty()) {
            return false;
        }
        boolean bl = false;
        ListIterator<GenericStack> listIterator = this.bufferSendList.listIterator();
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
        if (this.bufferSendList.isEmpty()) {
            ICraftingProvider.requestUpdate((IManagedGridNode)this.host.getMainNode());
        }
        return bl;
    }

    public Set<AEKey> getPatternInputKeys() {
        HashSet<AEKey> hashSet = new HashSet<AEKey>();
        for (IPatternDetails iPatternDetails : this.getAvailablePatterns()) {
            for (IPatternDetails.IInput iInput : iPatternDetails.getInputs()) {
                for (GenericStack genericStack : iInput.getPossibleInputs()) {
                    hashSet.add(genericStack.what().dropSecondary());
                }
            }
        }
        return hashSet;
    }

    public void onChangeInventory(AppEngInternalInventory appEngInternalInventory, int n) {
        super.onChangeInventory(appEngInternalInventory, n);
        this.host.returnUnusedBufferToNetwork();
        this.pruneBufferSendList();
    }

    private void pruneBufferSendList() {
        if (this.bufferSendList.isEmpty()) {
            return;
        }
        Set<AEKey> set = this.getPatternInputKeys();
        Iterator<GenericStack> iterator = this.bufferSendList.iterator();
        while (iterator.hasNext()) {
            GenericStack genericStack = iterator.next();
            if (set.contains(genericStack.what().dropSecondary())) continue;
            iterator.remove();
            this.host.returnToNetwork(genericStack.what(), genericStack.amount());
        }
    }

    public void writeToNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.writeToNBT(compoundTag, provider);
        ListTag listTag = new ListTag();
        for (GenericStack genericStack : this.bufferSendList) {
            listTag.add((Object)GenericStack.writeTag((HolderLookup.Provider)provider, (GenericStack)genericStack));
        }
        compoundTag.put(NBT_BUFFER_SEND_LIST, (Tag)listTag);
    }

    public void readFromNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.readFromNBT(compoundTag, provider);
        this.bufferSendList.clear();
        if (compoundTag.contains(NBT_BUFFER_SEND_LIST)) {
            ListTag listTag = compoundTag.getList(NBT_BUFFER_SEND_LIST, 10);
            for (int i = 0; i < listTag.size(); ++i) {
                GenericStack genericStack = GenericStack.readTag((HolderLookup.Provider)provider, (CompoundTag)listTag.getCompound(i));
                if (genericStack == null) continue;
                this.bufferSendList.add(genericStack);
            }
        }
    }
}

