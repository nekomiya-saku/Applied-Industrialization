/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package aeind.blockentity;

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
import aeind.blockentity.ModBlockEntities;
import aeind.isolation.ThreadIsolationHatch;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ThreadWarehouseBlockEntity
extends HatchBlockEntity
implements ThreadIsolationHatch {
    private final MIInventory inventory = new MIInventory(List.of(ConfigurableItemStack.standardInputSlot()), List.of(), new SlotPositions.Builder().addSlot(0, 0).build(), SlotPositions.empty());

    public ThreadWarehouseBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(blockPos, blockState, (BlockEntityType)ModBlockEntities.THREAD_WAREHOUSE.get(), "thread_warehouse");
    }

    protected ThreadWarehouseBlockEntity(BlockPos blockPos, BlockState blockState, BlockEntityType<?> blockEntityType, String string) {
        super(new BEP(blockEntityType, blockPos, blockState), new MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)string), false).build(), OrientationComponent.Params.noFacing((boolean)true, (boolean)false));
        this.registerComponents(new MachineComponent[]{this.inventory});
    }

    public HatchType getHatchType() {
        return HatchTypes.ITEM_INPUT;
    }

    public boolean upgradesToSteel() {
        return false;
    }

    public MIInventory getInventory() {
        return this.inventory;
    }

    public void appendItemInputs(List<ConfigurableItemStack> list) {
        list.addAll(this.inventory.getItemStacks());
    }

    public MachineModelClientData getMachineModelData() {
        return new MachineModelClientData();
    }
}

