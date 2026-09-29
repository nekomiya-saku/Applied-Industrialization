/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.config.Settings
 *  appeng.api.config.YesNo
 *  appeng.api.networking.GridFlags
 *  appeng.api.networking.GridHelper
 *  appeng.api.networking.IGridNode
 *  appeng.api.networking.IGridNodeListener
 *  appeng.api.networking.IGridNodeListener$State
 *  appeng.api.networking.IInWorldGridNodeHost
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.security.IActionHost
 *  appeng.api.networking.security.IActionSource
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.api.storage.MEStorage
 *  appeng.api.upgrades.IUpgradeInventory
 *  appeng.api.upgrades.UpgradeInventories
 *  appeng.api.util.AECableType
 *  appeng.core.definitions.AEItems
 *  appeng.helpers.patternprovider.PatternProviderLogicHost
 *  appeng.me.helpers.MachineSource
 *  aztech.modern_industrialization.inventory.ConfigurableFluidStack
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 *  aztech.modern_industrialization.inventory.MIInventory
 *  aztech.modern_industrialization.inventory.SlotPositions
 *  aztech.modern_industrialization.inventory.SlotPositions$Builder
 *  aztech.modern_industrialization.machines.BEP
 *  aztech.modern_industrialization.machines.MachineComponent
 *  aztech.modern_industrialization.machines.components.OrientationComponent$Params
 *  aztech.modern_industrialization.machines.gui.MachineGuiParameters$Builder
 *  aztech.modern_industrialization.machines.models.MachineModelClientData
 *  aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity
 *  aztech.modern_industrialization.machines.multiblocks.HatchType
 *  aztech.modern_industrialization.machines.multiblocks.HatchTypes
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.inventory.ContainerData
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.Nullable
 */
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
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
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
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aeind.block.ModBlocks;
import aeind.blockentity.ExtendedHatchPatternProviderLogic;
import aeind.blockentity.ModBlockEntities;
import aeind.blockentity.PatternInputHatchHost;
import aeind.isolation.IsolatedInputProvider;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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
Nameable {
    public static final int PATTERN_SLOTS = 36;
    public static final int SLOTS_PER_ROOM = 9;
    public static final int BUFFER_SLOTS = 324;
    public static final int BUFFER_FLUID_TANKS = 324;
    public static final int UPGRADE_SLOTS = 2;
    public static final long FLUID_CAPACITY = Long.MAX_VALUE;
    public static final int TICK_RATE = 20;
    public static final int REDSTONE_MODE_IGNORE = 0;
    public static final int REDSTONE_MODE_HIGH = 1;
    public static final int REDSTONE_MODE_LOW = 2;
    private final ExtendedHatchPatternProviderLogic patternLogic;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode((Object)this, NODE_LISTENER).setVisualRepresentation(new ItemStack((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get())).setInWorldNode(true).setTagName("advanced_extended_pattern_input_hatch_node").setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL}).setExposedOnSides(EnumSet.allOf(Direction.class));
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine((ItemLike)((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()), (int)2, this::onUpgradesChanged);
    private int blockingMode = 0;
    private int redstoneMode = 0;
    @Nullable
    private Component customName;
    private final MIInventory bufferInventory;
    private final MachineComponent persistentData = new MachineComponent(){

        public void writeNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
            ExtendedPatternInputHatchBlockEntity.this.mainNode.saveToNBT(compoundTag);
            ExtendedPatternInputHatchBlockEntity.this.patternLogic.writeToNBT(compoundTag, provider);
            ExtendedPatternInputHatchBlockEntity.this.upgrades.writeToNBT(compoundTag, "upgrades", provider);
            compoundTag.putInt("blockingMode", ExtendedPatternInputHatchBlockEntity.this.blockingMode);
            compoundTag.putInt("redstoneMode", ExtendedPatternInputHatchBlockEntity.this.redstoneMode);
            if (ExtendedPatternInputHatchBlockEntity.this.customName != null) {
                compoundTag.putString("customName", ExtendedPatternInputHatchBlockEntity.this.customName.getString());
            }
        }

        public void readNbt(CompoundTag compoundTag, HolderLookup.Provider provider, boolean bl) {
            ExtendedPatternInputHatchBlockEntity.this.mainNode.loadFromNBT(compoundTag);
            ExtendedPatternInputHatchBlockEntity.this.patternLogic.readFromNBT(compoundTag, provider);
            ExtendedPatternInputHatchBlockEntity.this.upgrades.readFromNBT(compoundTag, "upgrades", provider);
            if (compoundTag.contains("blockingMode")) {
                ExtendedPatternInputHatchBlockEntity.this.blockingMode = compoundTag.getInt("blockingMode");
            }
            if (compoundTag.contains("redstoneMode")) {
                ExtendedPatternInputHatchBlockEntity.this.redstoneMode = compoundTag.getInt("redstoneMode");
            }
            ExtendedPatternInputHatchBlockEntity.this.customName = compoundTag.contains("customName") ? Component.literal((String)compoundTag.getString("customName")) : null;
        }

        public void writeClientNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
            ExtendedPatternInputHatchBlockEntity.this.upgrades.writeToNBT(compoundTag, "upgrades", provider);
            compoundTag.putInt("blockingMode", ExtendedPatternInputHatchBlockEntity.this.blockingMode);
            compoundTag.putInt("redstoneMode", ExtendedPatternInputHatchBlockEntity.this.redstoneMode);
            if (ExtendedPatternInputHatchBlockEntity.this.customName != null) {
                compoundTag.putString("customName", ExtendedPatternInputHatchBlockEntity.this.customName.getString());
            }
        }

        public void readClientNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
            ExtendedPatternInputHatchBlockEntity.this.upgrades.readFromNBT(compoundTag, "upgrades", provider);
            if (compoundTag.contains("blockingMode")) {
                ExtendedPatternInputHatchBlockEntity.this.blockingMode = compoundTag.getInt("blockingMode");
            }
            if (compoundTag.contains("redstoneMode")) {
                ExtendedPatternInputHatchBlockEntity.this.redstoneMode = compoundTag.getInt("redstoneMode");
            }
            ExtendedPatternInputHatchBlockEntity.this.customName = compoundTag.contains("customName") ? Component.literal((String)compoundTag.getString("customName")) : null;
        }
    };
    private final ContainerData dataAccess = new ContainerData(){

        public int get(int n) {
            return switch (n) {
                case 0 -> ExtendedPatternInputHatchBlockEntity.this.blockingMode;
                case 1 -> ExtendedPatternInputHatchBlockEntity.this.redstoneMode;
                case 2 -> {
                    if (ExtendedPatternInputHatchBlockEntity.this.hasRedstoneCard()) {
                        yield 1;
                    }
                    yield 0;
                }
                case 3 -> {
                    if (ExtendedPatternInputHatchBlockEntity.this.isRedstonePowered()) {
                        yield 1;
                    }
                    yield 0;
                }
                default -> 0;
            };
        }

        public void set(int n, int n2) {
        }

        public int getCount() {
            return 4;
        }
    };
    private int tickCount = 0;
    private static final IGridNodeListener<ExtendedPatternInputHatchBlockEntity> NODE_LISTENER = new IGridNodeListener<ExtendedPatternInputHatchBlockEntity>(){

        public void onSaveChanges(ExtendedPatternInputHatchBlockEntity extendedPatternInputHatchBlockEntity, IGridNode iGridNode) {
            extendedPatternInputHatchBlockEntity.setChanged();
        }

        public void onStateChanged(ExtendedPatternInputHatchBlockEntity extendedPatternInputHatchBlockEntity, IGridNode iGridNode, IGridNodeListener.State state) {
            extendedPatternInputHatchBlockEntity.refreshPatterns();
        }
    };

    public ExtendedPatternInputHatchBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(new BEP((BlockEntityType)ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), blockPos, blockState), new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"advanced_extended_pattern_input_hatch"), true).build(), OrientationComponent.Params.noFacing((boolean)true, (boolean)false));
        this.patternLogic = new ExtendedHatchPatternProviderLogic(this.mainNode, this);
        ArrayList<ConfigurableItemStack> arrayList = new ArrayList<ConfigurableItemStack>(324);
        for (int i = 0; i < 324; ++i) {
            arrayList.add(ConfigurableItemStack.standardInputSlot());
        }
        ArrayList<ConfigurableFluidStack> arrayList2 = new ArrayList<ConfigurableFluidStack>(324);
        for (int i = 0; i < 324; ++i) {
            arrayList2.add(ConfigurableFluidStack.standardInputSlot(Long.MAX_VALUE));
        }
        SlotPositions slotPositions = new SlotPositions.Builder().addSlots(0, 0, 324, 1).build();
        SlotPositions slotPositions2 = new SlotPositions.Builder().addSlots(0, 0, 324, 1).build();
        this.bufferInventory = new MIInventory(arrayList, arrayList2, slotPositions, slotPositions2);
        this.registerComponents(new MachineComponent[]{this.bufferInventory, this.persistentData});
    }

    public void setRemoved() {
        super.setRemoved();
        this.mainNode.destroy();
    }

    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.mainNode.destroy();
    }

    public void clearRemoved() {
        super.clearRemoved();
        GridHelper.onFirstTick((BlockEntity)this, extendedPatternInputHatchBlockEntity -> {
            if (extendedPatternInputHatchBlockEntity.getLevel() == null || extendedPatternInputHatchBlockEntity.isRemoved()) {
                return;
            }
            extendedPatternInputHatchBlockEntity.mainNode.create(extendedPatternInputHatchBlockEntity.getLevel(), extendedPatternInputHatchBlockEntity.getBlockPos());
            extendedPatternInputHatchBlockEntity.refreshPatterns();
        });
    }

    public void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public Component getName() {
        return this.customName != null ? this.customName : Component.translatable((String)"block.aeind.advanced_extended_pattern_input_hatch");
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component component) {
        this.customName = component;
        this.markDirtyAndSync();
        this.refreshPatterns();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Nullable
    public IGridNode getGridNode(Direction direction) {
        return this.mainNode.getNode();
    }

    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.SMART;
    }

    @Nullable
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    public ExtendedHatchPatternProviderLogic getLogic() {
        return this.patternLogic;
    }

    public BlockEntity getBlockEntity() {
        return this;
    }

    public EnumSet<Direction> getTargets() {
        return EnumSet.noneOf(Direction.class);
    }

    @Override
    public void saveChanges() {
        this.setChanged();
    }

    public AEItemKey getTerminalIcon() {
        return AEItemKey.of((ItemLike)((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()));
    }

    public ItemStack getMainMenuIcon() {
        return new ItemStack((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
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

    public HatchType getHatchType() {
        return HatchTypes.ITEM_INPUT;
    }

    public boolean upgradesToSteel() {
        return false;
    }

    public MIInventory getInventory() {
        return this.bufferInventory;
    }

    public MachineModelClientData getMachineModelData() {
        return new MachineModelClientData();
    }

    public void appendItemInputs(List<ConfigurableItemStack> list) {
        list.addAll(this.bufferInventory.getItemStacks());
    }

    public void appendFluidInputs(List<ConfigurableFluidStack> list) {
        list.addAll(this.bufferInventory.getFluidStacks());
    }

    public IUpgradeInventory getUpgradeInventory() {
        return this.upgrades;
    }

    public boolean hasRedstoneCard() {
        return this.upgrades.getInstalledUpgrades((ItemLike)AEItems.REDSTONE_CARD) > 0;
    }

    public boolean isRedstonePowered() {
        return this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;
    }

    public boolean passesRedstone() {
        if (!this.hasRedstoneCard()) {
            return true;
        }
        boolean bl = this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;
        return switch (this.redstoneMode) {
            case 1 -> bl;
            case 2 -> {
                if (!bl) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    @Override
    public boolean canAcceptOrder() {
        if (!this.passesRedstone()) {
            return false;
        }
        boolean bl = this.patternLogic.getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.blockingMode != 0;
        boolean bl2 = bl;
        return !bl || !this.bufferHasContent();
    }

    public boolean canAcceptOrder(int n) {
        if (!this.passesRedstone()) {
            return false;
        }
        boolean bl = this.patternLogic.getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.blockingMode != 0;
        return !bl || !this.roomHasContent(n);
    }

    public boolean roomHasContent(int n) {
        if (n < 0 || n >= 36) {
            return false;
        }
        int n2 = n * 9;
        int n3 = n2 + 9;
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            return true;
        }
        for (ConfigurableFluidStack configurableItemStack : this.bufferInventory.getFluidStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            return true;
        }
        return false;
    }

    public long insertBuffer(int n, AEKey aEKey, long l, Actionable actionable) {
        if (n < 0 || n >= 36 || l <= 0L) {
            return 0L;
        }
        int n2 = n * 9;
        int n3 = n2 + 9;
        if (aEKey instanceof AEItemKey) {
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            return ExtendedPatternInputHatchBlockEntity.insertItems(this.bufferInventory.getItemStacks().subList(n2, n3), aEItemKey, l, actionable);
        }
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey aEFluidKey = (AEFluidKey)aEKey;
            return ExtendedPatternInputHatchBlockEntity.insertFluid(this.bufferInventory.getFluidStacks().subList(n2, n3), aEFluidKey, l, actionable != Actionable.MODULATE);
        }
        return 0L;
    }

    private boolean bufferHasContent() {
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks()) {
            if (configurableItemStack.isEmpty()) continue;
            return true;
        }
        return this.bufferInventory.getFluidStacks().stream().anyMatch(configurableFluidStack -> !configurableFluidStack.isEmpty());
    }

    public boolean hasStoredMaterials() {
        return this.bufferHasContent();
    }

    public int getBlockingMode() {
        return this.blockingMode;
    }

    public void setBlockingMode(int n) {
        this.blockingMode = n;
        this.markDirtyAndSync();
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    public void setRedstoneMode(int n) {
        this.redstoneMode = Math.floorMod(n, 3);
        this.markDirtyAndSync();
    }

    public void onUpgradesChanged() {
        int n = this.upgrades.getInstalledUpgrades((ItemLike)AEItems.CAPACITY_CARD);
        long l = Long.MAX_VALUE;
        for (ConfigurableFluidStack configurableFluidStack : this.bufferInventory.getFluidStacks()) {
            configurableFluidStack.setCapacity(l);
        }
        this.markDirtyAndSync();
    }

    public long countInBuffer(AEKey aEKey) {
        if (aEKey instanceof AEItemKey) {
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            ItemVariant itemVariant = ItemVariant.of((ItemStack)aEItemKey.toStack(1));
            long l = 0L;
            for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
                if (stack.isEmpty() || !((ItemVariant)stack.getResource()).equals((Object)itemVariant)) continue;
                l = ExtendedPatternInputHatchBlockEntity.saturatedAdd(l, stack.getAmount());
            }
            return l;
        }
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey aEFluidKey = (AEFluidKey)aEKey;
            long l = 0L;
            for (ConfigurableFluidStack stack : this.bufferInventory.getFluidStacks()) {
                if (stack.isEmpty() || ((FluidVariant)stack.getResource()).getFluid() != aEFluidKey.getFluid()) continue;
                l = ExtendedPatternInputHatchBlockEntity.saturatedAdd(l, stack.getAmount());
            }
            return l;
        }
        return 0L;
    }

    @Override
    public long insertBuffer(AEKey aEKey, long l, Actionable actionable) {
        if (l <= 0L) {
            return 0L;
        }
        if (aEKey instanceof AEItemKey) {
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            return this.insertItems(aEItemKey, l, actionable);
        }
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey aEFluidKey = (AEFluidKey)aEKey;
            return this.insertFluid(aEFluidKey, l, actionable != Actionable.MODULATE);
        }
        return 0L;
    }

    private long insertFluid(AEFluidKey aEFluidKey, long l, boolean bl) {
        return ExtendedPatternInputHatchBlockEntity.insertFluid(this.bufferInventory.getFluidStacks(), aEFluidKey, l, bl);
    }

    private static long insertFluid(List<ConfigurableFluidStack> list, AEFluidKey aEFluidKey, long l, boolean bl) {
        long l2 = l;
        if (l2 <= 0L) {
            return 0L;
        }
        for (ConfigurableFluidStack configurableFluidStack : list) {
            if (configurableFluidStack.isEmpty() || ((FluidVariant)configurableFluidStack.getResource()).getFluid() != aEFluidKey.getFluid()) continue;
            long l3 = configurableFluidStack.getCapacity() - configurableFluidStack.getAmount();
            if (l3 <= 0L) {
                return 0L;
            }
            long l4 = Math.min(l2, l3);
            if (!bl) {
                configurableFluidStack.increment(l4);
            }
            return l4;
        }
        for (ConfigurableFluidStack configurableFluidStack : list) {
            if (!configurableFluidStack.isEmpty()) continue;
            long l5 = Math.min(l2, configurableFluidStack.getCapacity());
            if (!bl) {
                configurableFluidStack.setKey((TransferVariant)FluidVariant.of((Fluid)aEFluidKey.getFluid()));
                configurableFluidStack.increment(l5);
            }
            return l5;
        }
        return 0L;
    }

    private long insertItems(AEItemKey aEItemKey, long l, Actionable actionable) {
        return ExtendedPatternInputHatchBlockEntity.insertItems(this.bufferInventory.getItemStacks(), aEItemKey, l, actionable);
    }

    private static long insertItems(List<ConfigurableItemStack> list, AEItemKey aEItemKey, long l, Actionable actionable) {
        if (l <= 0L) {
            return 0L;
        }
        ItemVariant itemVariant = ItemVariant.of((ItemStack)aEItemKey.toStack(1));
        long l2 = 0L;
        boolean bl = actionable != Actionable.MODULATE;
        for (ConfigurableItemStack configurableItemStack : list) {
            long l3;
            if (l2 >= l) break;
            if (configurableItemStack.isEmpty() ? !configurableItemStack.isResourceAllowedByLock((Object)aEItemKey.getItem()) : !((ItemVariant)configurableItemStack.getResource()).equals((Object)itemVariant)) continue;
            long l4 = Long.MAX_VALUE - configurableItemStack.getAmount();
            if (l4 <= 0L || (l3 = Math.min(l - l2, l4)) <= 0L) continue;
            if (!bl) {
                configurableItemStack.setKey(itemVariant);
                configurableItemStack.increment(l3);
            }
            l2 += l3;
        }
        return l2;
    }

    public long extractBuffer(AEKey aEKey, long l, Actionable actionable) {
        if (l <= 0L) {
            return 0L;
        }
        if (aEKey instanceof AEItemKey) {
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            ItemVariant itemVariant = ItemVariant.of((ItemStack)aEItemKey.toStack(1));
            long l2 = 0L;
            for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
                if (l2 >= l) break;
                if (stack.isEmpty() || !((ItemVariant)stack.getResource()).equals((Object)itemVariant)) continue;
                long extracted = Math.min(l - l2, stack.getAmount());
                if (actionable == Actionable.MODULATE) {
                    stack.decrement(extracted);
                }
                l2 += extracted;
            }
            return l2;
        }
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey aEFluidKey = (AEFluidKey)aEKey;
            long extracted = 0L;
            for (ConfigurableFluidStack stack : this.bufferInventory.getFluidStacks()) {
                if (extracted >= l) break;
                if (stack.isEmpty() || ((FluidVariant)stack.getResource()).getFluid() != aEFluidKey.getFluid()) continue;
                long amount = Math.min(l - extracted, stack.getAmount());
                if (actionable == Actionable.MODULATE) {
                    stack.decrement(amount);
                }
                extracted += amount;
            }
            return extracted;
        }
        return 0L;
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    @Override
    public boolean returnAllBufferToNetwork() {
        return this.returnBufferToNetwork(aEKey -> true);
    }

    public void returnRoomToNetwork(int n) {
        if (this.level == null || this.level.isClientSide || n < 0 || n >= 36) {
            return;
        }
        IGridNode iGridNode = this.mainNode.getNode();
        if (iGridNode == null || !iGridNode.isActive()) {
            return;
        }
        MEStorage mEStorage = iGridNode.getGrid().getStorageService().getInventory();
        MachineSource machineSource = new MachineSource((IActionHost)this);
        int n2 = n * 9;
        int n3 = n2 + 9;
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            configurableItemStack.decrement(mEStorage.insert((AEKey)AEItemKey.of(((ItemVariant)configurableItemStack.getResource()).toStack(1)), configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)machineSource));
        }
        for (ConfigurableFluidStack configurableItemStack : this.bufferInventory.getFluidStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            AEFluidKey aEFluidKey = AEFluidKey.of((Fluid)((FluidVariant)configurableItemStack.getResource()).getFluid());
            configurableItemStack.decrement(mEStorage.insert((AEKey)aEFluidKey, configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)machineSource));
        }
        this.setChanged();
    }

    @Override
    public List<ThreadIsolationRoom> aeind$isolatedInputRooms() {
        ArrayList<ThreadIsolationRoom> arrayList = new ArrayList<ThreadIsolationRoom>(36);
        for (int i = 0; i < 36; ++i) {
            int n = i * 9;
            int n2 = n + 9;
            arrayList.add(new ThreadIsolationRoom("advanced-extended:" + this.getBlockPos().asLong() + ":" + i, this.bufferInventory.getItemStacks().subList(n, n2), this.bufferInventory.getFluidStacks().subList(n, n2)));
        }
        return arrayList;
    }

    @Override
    public boolean returnUnusedBufferToNetwork() {
        Set<AEKey> set = this.patternLogic.getPatternInputKeys();
        return this.returnBufferToNetwork(aEKey -> !set.contains(aEKey.dropSecondary()));
    }

    private boolean returnBufferToNetwork(Predicate<AEKey> predicate) {
        long l;
        AEItemKey aEItemKey;
        if (this.level == null || this.level.isClientSide) {
            return false;
        }
        IGridNode iGridNode = this.mainNode.getNode();
        if (iGridNode == null || !iGridNode.isActive()) {
            return false;
        }
        MEStorage mEStorage = iGridNode.getGrid().getStorageService().getInventory();
        MachineSource machineSource = new MachineSource((IActionHost)this);
        boolean bl = false;
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks()) {
            aEItemKey = AEItemKey.of(((ItemVariant)configurableItemStack.getResource()).toStack(1));
            if (configurableItemStack.isEmpty() || !predicate.test((AEKey)aEItemKey) || (l = mEStorage.insert((AEKey)aEItemKey, configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)machineSource)) <= 0L) continue;
            configurableItemStack.decrement(l);
            bl = true;
        }
        for (ConfigurableFluidStack configurableItemStack : this.bufferInventory.getFluidStacks()) {
            if (configurableItemStack.isEmpty() || !predicate.test((AEKey)(aEItemKey = AEFluidKey.of((Fluid)((FluidVariant)configurableItemStack.getResource()).getFluid()))) || (l = mEStorage.insert((AEKey)aEItemKey, configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)machineSource)) <= 0L) continue;
            configurableItemStack.decrement(l);
            bl = true;
        }
        if (bl) {
            this.setChanged();
        }
        return bl;
    }

    @Override
    public void returnToNetwork(AEKey aEKey, long l) {
        if (this.level == null || this.level.isClientSide || l <= 0L) {
            return;
        }
        IGridNode iGridNode = this.mainNode.getNode();
        if (iGridNode == null || !iGridNode.isActive()) {
            return;
        }
        iGridNode.getGrid().getStorageService().getInventory().insert(aEKey, l, Actionable.MODULATE, (IActionSource)new MachineSource((IActionHost)this));
    }

    public void refreshPatterns() {
        this.patternLogic.updatePatterns();
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ExtendedPatternInputHatchBlockEntity extendedPatternInputHatchBlockEntity) {
        if (level.isClientSide) {
            return;
        }
        ++extendedPatternInputHatchBlockEntity.tickCount;
        if (extendedPatternInputHatchBlockEntity.tickCount % 20 != 0) {
            return;
        }
        extendedPatternInputHatchBlockEntity.doWork();
    }

    private void doWork() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean bl = false;
        if (bl |= this.patternLogic.flushBufferSendList()) {
            this.setChanged();
        }
    }

    public NonNullList<ItemStack> getDropItems() {
        NonNullList nonNullList = NonNullList.create();
        this.patternLogic.addDrops((List<ItemStack>)nonNullList);
        for (int i = 0; i < this.upgrades.size(); ++i) {
            ItemStack itemStack = this.upgrades.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            nonNullList.add((Object)itemStack.copy());
        }
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks()) {
            if (configurableItemStack.isEmpty()) continue;
            ItemVariant itemVariant = (ItemVariant)configurableItemStack.getResource();
            long remaining = configurableItemStack.getAmount();
            int maxStackSize = Math.max(1, itemVariant.getMaxStackSize());
            while (remaining > 0L) {
                int count = (int)Math.min(remaining, (long)maxStackSize);
                nonNullList.add((Object)itemVariant.toStack(count));
                remaining -= count;
            }
        }
        return nonNullList;
    }

    public ContainerData getContainerData() {
        return this.dataAccess;
    }
}
