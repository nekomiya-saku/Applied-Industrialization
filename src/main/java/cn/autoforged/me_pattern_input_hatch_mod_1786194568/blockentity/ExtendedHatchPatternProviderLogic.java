/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.config.LockCraftingMode
 *  appeng.api.crafting.IPatternDetails
 *  appeng.api.crafting.IPatternDetails$IInput
 *  appeng.api.crafting.PatternDetailsHelper
 *  appeng.api.inventories.InternalInventory
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
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExtendedHatchPatternProviderLogic
extends PatternProviderLogic {
    private static final String NBT_BUFFER_SEND_LIST = "advancedExtendedBufferSendList";
    private final ExtendedPatternInputHatchBlockEntity host;
    private final List<GenericStack>[] bufferSendLists;

    public ExtendedHatchPatternProviderLogic(IManagedGridNode iManagedGridNode, ExtendedPatternInputHatchBlockEntity extendedPatternInputHatchBlockEntity) {
        super(iManagedGridNode, (PatternProviderLogicHost)extendedPatternInputHatchBlockEntity, 36);
        this.host = extendedPatternInputHatchBlockEntity;
        this.bufferSendLists = new List[36];
        for (int i = 0; i < this.bufferSendLists.length; ++i) {
            this.bufferSendLists[i] = new ArrayList<GenericStack>();
        }
    }

    private int findRoom(IPatternDetails iPatternDetails) {
        IPatternDetails iPatternDetails2;
        ItemStack itemStack;
        int n;
        if (this.host.getLevel() == null) {
            return -1;
        }
        InternalInventory internalInventory = this.getPatternInv();
        for (n = 0; n < internalInventory.size(); ++n) {
            itemStack = internalInventory.getStackInSlot(n);
            if (itemStack.isEmpty() || (iPatternDetails2 = PatternDetailsHelper.decodePattern((ItemStack)itemStack, (Level)this.host.getLevel())) == null || !iPatternDetails2.getDefinition().equals((Object)iPatternDetails.getDefinition()) || !this.bufferSendLists[n].isEmpty() || this.host.roomHasContent(n)) continue;
            return n;
        }
        for (n = 0; n < internalInventory.size(); ++n) {
            itemStack = internalInventory.getStackInSlot(n);
            if (itemStack.isEmpty() || (iPatternDetails2 = PatternDetailsHelper.decodePattern((ItemStack)itemStack, (Level)this.host.getLevel())) == null || !iPatternDetails2.getDefinition().equals((Object)iPatternDetails.getDefinition())) continue;
            return n;
        }
        return -1;
    }

    public boolean pushPattern(IPatternDetails iPatternDetails, KeyCounter[] keyCounterArray) {
        int n = this.findRoom(iPatternDetails);
        if (!(n >= 0 && this.host.getMainNode().isActive() && this.getCraftingLockedReason() == LockCraftingMode.NONE && this.host.canAcceptOrder(n) && this.canAcceptAllInputs(n, keyCounterArray))) {
            return false;
        }
        iPatternDetails.pushInputsToExternalInventory(keyCounterArray, (aEKey, l) -> {
            long l2 = this.host.insertBuffer(n, aEKey, l, Actionable.MODULATE);
            if (l2 < l) {
                this.bufferSendLists[n].add(new GenericStack(aEKey, l - l2));
            }
        });
        this.host.saveChanges();
        return true;
    }

    private boolean canAcceptAllInputs(int n, KeyCounter[] keyCounterArray) {
        for (KeyCounter keyCounter : keyCounterArray) {
            for (Object2LongMap.Entry entry : keyCounter) {
                if (entry.getLongValue() <= 0L || this.host.insertBuffer(n, (AEKey)entry.getKey(), entry.getLongValue(), Actionable.SIMULATE) >= entry.getLongValue()) continue;
                return false;
            }
        }
        return true;
    }

    public boolean isBusy() {
        for (List<GenericStack> list : this.bufferSendLists) {
            if (list.isEmpty()) continue;
            return true;
        }
        return false;
    }

    public boolean flushBufferSendList() {
        boolean bl = false;
        for (int i = 0; i < this.bufferSendLists.length; ++i) {
            ListIterator<GenericStack> listIterator = this.bufferSendLists[i].listIterator();
            while (listIterator.hasNext()) {
                GenericStack genericStack = listIterator.next();
                long l = this.host.insertBuffer(i, genericStack.what(), genericStack.amount(), Actionable.MODULATE);
                bl |= l > 0L;
                if (l >= genericStack.amount()) {
                    listIterator.remove();
                    continue;
                }
                if (l <= 0L) continue;
                listIterator.set(new GenericStack(genericStack.what(), genericStack.amount() - l));
            }
        }
        if (!this.isBusy()) {
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
        if (n >= 0 && n < this.bufferSendLists.length) {
            this.host.returnRoomToNetwork(n);
            this.bufferSendLists[n].clear();
        }
    }

    public void writeToNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.writeToNBT(compoundTag, provider);
        ListTag listTag = new ListTag();
        for (List<GenericStack> list : this.bufferSendLists) {
            ListTag listTag2 = new ListTag();
            for (GenericStack genericStack : list) {
                listTag2.add((Object)GenericStack.writeTag((HolderLookup.Provider)provider, (GenericStack)genericStack));
            }
            listTag.add((Object)listTag2);
        }
        compoundTag.put(NBT_BUFFER_SEND_LIST, (Tag)listTag);
    }

    public void readFromNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.readFromNBT(compoundTag, provider);
        if (!compoundTag.contains(NBT_BUFFER_SEND_LIST, 9)) {
            return;
        }
        ListTag listTag = compoundTag.getList(NBT_BUFFER_SEND_LIST, 9);
        for (int i = 0; i < Math.min(listTag.size(), this.bufferSendLists.length); ++i) {
            this.bufferSendLists[i].clear();
            ListTag listTag2 = listTag.getList(i);
            for (int j = 0; j < listTag2.size(); ++j) {
                GenericStack genericStack = GenericStack.readTag((HolderLookup.Provider)provider, (CompoundTag)listTag2.getCompound(j));
                if (genericStack == null) continue;
                this.bufferSendLists[i].add(genericStack);
            }
        }
    }

    public void addDrops(List<ItemStack> list) {
        super.addDrops(list);
        for (List<GenericStack> list2 : this.bufferSendLists) {
            for (GenericStack genericStack : list2) {
                genericStack.what().addDrops(genericStack.amount(), list, this.host.getLevel(), this.host.getBlockPos());
            }
        }
    }
}

