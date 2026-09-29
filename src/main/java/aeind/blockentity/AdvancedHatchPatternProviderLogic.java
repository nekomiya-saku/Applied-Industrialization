/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.config.LockCraftingMode
 *  appeng.api.crafting.IPatternDetails
 *  appeng.api.crafting.PatternDetailsHelper
 *  appeng.api.inventories.InternalInventory
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.crafting.ICraftingProvider
 *  appeng.api.stacks.AEKey
 *  appeng.api.stacks.GenericStack
 *  appeng.api.stacks.KeyCounter
 *  appeng.util.inv.AppEngInternalInventory
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package aeind.blockentity;

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
import appeng.util.inv.AppEngInternalInventory;
import aeind.blockentity.AdvancedPatternInputHatchBlockEntity;
import aeind.blockentity.HatchPatternProviderLogic;
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

public class AdvancedHatchPatternProviderLogic
extends HatchPatternProviderLogic {
    private static final String NBT_ADVANCED_SEND_LIST = "advancedBufferSendList";
    private final AdvancedPatternInputHatchBlockEntity advancedHost;
    private final List<GenericStack>[] bufferSendLists;

    public AdvancedHatchPatternProviderLogic(IManagedGridNode iManagedGridNode, AdvancedPatternInputHatchBlockEntity advancedPatternInputHatchBlockEntity) {
        super(iManagedGridNode, advancedPatternInputHatchBlockEntity);
        this.advancedHost = advancedPatternInputHatchBlockEntity;
        this.bufferSendLists = new List[9];
        for (int i = 0; i < this.bufferSendLists.length; ++i) {
            this.bufferSendLists[i] = new ArrayList<GenericStack>();
        }
    }

    private int findRoom(IPatternDetails iPatternDetails) {
        IPatternDetails iPatternDetails2;
        ItemStack itemStack;
        int n;
        if (this.advancedHost.getLevel() == null) {
            return -1;
        }
        InternalInventory internalInventory = this.getPatternInv();
        for (n = 0; n < internalInventory.size(); ++n) {
            itemStack = internalInventory.getStackInSlot(n);
            if (itemStack.isEmpty() || (iPatternDetails2 = PatternDetailsHelper.decodePattern((ItemStack)itemStack, (Level)this.advancedHost.getLevel())) == null || !iPatternDetails2.getDefinition().equals((Object)iPatternDetails.getDefinition()) || !this.bufferSendLists[n].isEmpty() || this.advancedHost.roomHasContent(n)) continue;
            return n;
        }
        for (n = 0; n < internalInventory.size(); ++n) {
            itemStack = internalInventory.getStackInSlot(n);
            if (itemStack.isEmpty() || (iPatternDetails2 = PatternDetailsHelper.decodePattern((ItemStack)itemStack, (Level)this.advancedHost.getLevel())) == null || !iPatternDetails2.getDefinition().equals((Object)iPatternDetails.getDefinition())) continue;
            return n;
        }
        return -1;
    }

    @Override
    public boolean pushPattern(IPatternDetails iPatternDetails, KeyCounter[] keyCounterArray) {
        int n = this.findRoom(iPatternDetails);
        if (!(n >= 0 && this.advancedHost.getMainNode().isActive() && this.getCraftingLockedReason() == LockCraftingMode.NONE && this.advancedHost.canAcceptOrder(n) && this.canAcceptAllInputs(n, keyCounterArray))) {
            return false;
        }
        iPatternDetails.pushInputsToExternalInventory(keyCounterArray, (aEKey, l) -> {
            long l2 = this.advancedHost.insertBuffer(n, aEKey, l, Actionable.MODULATE);
            if (l2 < l) {
                this.bufferSendLists[n].add(new GenericStack(aEKey, l - l2));
            }
        });
        this.advancedHost.saveChanges();
        return true;
    }

    private boolean canAcceptAllInputs(int n, KeyCounter[] keyCounterArray) {
        for (KeyCounter keyCounter : keyCounterArray) {
            for (Object2LongMap.Entry entry : keyCounter) {
                if (entry.getLongValue() <= 0L || this.advancedHost.insertBuffer(n, (AEKey)entry.getKey(), entry.getLongValue(), Actionable.SIMULATE) >= entry.getLongValue()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        for (List<GenericStack> list : this.bufferSendLists) {
            if (list.isEmpty()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean flushBufferSendList() {
        boolean bl = false;
        for (int i = 0; i < this.bufferSendLists.length; ++i) {
            ListIterator<GenericStack> listIterator = this.bufferSendLists[i].listIterator();
            while (listIterator.hasNext()) {
                GenericStack genericStack = listIterator.next();
                long l = this.advancedHost.insertBuffer(i, genericStack.what(), genericStack.amount(), Actionable.MODULATE);
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
            ICraftingProvider.requestUpdate((IManagedGridNode)this.advancedHost.getMainNode());
        }
        return bl;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory appEngInternalInventory, int n) {
        if (n >= 0 && n < this.bufferSendLists.length) {
            this.advancedHost.returnRoomToNetwork(n);
            this.bufferSendLists[n].clear();
        }
        super.onChangeInventory(appEngInternalInventory, n);
    }

    @Override
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
        compoundTag.put(NBT_ADVANCED_SEND_LIST, (Tag)listTag);
    }

    @Override
    public void readFromNBT(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.readFromNBT(compoundTag, provider);
        if (!compoundTag.contains(NBT_ADVANCED_SEND_LIST, 9)) {
            return;
        }
        ListTag listTag = compoundTag.getList(NBT_ADVANCED_SEND_LIST, 9);
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
                genericStack.what().addDrops(genericStack.amount(), list, this.advancedHost.getLevel(), this.advancedHost.getBlockPos());
            }
        }
    }
}

