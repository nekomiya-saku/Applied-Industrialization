package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEKeyType;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.MachineSource;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aeind.block.ModBlocks;
import aeind.compat.AEKeyTypeSupport;
import aeind.isolation.IsolatedInputProvider;
import aeind.isolation.RoomInputStorage;
import aeind.isolation.ThreadIsolationRoom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedPatternInputHatchBlockEntity extends MEPatternInputHatchBlockEntity implements IsolatedInputProvider, aeind.isolation.CatalystStorageHost {
   public static final int PATTERN_SLOTS = 9;
   public static final int SLOTS_PER_ROOM = 9;
   private static final int TOTAL_SLOTS = 81;
   private static final int INPUT_STORAGE_VERSION = 1;
   private final RoomInputStorage[] roomStorages;
   private final GenericStackInv catalystStorage;

   public AdvancedPatternInputHatchBlockEntity(BlockPos var1, BlockState var2) {
      super(var1, var2, ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), 81, true);
      this.roomStorages = new RoomInputStorage[PATTERN_SLOTS];
      for (int room = 0; room < this.roomStorages.length; room++) {
         this.roomStorages[room] = new RoomInputStorage(this::setChanged);
      }
      this.catalystStorage = new GenericStackInv(
         AEKeyTypeSupport.registeredTypes(), this::setChanged, GenericStackInv.Mode.STORAGE, 18
      );
   }

   @Override
   public GenericStackInv aeind$catalystStorage() {
      return this.catalystStorage;
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
         return this.roomStorages[var1].insert(var2, var3, var5);
      } else {
         return 0L;
      }
   }

   public boolean roomHasContent(int var1) {
      return var1 >= 0 && var1 < this.roomStorages.length && !this.roomStorages[var1].isEmpty();
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
            this.roomStorages[var1].flushTo(var3, new MachineSource(this));
         }
      }
   }

   @Override
   public List<ThreadIsolationRoom> aeind$isolatedInputRooms() {
      ArrayList<ThreadIsolationRoom> var1 = new ArrayList<>(9);

      for (int var2 = 0; var2 < 9; var2++) {
         var1.add(new ThreadIsolationRoom("pattern:" + this.getBlockPos().asLong() + ":" + var2, this.roomStorages[var2], this.catalystStorage));
      }

      return var1;
   }

   @Override
   protected void writeRoomStorageNbt(CompoundTag tag, HolderLookup.Provider provider) {
      tag.putInt("aeindInputStorageVersion", INPUT_STORAGE_VERSION);
      ListTag rooms = new ListTag();
      for (RoomInputStorage roomStorage : this.roomStorages) {
         rooms.add(roomStorage.writeNbt(provider));
      }
      tag.put("aeindInputRooms", rooms);
      tag.put("aeindCatalysts", this.catalystStorage.writeToTag(provider));
   }

   @Override
   protected void readRoomStorageNbt(CompoundTag tag, HolderLookup.Provider provider) {
      for (RoomInputStorage roomStorage : this.roomStorages) {
         roomStorage.clear();
      }
      if (tag.getInt("aeindInputStorageVersion") >= INPUT_STORAGE_VERSION && tag.contains("aeindInputRooms")) {
         ListTag rooms = tag.getList("aeindInputRooms", 9);
         for (int room = 0; room < this.roomStorages.length; room++) {
            if (room < rooms.size()) {
               this.roomStorages[room].readNbt(rooms.getList(room), provider);
            } else {
               this.migrateLegacyRoom(room);
            }
         }
      } else {
         for (int room = 0; room < this.roomStorages.length; room++) {
            this.migrateLegacyRoom(room);
         }
      }
      this.catalystStorage.clear();
      if (tag.contains("aeindCatalysts")) {
         this.catalystStorage.readFromTag(tag.getList("aeindCatalysts", 10), provider);
      }
   }

   @Override
   protected void addRoomStorageDrops(List<ItemStack> drops) {
      for (RoomInputStorage roomStorage : this.roomStorages) {
         if (this.getLevel() != null) {
            roomStorage.addDrops(drops, this.getLevel(), this.getBlockPos());
         }
      }
      if (this.getLevel() != null) {
         for (var stack : this.catalystStorage.toList()) {
            if (stack != null) {
               stack.what().addDrops(stack.amount(), drops, this.getLevel(), this.getBlockPos());
            }
         }
      }
   }

   @Override
   public boolean returnAllBufferToNetwork() {
      return super.returnAllBufferToNetwork() | this.flushRoomStorages(key -> true) | this.flushCatalystsToNetwork();
   }

   @Override
   public boolean returnUnusedBufferToNetwork() {
      Set<AEKey> patternInputs = this.getPatternLogic().getPatternInputKeys();
      return super.returnUnusedBufferToNetwork()
         | this.flushRoomStorages(key -> !patternInputs.contains(key.dropSecondary()));
   }

   private boolean flushRoomStorages(Predicate<AEKey> filter) {
      if (this.getLevel() == null || this.getLevel().isClientSide) {
         return false;
      }
      IManagedGridNode node = this.getMainNode();
      if (node.getNode() == null || !node.getNode().isActive()) {
         return false;
      }
      MEStorage network = node.getNode().getGrid().getStorageService().getInventory();
      MachineSource source = new MachineSource(this);
      boolean changed = false;
      for (RoomInputStorage roomStorage : this.roomStorages) {
         changed |= roomStorage.flushTo(network, source, filter);
      }
      return changed;
   }

   private boolean flushCatalystsToNetwork() {
      if (this.getLevel() == null || this.getLevel().isClientSide || this.getMainNode().getNode() == null || !this.getMainNode().getNode().isActive()) return false;
      MEStorage network = this.getMainNode().getNode().getGrid().getStorageService().getInventory();
      boolean changed = false;
      for (var stack : this.catalystStorage.toList()) {
         if (stack == null) {
            continue;
         }
         long inserted = network.insert(stack.what(), stack.amount(), Actionable.MODULATE, new MachineSource(this));
         if (inserted > 0L) {
            this.catalystStorage.extract(stack.what(), inserted, Actionable.MODULATE, new MachineSource(this));
            changed = true;
         }
      }
      return changed;
   }

   private void migrateLegacyRoom(int room) {
      int start = room * SLOTS_PER_ROOM;
      int end = start + SLOTS_PER_ROOM;
      List<ConfigurableItemStack> items = this.getBuffer().getItemStacks().subList(start, end);
      List<ConfigurableFluidStack> fluids = this.getBuffer().getFluidStacks().subList(start, end);
      this.roomStorages[room].importLegacy(items, fluids);
      for (ConfigurableItemStack stack : items) {
         stack.decrement(stack.getAmount());
      }
      for (ConfigurableFluidStack stack : fluids) {
         stack.decrement(stack.getAmount());
      }
   }
}
