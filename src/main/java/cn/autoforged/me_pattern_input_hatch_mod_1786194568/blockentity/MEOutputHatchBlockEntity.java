/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.networking.GridFlags
 *  appeng.api.networking.GridHelper
 *  appeng.api.networking.IGridNode
 *  appeng.api.networking.IGridNodeListener
 *  appeng.api.networking.IInWorldGridNodeHost
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.security.IActionHost
 *  appeng.api.networking.security.IActionSource
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.api.storage.MEStorage
 *  appeng.api.util.AECableType
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
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.Nullable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
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
import appeng.api.util.AECableType;
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
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class MEOutputHatchBlockEntity
extends HatchBlockEntity
implements IInWorldGridNodeHost,
IActionHost {
    public static final int BUFFER_ITEM_SLOTS = 9;
    public static final int BUFFER_FLUID_TANKS = 9;
    public static final long FLUID_CAPACITY = Integer.MAX_VALUE;
    public static final int TICK_RATE = 1;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, NODE_LISTENER).setVisualRepresentation(new ItemStack((ItemLike)ModBlocks.ME_OUTPUT_HATCH.get())).setInWorldNode(true).setTagName("me_output_hatch_node").setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL}).setExposedOnSides(EnumSet.allOf(Direction.class));
    private final MIInventory bufferInventory;
    private final MachineComponent persistentData = new MachineComponent(){

        public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
            MEOutputHatchBlockEntity.this.mainNode.saveToNBT(tag);
        }

        public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
            MEOutputHatchBlockEntity.this.mainNode.loadFromNBT(tag);
        }
    };
    private int tickCount = 0;
    private static final IGridNodeListener<MEOutputHatchBlockEntity> NODE_LISTENER = new IGridNodeListener<MEOutputHatchBlockEntity>(){

        public void onSaveChanges(MEOutputHatchBlockEntity owner, IGridNode node) {
            owner.setChanged();
        }
    };

    public MEOutputHatchBlockEntity(BlockPos pos, BlockState blockState) {
        super(new BEP((BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), pos, blockState), new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"me_output_hatch"), true).build(), OrientationComponent.Params.noFacing((boolean)true, (boolean)false));
        ArrayList<ConfigurableItemStack> itemStacks = new ArrayList<ConfigurableItemStack>(9);
        for (int i = 0; i < 9; ++i) {
            itemStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        ArrayList<ConfigurableFluidStack> fluidStacks = new ArrayList<ConfigurableFluidStack>(9);
        for (int i = 0; i < 9; ++i) {
            fluidStacks.add(ConfigurableFluidStack.standardOutputSlot((long)Integer.MAX_VALUE));
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
        GridHelper.onFirstTick(this, (MEOutputHatchBlockEntity be) -> {
            if (be.getLevel() == null || be.isRemoved()) {
                return;
            }
            be.mainNode.create(be.getLevel(), be.getBlockPos());
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

    public IItemHandler getBufferInventory() {
        return this.bufferInventory.itemStorage.itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return this.bufferInventory.fluidStorage.fluidHandler;
    }

    public MIInventory getBuffer() {
        return this.bufferInventory;
    }

    public HatchType getHatchType() {
        return HatchTypes.ITEM_OUTPUT;
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

    public void appendItemOutputs(List<ConfigurableItemStack> list) {
        list.addAll(0, this.bufferInventory.getItemStacks());
    }

    public void appendFluidOutputs(List<ConfigurableFluidStack> list) {
        list.addAll(0, this.bufferInventory.getFluidStacks());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MEOutputHatchBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        ++blockEntity.tickCount;
        if (blockEntity.tickCount % 1 != 0) {
            return;
        }
        blockEntity.doWork();
    }

    private void doWork() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        IGridNode node = this.mainNode.getNode();
        if (node == null || !node.isActive()) {
            return;
        }
        MachineSource source = new MachineSource((IActionHost)this);
        MEStorage storage = this.mainNode.getGrid().getStorageService().getInventory();
        boolean changed = false;
        IItemHandler itemHandler = this.bufferInventory.itemStorage.itemHandler;
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            long toPush;
            AEItemKey key;
            long pushed;
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty() || (pushed = storage.insert((AEKey)(key = AEItemKey.of((ItemStack)stack)), toPush = (long)stack.getCount(), Actionable.MODULATE, (IActionSource)source)) <= 0L) continue;
            itemHandler.extractItem(i, (int)Math.min(pushed, Integer.MAX_VALUE), false);
            changed = true;
        }
        IFluidHandler fluidHandler = this.bufferInventory.fluidStorage.fluidHandler;
        for (int i = 0; i < fluidHandler.getTanks(); ++i) {
            long toPush;
            AEFluidKey key;
            long pushed;
            FluidStack fluid = fluidHandler.getFluidInTank(i);
            if (fluid.isEmpty() || (pushed = storage.insert((AEKey)(key = AEFluidKey.of((FluidStack)fluid)), toPush = (long)fluid.getAmount(), Actionable.MODULATE, (IActionSource)source)) <= 0L) continue;
            fluidHandler.drain(fluid.copyWithAmount((int)Math.min(pushed, Integer.MAX_VALUE)), IFluidHandler.FluidAction.EXECUTE);
            changed = true;
        }
        if (changed) {
            this.setChanged();
        }
    }

    public void addOutputDrops(List<ItemStack> drops) {
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            if (stack.isEmpty()) continue;
            ItemStack copy = stack.toStack();
            int max = Math.max(1, copy.getMaxStackSize());
            while (!copy.isEmpty()) {
                int count = Math.min(copy.getCount(), max);
                ItemStack part = copy.copy();
                part.setCount(count);
                drops.add(part);
                copy.shrink(count);
            }
        }
    }
}
