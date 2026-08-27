/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

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
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.HatchPatternProviderLogic;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
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
import net.minecraft.resources.ResourceLocation;
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

public class MEPatternInputHatchBlockEntity
extends HatchBlockEntity
implements IInWorldGridNodeHost,
IActionHost,
PatternProviderLogicHost {
    public static final int PATTERN_SLOTS = 9;
    public static final int BUFFER_SLOTS = 9;
    public static final int BUFFER_FLUID_TANKS = 9;
    public static final int UPGRADE_SLOTS = 2;
    public static final long FLUID_CAPACITY = Integer.MAX_VALUE;
    public static final int TICK_RATE = 20;
    public static final int REDSTONE_MODE_IGNORE = 0;
    public static final int REDSTONE_MODE_HIGH = 1;
    public static final int REDSTONE_MODE_LOW = 2;
    private final HatchPatternProviderLogic patternLogic;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode((Object)((Object)this), NODE_LISTENER).setVisualRepresentation(new ItemStack((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get())).setInWorldNode(true).setTagName("me_pattern_input_hatch_node").setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL}).setExposedOnSides(EnumSet.allOf(Direction.class));
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine((ItemLike)((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get()), (int)2, this::onUpgradesChanged);
    private int blockingMode = 0;
    private int redstoneMode = 0;
    private final MIInventory bufferInventory;
    private final MachineComponent persistentData = new MachineComponent(){

        public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
            MEPatternInputHatchBlockEntity.this.mainNode.saveToNBT(tag);
            MEPatternInputHatchBlockEntity.this.patternLogic.writeToNBT(tag, registries);
            MEPatternInputHatchBlockEntity.this.upgrades.writeToNBT(tag, "upgrades", registries);
            tag.putInt("blockingMode", MEPatternInputHatchBlockEntity.this.blockingMode);
            tag.putInt("redstoneMode", MEPatternInputHatchBlockEntity.this.redstoneMode);
        }

        public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
            MEPatternInputHatchBlockEntity.this.mainNode.loadFromNBT(tag);
            MEPatternInputHatchBlockEntity.this.patternLogic.readFromNBT(tag, registries);
            MEPatternInputHatchBlockEntity.this.upgrades.readFromNBT(tag, "upgrades", registries);
            if (tag.contains("blockingMode")) {
                MEPatternInputHatchBlockEntity.this.blockingMode = tag.getInt("blockingMode");
            }
            if (tag.contains("redstoneMode")) {
                MEPatternInputHatchBlockEntity.this.redstoneMode = tag.getInt("redstoneMode");
            }
        }

        public void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
            MEPatternInputHatchBlockEntity.this.upgrades.writeToNBT(tag, "upgrades", registries);
            tag.putInt("blockingMode", MEPatternInputHatchBlockEntity.this.blockingMode);
            tag.putInt("redstoneMode", MEPatternInputHatchBlockEntity.this.redstoneMode);
        }

        public void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
            MEPatternInputHatchBlockEntity.this.upgrades.readFromNBT(tag, "upgrades", registries);
            if (tag.contains("blockingMode")) {
                MEPatternInputHatchBlockEntity.this.blockingMode = tag.getInt("blockingMode");
            }
            if (tag.contains("redstoneMode")) {
                MEPatternInputHatchBlockEntity.this.redstoneMode = tag.getInt("redstoneMode");
            }
        }
    };
    private final ContainerData dataAccess = new ContainerData(){

        public int get(int index) {
            return switch (index) {
                case 0 -> MEPatternInputHatchBlockEntity.this.blockingMode;
                case 1 -> MEPatternInputHatchBlockEntity.this.redstoneMode;
                case 2 -> {
                    if (MEPatternInputHatchBlockEntity.this.hasRedstoneCard()) {
                        yield 1;
                    }
                    yield 0;
                }
                case 3 -> {
                    if (MEPatternInputHatchBlockEntity.this.isRedstonePowered()) {
                        yield 1;
                    }
                    yield 0;
                }
                default -> 0;
            };
        }

        public void set(int index, int value) {
        }

        public int getCount() {
            return 4;
        }
    };
    private int tickCount = 0;
    private static final IGridNodeListener<MEPatternInputHatchBlockEntity> NODE_LISTENER = new IGridNodeListener<MEPatternInputHatchBlockEntity>(){

        public void onSaveChanges(MEPatternInputHatchBlockEntity owner, IGridNode node) {
            owner.setChanged();
        }

        public void onStateChanged(MEPatternInputHatchBlockEntity owner, IGridNode node, IGridNodeListener.State state) {
            owner.refreshPatterns();
        }
    };

    public MEPatternInputHatchBlockEntity(BlockPos pos, BlockState blockState) {
        super(new BEP((BlockEntityType)ModBlockEntities.ME_PATTERN_INPUT_HATCH.get(), pos, blockState), new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"me_pattern_input_hatch"), true).build(), OrientationComponent.Params.noFacing((boolean)true, (boolean)false));
        this.patternLogic = new HatchPatternProviderLogic(this.mainNode, this);
        ArrayList<ConfigurableItemStack> itemStacks = new ArrayList<ConfigurableItemStack>(9);
        for (int i = 0; i < 9; ++i) {
            itemStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        ArrayList<ConfigurableFluidStack> fluidStacks = new ArrayList<ConfigurableFluidStack>(9);
        for (int i = 0; i < 9; ++i) {
            fluidStacks.add(ConfigurableFluidStack.standardInputSlot((long)Integer.MAX_VALUE));
        }
        SlotPositions itemPos = new SlotPositions.Builder().addSlots(0, 0, 9, 1).build();
        SlotPositions fluidPos = new SlotPositions.Builder().addSlots(0, 0, 9, 1).build();
        this.bufferInventory = new MIInventory(itemStacks, fluidStacks, itemPos, fluidPos);
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
        GridHelper.onFirstTick((BlockEntity)this, be -> {
            if (be.getLevel() == null || be.isRemoved()) {
                return;
            }
            be.mainNode.create(be.getLevel(), be.getBlockPos());
            be.refreshPatterns();
        });
    }

    public void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Nullable
    public IGridNode getGridNode(Direction direction) {
        return this.mainNode.getNode();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Nullable
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    public HatchPatternProviderLogic getLogic() {
        return this.patternLogic;
    }

    public BlockEntity getBlockEntity() {
        return this;
    }

    public EnumSet<Direction> getTargets() {
        return EnumSet.noneOf(Direction.class);
    }

    public void saveChanges() {
        this.setChanged();
    }

    public AEItemKey getTerminalIcon() {
        return AEItemKey.of((ItemLike)((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get()));
    }

    public ItemStack getMainMenuIcon() {
        return new ItemStack((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get());
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

    public HatchPatternProviderLogic getPatternLogic() {
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
        boolean powered = this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;
        return switch (this.redstoneMode) {
            case 1 -> powered;
            case 2 -> {
                if (!powered) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    public boolean canAcceptOrder() {
        boolean blocking;
        if (!this.passesRedstone()) {
            return false;
        }
        boolean bl = blocking = this.patternLogic.getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.blockingMode != 0;
        return !blocking || !this.bufferHasContent();
    }

    private boolean bufferHasContent() {
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            if (stack.isEmpty()) continue;
            return true;
        }
        return this.bufferInventory.getFluidStacks().stream().anyMatch(s -> !s.isEmpty());
    }

    public boolean hasStoredMaterials() {
        return this.bufferHasContent();
    }

    public int getBlockingMode() {
        return this.blockingMode;
    }

    public void setBlockingMode(int mode) {
        this.blockingMode = mode;
        this.markDirtyAndSync();
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    public void setRedstoneMode(int mode) {
        this.redstoneMode = Math.floorMod(mode, 3);
        this.markDirtyAndSync();
    }

    public void onUpgradesChanged() {
        int cards = this.upgrades.getInstalledUpgrades((ItemLike)AEItems.CAPACITY_CARD);
        long cap = Math.min(Integer.MAX_VALUE * (long)(1 << Math.min(cards, 2)), Integer.MAX_VALUE);
        for (ConfigurableFluidStack fluid : this.bufferInventory.getFluidStacks()) {
            fluid.setCapacity(cap);
        }
        this.markDirtyAndSync();
    }

    public long countInBuffer(AEKey key) {
        if (key instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)key;
            long count = 0L;
            IItemHandler handler = this.bufferInventory.itemStorage.itemHandler;
            for (int i = 0; i < handler.getSlots(); ++i) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.isEmpty() || !itemKey.matches(stack)) continue;
                count += (long)stack.getCount();
            }
            return count;
        }
        if (key instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)key;
            long count = 0L;
            IFluidHandler handler = this.bufferInventory.fluidStorage.fluidHandler;
            for (int i = 0; i < handler.getTanks(); ++i) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (!fluidKey.matches(fluid)) continue;
                count += (long)fluid.getAmount();
            }
            return count;
        }
        return 0L;
    }

    public long insertBuffer(AEKey what, long amount, Actionable mode) {
        if (amount <= 0L) {
            return 0L;
        }
        if (what instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)what;
            return this.insertItems(itemKey, amount, mode);
        }
        if (what instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)what;
            return this.insertFluid(fluidKey, amount, mode != Actionable.MODULATE);
        }
        return 0L;
    }

    private long insertFluid(AEFluidKey fluidKey, long amount, boolean simulate) {
        long capped = Math.min(amount, Integer.MAX_VALUE);
        if (capped <= 0L) {
            return 0L;
        }
        List stacks = this.bufferInventory.getFluidStacks();
        for (ConfigurableFluidStack stack : stacks) {
            if (stack.isEmpty() || ((FluidVariant)stack.getResource()).getFluid() != fluidKey.getFluid()) continue;
            long space = stack.getCapacity() - stack.getAmount();
            if (space <= 0L) {
                return 0L;
            }
            long toAdd = Math.min(capped, space);
            if (!simulate) {
                stack.increment(toAdd);
            }
            return toAdd;
        }
        for (ConfigurableFluidStack stack : stacks) {
            if (!stack.isEmpty()) continue;
            long toAdd = Math.min(capped, stack.getCapacity());
            if (!simulate) {
                stack.setKey((TransferVariant)FluidVariant.of((Fluid)fluidKey.getFluid()));
                stack.increment(toAdd);
            }
            return toAdd;
        }
        return 0L;
    }

    private long insertItems(AEItemKey key, long amount, Actionable mode) {
        if (amount <= 0L) {
            return 0L;
        }
        ItemVariant variant = ItemVariant.of((ItemStack)key.toStack(1));
        long placed = 0L;
        boolean simulate = mode != Actionable.MODULATE;
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            long toAdd;
            if (placed >= amount) break;
            if (!stack.isEmpty() ? !((ItemVariant)stack.getResource()).equals((Object)variant) : !stack.isResourceAllowedByLock((Object)key.getItem())) continue;
            long space = Integer.MAX_VALUE - stack.getAmount();
            if (space <= 0L || (toAdd = Math.min(amount - placed, space)) <= 0L) continue;
            if (!simulate) {
                stack.setKey(variant);
                stack.increment(toAdd);
            }
            placed += toAdd;
        }
        return placed;
    }

    public long extractBuffer(AEKey what, long amount, Actionable mode) {
        if (amount <= 0L) {
            return 0L;
        }
        if (what instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)what;
            long extracted = 0L;
            boolean simulate = mode != Actionable.MODULATE;
            IItemHandler handler = this.bufferInventory.itemStorage.itemHandler;
            for (int slot = 0; slot < handler.getSlots() && extracted < amount; ++slot) {
                ItemStack current = handler.getStackInSlot(slot);
                if (current.isEmpty() || !itemKey.matches(current)) continue;
                int take = (int)Math.min(amount - extracted, (long)current.getCount());
                ItemStack got = handler.extractItem(slot, take, simulate);
                extracted += (long)got.getCount();
            }
            return extracted;
        }
        if (what instanceof AEFluidKey) {
            AEFluidKey fluidKey = (AEFluidKey)what;
            long capped = Math.min(amount, Integer.MAX_VALUE);
            FluidStack drained = this.bufferInventory.fluidStorage.fluidHandler.drain(fluidKey.toStack((int)capped), mode == Actionable.MODULATE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            return drained.getAmount();
        }
        return 0L;
    }

    public boolean returnAllBufferToNetwork() {
        return this.returnBufferToNetwork(key -> true);
    }

    public boolean returnUnusedBufferToNetwork() {
        Set<AEKey> allowed = this.patternLogic.getPatternInputKeys();
        return this.returnBufferToNetwork(key -> !allowed.contains(key.dropSecondary()));
    }

    private boolean returnBufferToNetwork(Predicate<AEKey> filter) {
        long pushed;
        AEItemKey key;
        if (this.level == null || this.level.isClientSide) {
            return false;
        }
        IGridNode node = this.mainNode.getNode();
        if (node == null || !node.isActive()) {
            return false;
        }
        MEStorage storage = node.getGrid().getStorageService().getInventory();
        MachineSource source = new MachineSource((IActionHost)this);
        boolean changed = false;
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            if (stack.isEmpty() || !filter.test((AEKey)(key = AEItemKey.of((ItemStack)stack.toStack()))) || (pushed = storage.insert((AEKey)key, stack.getAmount(), Actionable.MODULATE, (IActionSource)source)) <= 0L) continue;
            stack.decrement(pushed);
            changed = true;
        }
        for (ConfigurableItemStack stack : this.bufferInventory.getFluidStacks()) {
            if (stack.isEmpty() || !filter.test((AEKey)(key = AEFluidKey.of((Fluid)((FluidVariant)stack.getResource()).getFluid()))) || (pushed = storage.insert((AEKey)key, stack.getAmount(), Actionable.MODULATE, (IActionSource)source)) <= 0L) continue;
            stack.decrement(pushed);
            changed = true;
        }
        if (changed) {
            this.setChanged();
        }
        return changed;
    }

    public void returnToNetwork(AEKey what, long amount) {
        if (this.level == null || this.level.isClientSide || amount <= 0L) {
            return;
        }
        IGridNode node = this.mainNode.getNode();
        if (node == null || !node.isActive()) {
            return;
        }
        node.getGrid().getStorageService().getInventory().insert(what, amount, Actionable.MODULATE, (IActionSource)new MachineSource((IActionHost)this));
    }

    public void refreshPatterns() {
        this.patternLogic.updatePatterns();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MEPatternInputHatchBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        ++blockEntity.tickCount;
        if (blockEntity.tickCount % 20 != 0) {
            return;
        }
        blockEntity.doWork();
    }

    private void doWork() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean changed = false;
        if (changed |= this.patternLogic.flushBufferSendList()) {
            this.setChanged();
        }
    }

    public NonNullList<ItemStack> getDropItems() {
        NonNullList drops = NonNullList.create();
        this.patternLogic.addDrops((List)drops);
        for (int i = 0; i < this.upgrades.size(); ++i) {
            ItemStack card = this.upgrades.getStackInSlot(i);
            if (card.isEmpty()) continue;
            drops.add((Object)card.copy());
        }
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            if (stack.isEmpty()) continue;
            ItemStack copy = stack.toStack();
            int max = Math.max(1, copy.getMaxStackSize());
            while (!copy.isEmpty()) {
                int count = Math.min(copy.getCount(), max);
                ItemStack part = copy.copy();
                part.setCount(count);
                drops.add((Object)part);
                copy.shrink(count);
            }
        }
        return drops;
    }

    public ContainerData getContainerData() {
        return this.dataAccess;
    }
}

