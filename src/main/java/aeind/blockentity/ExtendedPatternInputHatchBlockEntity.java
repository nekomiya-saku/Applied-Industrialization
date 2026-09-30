package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEKeyType;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEItems;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.MachineSource;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent.Params;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters.Builder;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aeind.block.ModBlocks;
import aeind.isolation.IsolatedInputProvider;
import aeind.isolation.RoomInputStorage;
import aeind.isolation.ThreadIsolationRoom;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ExtendedPatternInputHatchBlockEntity
   extends HatchBlockEntity
   implements IInWorldGridNodeHost,
   IActionHost,
   PatternProviderLogicHost,
   PatternInputHatchHost,
   IsolatedInputProvider,
   Nameable, aeind.isolation.CatalystStorageHost {
   public static final int PATTERN_SLOTS = 36;
   public static final int SLOTS_PER_ROOM = 9;
   public static final int BUFFER_SLOTS = 324;
   public static final int BUFFER_FLUID_TANKS = 324;
   public static final int UPGRADE_SLOTS = 2;
   public static final long FLUID_CAPACITY = 2147483647L;
   public static final int TICK_RATE = 20;
   public static final int REDSTONE_MODE_IGNORE = 0;
   public static final int REDSTONE_MODE_HIGH = 1;
   public static final int REDSTONE_MODE_LOW = 2;
   private static final int INPUT_STORAGE_VERSION = 1;
   private final ExtendedHatchPatternProviderLogic patternLogic;
   private final IManagedGridNode mainNode;
   private final IUpgradeInventory upgrades;
   private int blockingMode;
   private int redstoneMode;
   @Nullable
   private Component customName;
   private final MIInventory bufferInventory;
   private final RoomInputStorage[] roomStorages;
   private final GenericStackInv catalystStorage;
   private final MachineComponent persistentData;
   private final ContainerData dataAccess;
   private int tickCount;
   private static final IGridNodeListener<ExtendedPatternInputHatchBlockEntity> NODE_LISTENER = new IGridNodeListener<ExtendedPatternInputHatchBlockEntity>() {
      public void onSaveChanges(ExtendedPatternInputHatchBlockEntity var1, IGridNode var2) {
         var1.setChanged();
      }

      public void onStateChanged(ExtendedPatternInputHatchBlockEntity var1, IGridNode var2, State var3) {
         var1.refreshPatterns();
      }
   };

   public ExtendedPatternInputHatchBlockEntity(BlockPos var1, BlockState var2) {
      super(
         new BEP(ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), var1, var2),
         new Builder(ResourceLocation.fromNamespaceAndPath("aeind", "advanced_extended_pattern_input_hatch"), true).build(),
         Params.noFacing(true, false)
      );
      this.mainNode = GridHelper.createManagedNode(this, NODE_LISTENER)
         .setVisualRepresentation(new ItemStack(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()))
         .setInWorldNode(true)
         .setTagName("advanced_extended_pattern_input_hatch_node")
         .setFlags(GridFlags.REQUIRE_CHANNEL)
         .setExposedOnSides(EnumSet.allOf(Direction.class));
      this.upgrades = UpgradeInventories.forMachine(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), 2, this::onUpgradesChanged);
      this.blockingMode = 0;
      this.redstoneMode = 0;
      this.persistentData = new MachineComponent() {
         @Override
         public void writeNbt(CompoundTag var1, HolderLookup.Provider var2x) {
            ExtendedPatternInputHatchBlockEntity.this.mainNode.saveToNBT(var1);
            ExtendedPatternInputHatchBlockEntity.this.patternLogic.writeToNBT(var1, var2x);
            ExtendedPatternInputHatchBlockEntity.this.upgrades.writeToNBT(var1, "upgrades", var2x);
            ExtendedPatternInputHatchBlockEntity.this.writeRoomStorageNbt(var1, var2x);
            var1.putInt("blockingMode", ExtendedPatternInputHatchBlockEntity.this.blockingMode);
            var1.putInt("redstoneMode", ExtendedPatternInputHatchBlockEntity.this.redstoneMode);
            if (ExtendedPatternInputHatchBlockEntity.this.customName != null) {
               var1.putString("customName", ExtendedPatternInputHatchBlockEntity.this.customName.getString());
            }
         }

         @Override
         public void readNbt(CompoundTag var1, HolderLookup.Provider var2x, boolean var3) {
            ExtendedPatternInputHatchBlockEntity.this.mainNode.loadFromNBT(var1);
            ExtendedPatternInputHatchBlockEntity.this.patternLogic.readFromNBT(var1, var2x);
            ExtendedPatternInputHatchBlockEntity.this.upgrades.readFromNBT(var1, "upgrades", var2x);
            ExtendedPatternInputHatchBlockEntity.this.readRoomStorageNbt(var1, var2x);
            if (var1.contains("blockingMode")) {
               ExtendedPatternInputHatchBlockEntity.this.blockingMode = var1.getInt("blockingMode");
            }

            if (var1.contains("redstoneMode")) {
               ExtendedPatternInputHatchBlockEntity.this.redstoneMode = var1.getInt("redstoneMode");
            }

            ExtendedPatternInputHatchBlockEntity.this.customName = var1.contains("customName") ? Component.literal(var1.getString("customName")) : null;
         }

         @Override
         public void writeClientNbt(CompoundTag var1, HolderLookup.Provider var2x) {
            ExtendedPatternInputHatchBlockEntity.this.upgrades.writeToNBT(var1, "upgrades", var2x);
            var1.putInt("blockingMode", ExtendedPatternInputHatchBlockEntity.this.blockingMode);
            var1.putInt("redstoneMode", ExtendedPatternInputHatchBlockEntity.this.redstoneMode);
            if (ExtendedPatternInputHatchBlockEntity.this.customName != null) {
               var1.putString("customName", ExtendedPatternInputHatchBlockEntity.this.customName.getString());
            }
         }

         @Override
         public void readClientNbt(CompoundTag var1, HolderLookup.Provider var2x) {
            ExtendedPatternInputHatchBlockEntity.this.upgrades.readFromNBT(var1, "upgrades", var2x);
            if (var1.contains("blockingMode")) {
               ExtendedPatternInputHatchBlockEntity.this.blockingMode = var1.getInt("blockingMode");
            }

            if (var1.contains("redstoneMode")) {
               ExtendedPatternInputHatchBlockEntity.this.redstoneMode = var1.getInt("redstoneMode");
            }

            ExtendedPatternInputHatchBlockEntity.this.customName = var1.contains("customName") ? Component.literal(var1.getString("customName")) : null;
         }
      };
      this.dataAccess = new ContainerData() {
         @Override
         public int get(int var1) {
            return switch (var1) {
               case 0 -> ExtendedPatternInputHatchBlockEntity.this.blockingMode;
               case 1 -> ExtendedPatternInputHatchBlockEntity.this.redstoneMode;
               case 2 -> ExtendedPatternInputHatchBlockEntity.this.hasRedstoneCard() ? 1 : 0;
               case 3 -> ExtendedPatternInputHatchBlockEntity.this.isRedstonePowered() ? 1 : 0;
               default -> 0;
            };
         }

         @Override
         public void set(int var1, int var2x) {
         }

         @Override
         public int getCount() {
            return 4;
         }
      };
      this.tickCount = 0;
      this.patternLogic = new ExtendedHatchPatternProviderLogic(this.mainNode, this);
      ArrayList<ConfigurableItemStack> var3 = new ArrayList<>(324);

      for (int var4 = 0; var4 < 324; var4++) {
         var3.add(ConfigurableItemStack.standardInputSlot());
      }

      ArrayList<ConfigurableFluidStack> var7 = new ArrayList<>(324);

      for (int var5 = 0; var5 < 324; var5++) {
         var7.add(ConfigurableFluidStack.standardInputSlot(2147483647L));
      }

      SlotPositions var8 = new aztech.modern_industrialization.inventory.SlotPositions.Builder().addSlots(0, 0, 324, 1).build();
      SlotPositions var6 = new aztech.modern_industrialization.inventory.SlotPositions.Builder().addSlots(0, 0, 324, 1).build();
      this.bufferInventory = new MIInventory(var3, var7, var8, var6);
      this.roomStorages = new RoomInputStorage[PATTERN_SLOTS];
      for (int room = 0; room < this.roomStorages.length; room++) {
         this.roomStorages[room] = new RoomInputStorage(this::setChanged);
      }
      this.catalystStorage = new GenericStackInv(
         Set.of(AEKeyType.items(), AEKeyType.fluids()), this::setChanged, GenericStackInv.Mode.STORAGE, 18
      );
      this.registerComponents(this.bufferInventory, this.persistentData);
   }

   @Override
   public GenericStackInv aeind$catalystStorage() {
      return this.catalystStorage;
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      this.mainNode.destroy();
   }

   @Override
   public void onChunkUnloaded() {
      super.onChunkUnloaded();
      this.mainNode.destroy();
   }

   @Override
   public void clearRemoved() {
      super.clearRemoved();
      GridHelper.onFirstTick(this, var0 -> {
         if (var0.getLevel() != null && !var0.isRemoved()) {
            var0.mainNode.create(var0.getLevel(), var0.getBlockPos());
            var0.refreshPatterns();
         }
      });
   }

   public void markDirtyAndSync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Override
   public Component getName() {
      return this.customName != null ? this.customName : Component.translatable("block.aeind.advanced_extended_pattern_input_hatch");
   }

   @Nullable
   @Override
   public Component getCustomName() {
      return this.customName;
   }

   public void setCustomName(@Nullable Component var1) {
      this.customName = var1;
      this.markDirtyAndSync();
      this.refreshPatterns();
   }

   @Override
   public IManagedGridNode getMainNode() {
      return this.mainNode;
   }

   @Nullable
   @Override
   public IGridNode getGridNode(Direction var1) {
      return this.mainNode.getNode();
   }

   @Override
   public AECableType getCableConnectionType(Direction var1) {
      return AECableType.SMART;
   }

   @Nullable
   @Override
   public IGridNode getActionableNode() {
      return this.mainNode.getNode();
   }

   public ExtendedHatchPatternProviderLogic getLogic() {
      return this.patternLogic;
   }

   @Override
   public BlockEntity getBlockEntity() {
      return this;
   }

   @Override
   public EnumSet<Direction> getTargets() {
      return EnumSet.noneOf(Direction.class);
   }

   @Override
   public void saveChanges() {
      this.setChanged();
   }

   @Override
   public AEItemKey getTerminalIcon() {
      return AEItemKey.of(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
   }

   @Override
   public ItemStack getMainMenuIcon() {
      return new ItemStack(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
   }

   public IItemHandler getBufferInventory() {
      return this.bufferInventory.itemStorage.itemHandler;
   }

   public IFluidHandler getFluidHandler() {
      return this.bufferInventory.fluidStorage.fluidHandler;
   }

   public MIInventory getBuffer() {
      return this.bufferInventory;
   }

   public ExtendedHatchPatternProviderLogic getPatternLogic() {
      return this.patternLogic;
   }

   @Override
   public HatchType getHatchType() {
      return HatchTypes.ITEM_INPUT;
   }

   @Override
   public boolean upgradesToSteel() {
      return false;
   }

   @Override
   public MIInventory getInventory() {
      return this.bufferInventory;
   }

   @Override
   public MachineModelClientData getMachineModelData() {
      return super.getMachineModelData();
   }

   @Override
   public void appendItemInputs(List<ConfigurableItemStack> var1) {
      var1.addAll(this.bufferInventory.getItemStacks());
   }

   @Override
   public void appendFluidInputs(List<ConfigurableFluidStack> var1) {
      var1.addAll(this.bufferInventory.getFluidStacks());
   }

   public IUpgradeInventory getUpgradeInventory() {
      return this.upgrades;
   }

   public boolean hasRedstoneCard() {
      return this.upgrades.getInstalledUpgrades(AEItems.REDSTONE_CARD) > 0;
   }

   public boolean isRedstonePowered() {
      return this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;
   }

   public boolean passesRedstone() {
      if (!this.hasRedstoneCard()) {
         return true;
      }

      boolean var1 = this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;

      return switch (this.redstoneMode) {
         case 1 -> var1;
         case 2 -> !var1;
         default -> true;
      };
   }

   @Override
   public boolean canAcceptOrder() {
      if (!this.passesRedstone()) {
         return false;
      }

      boolean var1 = this.patternLogic.getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.blockingMode != 0;
      return !var1 || !this.bufferHasContent();
   }

   public boolean canAcceptOrder(int var1) {
      if (!this.passesRedstone()) {
         return false;
      }

      boolean var2 = this.patternLogic.getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.blockingMode != 0;
      return !var2 || !this.roomHasContent(var1);
   }

   public boolean roomHasContent(int var1) {
      return var1 >= 0 && var1 < this.roomStorages.length && !this.roomStorages[var1].isEmpty();
   }

   public long insertBuffer(int var1, AEKey var2, long var3, Actionable var5) {
      if (var1 >= 0 && var1 < 36 && var3 > 0L) {
         return var2 instanceof AEItemKey || var2 instanceof AEFluidKey ? this.roomStorages[var1].insert(var2, var3, var5) : 0L;
      } else {
         return 0L;
      }
   }

   private boolean bufferHasContent() {
      for (ConfigurableItemStack var2 : this.bufferInventory.getItemStacks()) {
         if (!var2.isEmpty()) {
            return true;
         }
      }

      return this.bufferInventory.getFluidStacks().stream().anyMatch(var0 -> !var0.isEmpty());
   }

   public boolean hasStoredMaterials() {
      return this.bufferHasContent();
   }

   public int getBlockingMode() {
      return this.blockingMode;
   }

   public void setBlockingMode(int var1) {
      this.blockingMode = var1;
      this.markDirtyAndSync();
   }

   public int getRedstoneMode() {
      return this.redstoneMode;
   }

   public void setRedstoneMode(int var1) {
      this.redstoneMode = Math.floorMod(var1, 3);
      this.markDirtyAndSync();
   }

   public void onUpgradesChanged() {
      int var1 = this.upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD);
      long var2 = Math.min(2147483647L * (1 << Math.min(var1, 2)), 2147483647L);

      for (ConfigurableFluidStack var5 : this.bufferInventory.getFluidStacks()) {
         var5.setCapacity(var2);
      }

      this.markDirtyAndSync();
   }

   public long countInBuffer(AEKey var1) {
      if (var1 instanceof AEItemKey var8) {
         long var9 = 0L;
         IItemHandler var10 = this.bufferInventory.itemStorage.itemHandler;

         for (int var11 = 0; var11 < var10.getSlots(); var11++) {
            ItemStack var12 = var10.getStackInSlot(var11);
            if (!var12.isEmpty() && var8.matches(var12)) {
               var9 += var12.getCount();
            }
         }

         return var9;
      } else if (var1 instanceof AEFluidKey var2) {
         long var3 = 0L;
         IFluidHandler var5 = this.bufferInventory.fluidStorage.fluidHandler;

         for (int var6 = 0; var6 < var5.getTanks(); var6++) {
            FluidStack var7 = var5.getFluidInTank(var6);
            if (var2.matches(var7)) {
               var3 += var7.getAmount();
            }
         }

         return var3;
      } else {
         return 0L;
      }
   }

   @Override
   public long insertBuffer(AEKey var1, long var2, Actionable var4) {
      if (var2 <= 0L) {
         return 0L;
      } else if (var1 instanceof AEItemKey var6) {
         return this.insertItems(var6, var2, var4);
      } else {
         return var1 instanceof AEFluidKey var5 ? this.insertFluid(var5, var2, var4 != Actionable.MODULATE) : 0L;
      }
   }

   private long insertFluid(AEFluidKey var1, long var2, boolean var4) {
      return insertFluid(this.bufferInventory.getFluidStacks(), var1, var2, var4);
   }

   private static long insertFluid(List<ConfigurableFluidStack> var0, AEFluidKey var1, long var2, boolean var4) {
      long var5 = Math.min(var2, 2147483647L);
      if (var5 <= 0L) {
         return 0L;
      }

      for (ConfigurableFluidStack var8 : var0) {
         if (!var8.isEmpty() && var8.getResource().getFluid() == var1.getFluid()) {
            long var9 = var8.getCapacity() - var8.getAmount();
            if (var9 <= 0L) {
               return 0L;
            }

            long var11 = Math.min(var5, var9);
            if (!var4) {
               var8.increment(var11);
            }

            return var11;
         }
      }

      for (ConfigurableFluidStack var14 : var0) {
         if (var14.isEmpty()) {
            long var15 = Math.min(var5, var14.getCapacity());
            if (!var4) {
               var14.setKey(FluidVariant.of(var1.getFluid()));
               var14.increment(var15);
            }

            return var15;
         }
      }

      return 0L;
   }

   private long insertItems(AEItemKey var1, long var2, Actionable var4) {
      return insertItems(this.bufferInventory.getItemStacks(), var1, var2, var4);
   }

   private static long insertItems(List<ConfigurableItemStack> var0, AEItemKey var1, long var2, Actionable var4) {
      if (var2 <= 0L) {
         return 0L;
      }

      ItemVariant var5 = ItemVariant.of(var1.toStack(1));
      long var6 = 0L;
      boolean var8 = var4 != Actionable.MODULATE;

      for (ConfigurableItemStack var10 : var0) {
         if (var6 >= var2) {
            break;
         }

         if (!var10.isEmpty() ? var10.getResource().equals(var5) : var10.isResourceAllowedByLock(var1.getItem())) {
            long var13 = 2147483647L - var10.getAmount();
            long var11;
            if (var13 > 0L && (var11 = Math.min(var2 - var6, var13)) > 0L) {
               if (!var8) {
                  var10.setKey(var5);
                  var10.increment(var11);
               }

               var6 += var11;
            }
         }
      }

      return var6;
   }

   public long extractBuffer(AEKey var1, long var2, Actionable var4) {
      if (var2 <= 0L) {
         return 0L;
      }

      if (!(var1 instanceof AEItemKey var5)) {
         if (var1 instanceof AEFluidKey var14) {
            long var15 = Math.min(var2, 2147483647L);
            FluidStack var16 = this.bufferInventory
               .fluidStorage
               .fluidHandler
               .drain(var14.toStack((int)var15), var4 == Actionable.MODULATE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            return var16.getAmount();
         } else {
            return 0L;
         }
      } else {
         long var6 = 0L;
         boolean var8 = var4 != Actionable.MODULATE;
         IItemHandler var9 = this.bufferInventory.itemStorage.itemHandler;

         for (int var10 = 0; var10 < var9.getSlots() && var6 < var2; var10++) {
            ItemStack var11 = var9.getStackInSlot(var10);
            if (!var11.isEmpty() && var5.matches(var11)) {
               int var12 = (int)Math.min(var2 - var6, var11.getCount());
               ItemStack var13 = var9.extractItem(var10, var12, var8);
               var6 += var13.getCount();
            }
         }

         return var6;
      }
   }

   @Override
   public boolean returnAllBufferToNetwork() {
      return this.returnBufferToNetwork(var0 -> true) | this.flushCatalystsToNetwork();
   }

   public void returnRoomToNetwork(int var1) {
      if (this.level != null && !this.level.isClientSide && var1 >= 0 && var1 < 36) {
         IGridNode var2 = this.mainNode.getNode();
         if (var2 != null && var2.isActive()) {
            MEStorage var3 = var2.getGrid().getStorageService().getInventory();
            MachineSource var4 = new MachineSource(this);
            this.roomStorages[var1].flushTo(var3, var4);
         }
      }
   }

   @Override
   public List<ThreadIsolationRoom> aeind$isolatedInputRooms() {
      ArrayList<ThreadIsolationRoom> var1 = new ArrayList<>(36);

      for (int var2 = 0; var2 < 36; var2++) {
         var1.add(new ThreadIsolationRoom("advanced-extended:" + this.getBlockPos().asLong() + ":" + var2, this.roomStorages[var2], this.catalystStorage));
      }

      return var1;
   }

   @Override
   public boolean returnUnusedBufferToNetwork() {
      Set<AEKey> var1 = this.patternLogic.getPatternInputKeys();
      return this.returnBufferToNetwork(var1x -> !var1.contains(var1x.dropSecondary()));
   }

   private boolean returnBufferToNetwork(Predicate<AEKey> var1) {
      if (this.level != null && !this.level.isClientSide) {
         IGridNode var4 = this.mainNode.getNode();
         if (var4 != null && var4.isActive()) {
            MEStorage var5 = var4.getGrid().getStorageService().getInventory();
            MachineSource var6 = new MachineSource(this);
            boolean var7 = false;

            for (ConfigurableItemStack var9 : this.bufferInventory.getItemStacks()) {
               AEItemKey var10 = AEItemKey.of(var9.toStack());
               long var2;
               if (!var9.isEmpty() && var1.test(var10) && (var2 = var5.insert(var10, var9.getAmount(), Actionable.MODULATE, var6)) > 0L) {
                  var9.decrement(var2);
                  var7 = true;
               }
            }

            for (ConfigurableFluidStack var13 : this.bufferInventory.getFluidStacks()) {
               if (!var13.isEmpty()) {
                  AEFluidKey var14 = AEFluidKey.of(var13.getResource().getFluid());
                  long var11;
                  if (var1.test(var14) && (var11 = var5.insert(var14, var13.getAmount(), Actionable.MODULATE, var6)) > 0L) {
                     var13.decrement(var11);
                     var7 = true;
                  }
               }
            }

            for (RoomInputStorage roomStorage : this.roomStorages) {
               var7 |= roomStorage.flushTo(var5, var6, var1);
            }

            if (var7) {
               this.setChanged();
            }

            return var7;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean flushCatalystsToNetwork() {
      if (this.level == null || this.level.isClientSide || this.mainNode.getNode() == null || !this.mainNode.getNode().isActive()) return false;
      MEStorage network = this.mainNode.getNode().getGrid().getStorageService().getInventory();
      boolean changed = false;
      for (var stack : this.catalystStorage.toList()) {
         long inserted = network.insert(stack.what(), stack.amount(), Actionable.MODULATE, new MachineSource(this));
         if (inserted > 0L) {
            this.catalystStorage.extract(stack.what(), inserted, Actionable.MODULATE, new MachineSource(this));
            changed = true;
         }
      }
      return changed;
   }

   @Override
   public void returnToNetwork(AEKey var1, long var2) {
      if (this.level != null && !this.level.isClientSide && var2 > 0L) {
         IGridNode var4 = this.mainNode.getNode();
         if (var4 != null && var4.isActive()) {
            var4.getGrid().getStorageService().getInventory().insert(var1, var2, Actionable.MODULATE, new MachineSource(this));
         }
      }
   }

   public void refreshPatterns() {
      this.patternLogic.updatePatterns();
   }

   public static void serverTick(Level var0, BlockPos var1, BlockState var2, ExtendedPatternInputHatchBlockEntity var3) {
      if (!var0.isClientSide) {
         var3.tick();
         var3.tickCount++;
         if (var3.tickCount % 20 == 0) {
            var3.doWork();
         }
      }
   }

   private void doWork() {
      if (this.level != null && !this.level.isClientSide) {
         boolean var1 = false;
         if (var1 | this.patternLogic.flushBufferSendList()) {
            this.setChanged();
         }
      }
   }

   public NonNullList<ItemStack> getDropItems() {
      NonNullList<ItemStack> var1 = NonNullList.create();
      this.patternLogic.addDrops(var1);

      for (int var2 = 0; var2 < this.upgrades.size(); var2++) {
         ItemStack var3 = this.upgrades.getStackInSlot(var2);
         if (!var3.isEmpty()) {
            var1.add(var3.copy());
         }
      }

      for (ConfigurableItemStack var9 : this.bufferInventory.getItemStacks()) {
         if (!var9.isEmpty()) {
            ItemStack var4 = var9.toStack();
            int var5 = Math.max(1, var4.getMaxStackSize());

            while (!var4.isEmpty()) {
               int var6 = Math.min(var4.getCount(), var5);
               ItemStack var7 = var4.copy();
               var7.setCount(var6);
               var1.add(var7);
               var4.shrink(var6);
            }
         }
      }

      for (RoomInputStorage roomStorage : this.roomStorages) {
         roomStorage.addItemDrops(var1);
      }
      if (this.level != null) {
         for (var stack : this.catalystStorage.toList()) {
            stack.what().addDrops(stack.amount(), var1, this.level, this.getBlockPos());
         }
      }

      return var1;
   }

   private void writeRoomStorageNbt(CompoundTag tag, HolderLookup.Provider provider) {
      tag.putInt("aeindInputStorageVersion", INPUT_STORAGE_VERSION);
      ListTag rooms = new ListTag();
      for (RoomInputStorage roomStorage : this.roomStorages) {
         rooms.add(roomStorage.writeNbt(provider));
      }
      tag.put("aeindInputRooms", rooms);
      tag.put("aeindCatalysts", this.catalystStorage.writeToTag(provider));
   }

   private void readRoomStorageNbt(CompoundTag tag, HolderLookup.Provider provider) {
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

   private void migrateLegacyRoom(int room) {
      int start = room * SLOTS_PER_ROOM;
      int end = start + SLOTS_PER_ROOM;
      List<ConfigurableItemStack> items = this.bufferInventory.getItemStacks().subList(start, end);
      List<ConfigurableFluidStack> fluids = this.bufferInventory.getFluidStacks().subList(start, end);
      this.roomStorages[room].importLegacy(items, fluids);
      for (ConfigurableItemStack stack : items) {
         stack.decrement(stack.getAmount());
      }
      for (ConfigurableFluidStack stack : fluids) {
         stack.decrement(stack.getAmount());
      }
   }

   public ContainerData getContainerData() {
      return this.dataAccess;
   }
}
