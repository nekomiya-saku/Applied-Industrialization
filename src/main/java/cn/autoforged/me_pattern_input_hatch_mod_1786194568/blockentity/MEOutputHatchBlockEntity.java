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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
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
    public static final int BUFFER_ITEM_SLOTS = 36;
    public static final int BUFFER_FLUID_TANKS = 36;
    public static final long FLUID_CAPACITY = Long.MAX_VALUE;
    public static final int TICK_RATE = 1;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode((Object)((Object)this), NODE_LISTENER).setVisualRepresentation(new ItemStack((ItemLike)ModBlocks.ME_OUTPUT_HATCH.get())).setInWorldNode(true).setTagName("me_output_hatch_node").setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL}).setExposedOnSides(EnumSet.allOf(Direction.class));
    private final MIInventory bufferInventory;
    private final AEKeyLongStorage outputBuffer = new AEKeyLongStorage(this::setChanged);
    private final MachineComponent persistentData = new MachineComponent(){

        public void writeNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
            MEOutputHatchBlockEntity.this.mainNode.saveToNBT(compoundTag);
            compoundTag.put("aeindOutputBuffer", MEOutputHatchBlockEntity.this.outputBuffer.writeNbt(provider));
        }

        public void readNbt(CompoundTag compoundTag, HolderLookup.Provider provider, boolean bl) {
            MEOutputHatchBlockEntity.this.mainNode.loadFromNBT(compoundTag);
            MEOutputHatchBlockEntity.this.outputBuffer.readNbt(compoundTag.getList("aeindOutputBuffer", 10), provider);
            MEOutputHatchBlockEntity.this.ensureLongOutputSlots();
        }
    };
    private int tickCount = 0;
    private static final IGridNodeListener<MEOutputHatchBlockEntity> NODE_LISTENER = new IGridNodeListener<MEOutputHatchBlockEntity>(){

        public void onSaveChanges(MEOutputHatchBlockEntity mEOutputHatchBlockEntity, IGridNode iGridNode) {
            mEOutputHatchBlockEntity.setChanged();
        }
    };

    public MEOutputHatchBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(new BEP((BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), blockPos, blockState), new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"me_output_hatch"), true).backgroundHeight(200).build(), OrientationComponent.Params.noFacing((boolean)true, (boolean)false));
        ArrayList<ConfigurableItemStack> arrayList = new ArrayList<ConfigurableItemStack>(36);
        for (int i = 0; i < 36; ++i) {
            arrayList.add(new LongOutputItemStack());
        }
        ArrayList<ConfigurableFluidStack> arrayList2 = new ArrayList<ConfigurableFluidStack>(36);
        for (int i = 0; i < 36; ++i) {
            arrayList2.add(ConfigurableFluidStack.standardOutputSlot(Long.MAX_VALUE));
        }
        SlotPositions slotPositions = new SlotPositions.Builder().addSlots(8, 18, 9, 4).build();
        SlotPositions slotPositions2 = new SlotPositions.Builder().addSlots(8, 18, 9, 4).build();
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
        GridHelper.onFirstTick((BlockEntity)this, mEOutputHatchBlockEntity -> {
            if (mEOutputHatchBlockEntity.getLevel() == null || mEOutputHatchBlockEntity.isRemoved()) {
                return;
            }
            mEOutputHatchBlockEntity.mainNode.create(mEOutputHatchBlockEntity.getLevel(), mEOutputHatchBlockEntity.getBlockPos());
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

    public AECableType getCableConnectionType(Direction direction) {
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

    public AEKeyLongStorage getOutputBuffer() {
        return this.outputBuffer;
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

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, MEOutputHatchBlockEntity mEOutputHatchBlockEntity) {
        if (level.isClientSide) {
            return;
        }
        ++mEOutputHatchBlockEntity.tickCount;
        if (mEOutputHatchBlockEntity.tickCount % 1 != 0) {
            return;
        }
        mEOutputHatchBlockEntity.doWork();
    }

    private void doWork() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.ensureLongOutputSlots();
        this.collectMachineOutputs();
        IGridNode iGridNode = this.mainNode.getNode();
        if (iGridNode == null || !iGridNode.isActive()) {
            return;
        }
        MachineSource machineSource = new MachineSource((IActionHost)this);
        MEStorage mEStorage = this.mainNode.getGrid().getStorageService().getInventory();
        this.outputBuffer.flushTo(mEStorage, (IActionSource)machineSource);
    }

    void collectMachineOutputs() {
        this.ensureLongOutputSlots();
        for (ConfigurableItemStack stack : this.bufferInventory.getItemStacks()) {
            if (stack.isEmpty() || stack.getAmount() <= 0L) {
                continue;
            }
            AEItemKey key = AEItemKey.of(((ItemVariant)stack.getResource()).toStack(1));
            long inserted = this.outputBuffer.insert((AEKey)key, stack.getAmount(), Actionable.MODULATE);
            if (inserted > 0L) {
                stack.decrement(inserted);
                if (stack.isEmpty()) {
                    stack.disableMachineLock();
                }
            }
        }
        for (ConfigurableFluidStack stack : this.bufferInventory.getFluidStacks()) {
            if (stack.isEmpty() || stack.getAmount() <= 0L) {
                continue;
            }
            AEFluidKey key = AEFluidKey.of(stack.toStack());
            long inserted = this.outputBuffer.insert((AEKey)key, stack.getAmount(), Actionable.MODULATE);
            if (inserted > 0L) {
                stack.decrement(inserted);
                if (stack.isEmpty()) {
                    stack.disableMachineLock();
                }
            }
        }
    }

    private void ensureLongOutputSlots() {
        List<ConfigurableItemStack> itemStacks = this.bufferInventory.getItemStacks();
        for (int i = 0; i < itemStacks.size(); ++i) {
            ConfigurableItemStack stack = itemStacks.get(i);
            if (!(stack instanceof LongOutputItemStack)) {
                itemStacks.set(i, new LongOutputItemStack(stack));
            }
        }
        for (ConfigurableFluidStack stack : this.bufferInventory.getFluidStacks()) {
            stack.setCapacity(Long.MAX_VALUE);
        }
    }

    public void addOutputDrops(List<ItemStack> list) {
        this.collectMachineOutputs();
        this.outputBuffer.addItemDrops(list);
        for (ConfigurableItemStack configurableItemStack : this.bufferInventory.getItemStacks()) {
            if (configurableItemStack.isEmpty()) continue;
            ItemVariant itemVariant = (ItemVariant)configurableItemStack.getResource();
            long remaining = configurableItemStack.getAmount();
            int maxStackSize = Math.max(1, itemVariant.getMaxStackSize());
            while (remaining > 0L) {
                int count = (int)Math.min(remaining, (long)maxStackSize);
                list.add(itemVariant.toStack(count));
                remaining -= count;
            }
        }
    }
}
