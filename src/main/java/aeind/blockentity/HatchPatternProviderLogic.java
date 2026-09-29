package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class HatchPatternProviderLogic extends PatternProviderLogic {
   private static final String NBT_BUFFER_SEND_LIST = "bufferSendList";
   protected final PatternInputHatchHost host;
   private final List<GenericStack> bufferSendList = new ArrayList<>();

   public HatchPatternProviderLogic(IManagedGridNode var1, PatternInputHatchHost var2) {
      super(var1, (PatternProviderLogicHost)var2, 9);
      this.host = var2;
   }

   @Override
   public boolean pushPattern(IPatternDetails var1, KeyCounter[] var2) {
      if (!this.bufferSendList.isEmpty() || !this.host.getMainNode().isActive() || !this.getAvailablePatterns().contains(var1)) {
         return false;
      }

      if (this.getCraftingLockedReason() != LockCraftingMode.NONE || !this.host.canAcceptOrder()) {
         return false;
      }

      if (!this.canAcceptAllInputs(var2)) {
         return false;
      }

      var1.pushInputsToExternalInventory(var2, (var1x, var2x) -> {
         long var4 = this.host.insertBuffer(var1x, var2x, Actionable.MODULATE);
         if (var4 < var2x) {
            this.bufferSendList.add(new GenericStack(var1x, var2x - var4));
         }
      });
      this.host.saveChanges();
      return true;
   }

   @Override
   public boolean isBusy() {
      return !this.bufferSendList.isEmpty();
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

   public boolean flushBufferSendList() {
      if (this.bufferSendList.isEmpty()) {
         return false;
      }

      boolean var1 = false;
      ListIterator<GenericStack> var2 = this.bufferSendList.listIterator();

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

      if (this.bufferSendList.isEmpty()) {
         ICraftingProvider.requestUpdate(this.host.getMainNode());
      }

      return var1;
   }

   public Set<AEKey> getPatternInputKeys() {
      HashSet<AEKey> var1 = new HashSet<>();

      for (IPatternDetails var3 : this.getAvailablePatterns()) {
         for (IInput var7 : var3.getInputs()) {
            for (GenericStack var11 : var7.getPossibleInputs()) {
               var1.add(var11.what().dropSecondary());
            }
         }
      }

      return var1;
   }

   @Override
   public void onChangeInventory(AppEngInternalInventory var1, int var2) {
      super.onChangeInventory(var1, var2);
      this.host.returnUnusedBufferToNetwork();
      this.pruneBufferSendList();
   }

   private void pruneBufferSendList() {
      if (!this.bufferSendList.isEmpty()) {
         Set var1 = this.getPatternInputKeys();
         Iterator var2 = this.bufferSendList.iterator();

         while (var2.hasNext()) {
            GenericStack var3 = (GenericStack)var2.next();
            if (!var1.contains(var3.what().dropSecondary())) {
               var2.remove();
               this.host.returnToNetwork(var3.what(), var3.amount());
            }
         }
      }
   }

   @Override
   public void writeToNBT(CompoundTag var1, HolderLookup.Provider var2) {
      super.writeToNBT(var1, var2);
      ListTag var3 = new ListTag();

      for (GenericStack var5 : this.bufferSendList) {
         var3.add(GenericStack.writeTag(var2, var5));
      }

      var1.put("bufferSendList", var3);
   }

   @Override
   public void readFromNBT(CompoundTag var1, HolderLookup.Provider var2) {
      super.readFromNBT(var1, var2);
      this.bufferSendList.clear();
      if (var1.contains("bufferSendList")) {
         ListTag var3 = var1.getList("bufferSendList", 10);

         for (int var4 = 0; var4 < var3.size(); var4++) {
            GenericStack var5 = GenericStack.readTag(var2, var3.getCompound(var4));
            if (var5 != null) {
               this.bufferSendList.add(var5);
            }
         }
      }
   }
}
