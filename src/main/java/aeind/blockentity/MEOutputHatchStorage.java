package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class MEOutputHatchStorage implements MEStorage {
   private final MEOutputHatchBlockEntity host;

   public MEOutputHatchStorage(MEOutputHatchBlockEntity var1) {
      this.host = var1;
   }

   @Override
   public long insert(AEKey var1, long var2, Actionable var4, IActionSource var5) {
      return 0L;
   }

   @Override
   public long extract(AEKey var1, long var2, Actionable var4, IActionSource var5) {
      if (var2 <= 0L) {
         return 0L;
      }

      long buffered = this.host.getOutputBuffer().extract(var1, var2, var4);
      if (buffered >= var2) {
         return buffered;
      }

      long remaining = var2 - buffered;

      if (!(var1 instanceof AEItemKey var6)) {
         if (var1 instanceof AEFluidKey var15) {
            long var16 = Math.min(remaining, 2147483647L);
            FluidStack var17 = this.host
               .getFluidHandler()
               .drain(var15.toStack((int)var16), var4 == Actionable.MODULATE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            return buffered + var17.getAmount();
         } else {
            return buffered;
         }
      } else {
         long var7 = buffered;
         boolean var9 = var4 != Actionable.MODULATE;
         IItemHandler var10 = this.host.getBufferInventory();

         for (int var11 = 0; var11 < var10.getSlots() && var7 < var2; var11++) {
            ItemStack var12 = var10.getStackInSlot(var11);
            if (!var12.isEmpty() && var6.matches(var12)) {
               int var13 = (int)Math.min(var2 - var7, var12.getCount());
               ItemStack var14 = var10.extractItem(var11, var13, var9);
               var7 += var14.getCount();
            }
         }

         return var7;
      }
   }

   @Override
   public void getAvailableStacks(KeyCounter var1) {
      this.host.getOutputBuffer().getAvailableStacks(var1);
      IItemHandler var2 = this.host.getBufferInventory();

      for (int var3 = 0; var3 < var2.getSlots(); var3++) {
         ItemStack var4 = var2.getStackInSlot(var3);
         if (!var4.isEmpty()) {
            var1.add(AEItemKey.of(var4), var4.getCount());
         }
      }

      IFluidHandler var6 = this.host.getFluidHandler();

      for (int var7 = 0; var7 < var6.getTanks(); var7++) {
         FluidStack var5 = var6.getFluidInTank(var7);
         if (!var5.isEmpty()) {
            var1.add(AEFluidKey.of(var5), var5.getAmount());
         }
      }
   }

   @Override
   public Component getDescription() {
      return Component.literal("ME Output Hatch Buffer");
   }
}
