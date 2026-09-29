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
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class AdvancedHatchPatternProviderLogic extends HatchPatternProviderLogic {
   private static final String NBT_ADVANCED_SEND_LIST = "advancedBufferSendList";
   private final AdvancedPatternInputHatchBlockEntity advancedHost;
   private final List<GenericStack>[] bufferSendLists;

   @SuppressWarnings("unchecked")
   public AdvancedHatchPatternProviderLogic(IManagedGridNode var1, AdvancedPatternInputHatchBlockEntity var2) {
      super(var1, var2);
      this.advancedHost = var2;
      this.bufferSendLists = (List<GenericStack>[])new List<?>[9];

      for (int var3 = 0; var3 < this.bufferSendLists.length; var3++) {
         this.bufferSendLists[var3] = new ArrayList<>();
      }
   }

   private int findRoom(IPatternDetails var1) {
      if (this.advancedHost.getLevel() == null) {
         return -1;
      }

      InternalInventory var2 = this.getPatternInv();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         ItemStack var4 = var2.getStackInSlot(var3);
         if (!var4.isEmpty()) {
            IPatternDetails var5 = PatternDetailsHelper.decodePattern(var4, this.advancedHost.getLevel());
            if (var5 != null
               && var5.getDefinition().equals(var1.getDefinition())
               && this.bufferSendLists[var3].isEmpty()
               && !this.advancedHost.roomHasContent(var3)) {
               return var3;
            }
         }
      }

      for (int var6 = 0; var6 < var2.size(); var6++) {
         ItemStack var7 = var2.getStackInSlot(var6);
         if (!var7.isEmpty()) {
            IPatternDetails var8 = PatternDetailsHelper.decodePattern(var7, this.advancedHost.getLevel());
            if (var8 != null && var8.getDefinition().equals(var1.getDefinition())) {
               return var6;
            }
         }
      }

      return -1;
   }

   @Override
   public boolean pushPattern(IPatternDetails var1, KeyCounter[] var2) {
      int var3 = this.findRoom(var1);
      if (var3 >= 0
         && this.advancedHost.getMainNode().isActive()
         && this.getCraftingLockedReason() == LockCraftingMode.NONE
         && this.advancedHost.canAcceptOrder(var3)
         && this.canAcceptAllInputs(var3, var2)) {
         var1.pushInputsToExternalInventory(var2, (var2x, var3x) -> {
            long var5 = this.advancedHost.insertBuffer(var3, var2x, var3x, Actionable.MODULATE);
            if (var5 < var3x) {
               this.bufferSendLists[var3].add(new GenericStack(var2x, var3x - var5));
            }
         });
         this.advancedHost.saveChanges();
         return true;
      } else {
         return false;
      }
   }

   private boolean canAcceptAllInputs(int var1, KeyCounter[] var2) {
      KeyCounter[] var3 = var2;
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         for (Entry var8 : var3[var5]) {
            if (var8.getLongValue() > 0L
               && this.advancedHost.insertBuffer(var1, (AEKey)var8.getKey(), var8.getLongValue(), Actionable.SIMULATE) < var8.getLongValue()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean isBusy() {
      for (List<GenericStack> var4 : this.bufferSendLists) {
         if (!var4.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean flushBufferSendList() {
      boolean var1 = false;

      for (int var2 = 0; var2 < this.bufferSendLists.length; var2++) {
         ListIterator<GenericStack> var3 = this.bufferSendLists[var2].listIterator();

         while (var3.hasNext()) {
            GenericStack var4 = var3.next();
            long var5 = this.advancedHost.insertBuffer(var2, var4.what(), var4.amount(), Actionable.MODULATE);
            var1 |= var5 > 0L;
            if (var5 >= var4.amount()) {
               var3.remove();
            } else if (var5 > 0L) {
               var3.set(new GenericStack(var4.what(), var4.amount() - var5));
            }
         }
      }

      if (!this.isBusy()) {
         ICraftingProvider.requestUpdate(this.advancedHost.getMainNode());
      }

      return var1;
   }

   @Override
   public void onChangeInventory(AppEngInternalInventory var1, int var2) {
      if (var2 >= 0 && var2 < this.bufferSendLists.length) {
         this.advancedHost.returnRoomToNetwork(var2);
         this.bufferSendLists[var2].clear();
      }

      super.onChangeInventory(var1, var2);
   }

   @Override
   public void writeToNBT(CompoundTag var1, HolderLookup.Provider var2) {
      super.writeToNBT(var1, var2);
      ListTag var3 = new ListTag();

      for (List<GenericStack> var7 : this.bufferSendLists) {
         ListTag var8 = new ListTag();

         for (GenericStack var10 : var7) {
            var8.add(GenericStack.writeTag(var2, var10));
         }

         var3.add(var8);
      }

      var1.put("advancedBufferSendList", var3);
   }

   @Override
   public void readFromNBT(CompoundTag var1, HolderLookup.Provider var2) {
      super.readFromNBT(var1, var2);
      if (var1.contains("advancedBufferSendList", 9)) {
         ListTag var3 = var1.getList("advancedBufferSendList", 9);

         for (int var4 = 0; var4 < Math.min(var3.size(), this.bufferSendLists.length); var4++) {
            this.bufferSendLists[var4].clear();
            ListTag var5 = var3.getList(var4);

            for (int var6 = 0; var6 < var5.size(); var6++) {
               GenericStack var7 = GenericStack.readTag(var2, var5.getCompound(var6));
               if (var7 != null) {
                  this.bufferSendLists[var4].add(var7);
               }
            }
         }
      }
   }

   @Override
   public void addDrops(List<ItemStack> var1) {
      super.addDrops(var1);
      List<GenericStack>[] var2 = this.bufferSendLists;
      int var3 = var2.length;

      for (int var4 = 0; var4 < var3; var4++) {
         for (GenericStack var7 : var2[var4]) {
            var7.what().addDrops(var7.amount(), var1, this.advancedHost.getLevel(), this.advancedHost.getBlockPos());
         }
      }
   }
}
