package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class HatchPatternLogic implements ICraftingProvider {
   private final MEPatternInputHatchBlockEntity host;
   private final ItemStackHandler patternInventory = new ItemStackHandler(9) {
      @Override
      public boolean isItemValid(int var1, ItemStack var2) {
         return PatternDetailsHelper.isEncodedPattern(var2);
      }

      @Override
      protected void onContentsChanged(int var1) {
         HatchPatternLogic.this.host.markDirtyAndSync();
         HatchPatternLogic.this.updatePatterns();
      }
   };
   private final List<IPatternDetails> patterns = new ArrayList<>();
   private final List<GenericStack> sendList = new ArrayList<>();

   public HatchPatternLogic(MEPatternInputHatchBlockEntity var1) {
      this.host = var1;
   }

   public ItemStackHandler getPatternInventory() {
      return this.patternInventory;
   }

   public List<GenericStack> getSendList() {
      return this.sendList;
   }

   public void updatePatterns() {
      this.patterns.clear();

      for (int var1 = 0; var1 < this.patternInventory.getSlots(); var1++) {
         ItemStack var2 = this.patternInventory.getStackInSlot(var1);
         IPatternDetails var3 = PatternDetailsHelper.decodePattern(var2, this.host.getLevel());
         if (var3 != null) {
            this.patterns.add(var3);
         }
      }

      ICraftingProvider.requestUpdate(this.host.getMainNode());
   }

   @Override
   public List<IPatternDetails> getAvailablePatterns() {
      return this.patterns;
   }

   @Override
   public boolean pushPattern(IPatternDetails var1, KeyCounter[] var2) {
      if (!this.sendList.isEmpty() || !this.host.getMainNode().isActive() || !this.patterns.contains(var1) || !this.host.canAcceptOrder()) {
         return false;
      }

      if (!this.canAcceptAllInputs(var2)) {
         return false;
      }

      var1.pushInputsToExternalInventory(var2, (var1x, var2x) -> {
         long var4 = this.host.insertBuffer(var1x, var2x, Actionable.MODULATE);
         if (var4 < var2x) {
            this.sendList.add(new GenericStack(var1x, var2x - var4));
         }
      });
      this.host.markDirtyAndSync();
      return true;
   }

   private boolean canAcceptAllInputs(KeyCounter[] var1) {
      KeyCounter[] var2 = var1;
      int var3 = var2.length;

      for (int var4 = 0; var4 < var3; var4++) {
         for (Entry var7 : var2[var4]) {
            AEKey var8 = (AEKey)var7.getKey();
            long var9 = var7.getLongValue();
            if (var9 > 0L && this.host.insertBuffer(var8, var9, Actionable.SIMULATE) < var9) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean isBusy() {
      return !this.sendList.isEmpty();
   }

   public boolean flushSendList() {
      if (this.sendList.isEmpty()) {
         return false;
      }

      boolean var1 = false;
      ListIterator<GenericStack> var2 = this.sendList.listIterator();

      while (var2.hasNext()) {
         GenericStack var3 = var2.next();
         long var4 = this.host.insertBuffer(var3.what(), var3.amount(), Actionable.MODULATE);
         if (var4 > 0L) {
            var1 = true;
         }

         if (var4 >= var3.amount()) {
            var2.remove();
         } else if (var4 > 0L) {
            var2.set(new GenericStack(var3.what(), var3.amount() - var4));
         }
      }

      if (this.sendList.isEmpty()) {
         ICraftingProvider.requestUpdate(this.host.getMainNode());
      }

      return var1;
   }

   public void writeToNBT(CompoundTag var1, HolderLookup.Provider var2) {
      var1.put("patterns", this.patternInventory.serializeNBT(var2));
      ListTag var3 = new ListTag();

      for (GenericStack var5 : this.sendList) {
         var3.add(GenericStack.writeTag(var2, var5));
      }

      var1.put("sendList", var3);
   }

   public void readFromNBT(CompoundTag var1, HolderLookup.Provider var2) {
      if (var1.contains("patterns")) {
         this.patternInventory.deserializeNBT(var2, var1.getCompound("patterns"));
      }

      this.sendList.clear();
      if (var1.contains("sendList")) {
         ListTag var3 = var1.getList("sendList", 10);

         for (int var4 = 0; var4 < var3.size(); var4++) {
            GenericStack var5 = GenericStack.readTag(var2, var3.getCompound(var4));
            if (var5 != null) {
               this.sendList.add(var5);
            }
         }
      }
   }

   public void addDrops(List<ItemStack> var1) {
      for (int var2 = 0; var2 < this.patternInventory.getSlots(); var2++) {
         ItemStack var3 = this.patternInventory.getStackInSlot(var2);
         if (!var3.isEmpty()) {
            var1.add(var3.copy());
         }
      }

      if (this.host.getLevel() != null) {
         for (GenericStack var5 : this.sendList) {
            var5.what().addDrops(var5.amount(), var1, this.host.getLevel(), this.host.getBlockPos());
         }
      }
   }
}
