package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aeind.block.ModBlocks;
import aeind.isolation.IsolatedInputProvider;
import aeind.isolation.ThreadIsolationRoom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedPatternInputHatchBlockEntity extends MEPatternInputHatchBlockEntity implements IsolatedInputProvider {
   public static final int PATTERN_SLOTS = 9;
   public static final int SLOTS_PER_ROOM = 9;
   private static final int TOTAL_SLOTS = 81;

   public AdvancedPatternInputHatchBlockEntity(BlockPos var1, BlockState var2) {
      super(var1, var2, ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), 81, true);
   }

   @Override
   public Component getName() {
      return this.getCustomName() != null ? this.getCustomName() : Component.translatable("block.aeind.advanced_pattern_input_hatch");
   }

   @Override
   public ItemStack getMainMenuIcon() {
      return new ItemStack(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
   }

   @Override
   public AEItemKey getTerminalIcon() {
      return AEItemKey.of(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get().asItem());
   }

   public AdvancedHatchPatternProviderLogic getAdvancedPatternLogic() {
      return (AdvancedHatchPatternProviderLogic)this.getPatternLogic();
   }

   public long insertBuffer(int var1, AEKey var2, long var3, Actionable var5) {
      if (var1 >= 0 && var1 < 9 && var3 > 0L) {
         int var6 = var1 * 9;
         int var7 = var6 + 9;
         if (var2 instanceof AEItemKey var9) {
            return insertItemsInto(this.getBuffer().getItemStacks().subList(var6, var7), var9, var3, var5);
         } else {
            return var2 instanceof AEFluidKey var8
               ? insertFluidInto(this.getBuffer().getFluidStacks().subList(var6, var7), var8, var3, var5 != Actionable.MODULATE)
               : 0L;
         }
      } else {
         return 0L;
      }
   }

   public boolean roomHasContent(int var1) {
      int var2 = var1 * 9;
      int var3 = var2 + 9;

      for (ConfigurableItemStack var5 : this.getBuffer().getItemStacks().subList(var2, var3)) {
         if (!var5.isEmpty()) {
            return true;
         }
      }

      for (ConfigurableFluidStack var7 : this.getBuffer().getFluidStacks().subList(var2, var3)) {
         if (!var7.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public boolean canAcceptOrder(int var1) {
      if (!this.passesRedstone()) {
         return false;
      }

      boolean var2 = this.getAdvancedPatternLogic().getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.getBlockingMode() != 0;
      return !var2 || !this.roomHasContent(var1);
   }

   public void returnRoomToNetwork(int var1) {
      if (this.getLevel() != null && !this.getLevel().isClientSide && var1 >= 0 && var1 < 9) {
         IManagedGridNode var2 = this.getMainNode();
         if (var2.getNode() != null && var2.getNode().isActive()) {
            MEStorage var3 = var2.getNode().getGrid().getStorageService().getInventory();
            int var4 = var1 * 9;
            int var5 = var4 + 9;

            for (ConfigurableItemStack var7 : this.getBuffer().getItemStacks().subList(var4, var5)) {
               if (!var7.isEmpty()) {
                  long var8 = var3.insert(AEItemKey.of(var7.toStack()), var7.getAmount(), Actionable.MODULATE, new MachineSource(this));
                  var7.decrement(var8);
               }
            }

            for (ConfigurableFluidStack var12 : this.getBuffer().getFluidStacks().subList(var4, var5)) {
               if (!var12.isEmpty()) {
                  AEFluidKey var13 = AEFluidKey.of(var12.getResource().getFluid());
                  long var9 = var3.insert(var13, var12.getAmount(), Actionable.MODULATE, new MachineSource(this));
                  var12.decrement(var9);
               }
            }

            this.setChanged();
         }
      }
   }

   @Override
   public List<ThreadIsolationRoom> aeind$isolatedInputRooms() {
      ArrayList<ThreadIsolationRoom> var1 = new ArrayList<>(9);

      for (int var2 = 0; var2 < 9; var2++) {
         int var3 = var2 * 9;
         int var4 = var3 + 9;
         var1.add(
            new ThreadIsolationRoom(
               "pattern:" + this.getBlockPos().asLong() + ":" + var2,
               this.getBuffer().getItemStacks().subList(var3, var4),
               this.getBuffer().getFluidStacks().subList(var3, var4)
            )
         );
      }

      return var1;
   }
}
