/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.AECapabilities
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
 */
package aeind;

import appeng.api.AECapabilities;
import aeind.blockentity.MEOutputHatchBlockEntity;
import aeind.blockentity.MEOutputHatchStorage;
import aeind.blockentity.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid="aeind")
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent registerCapabilitiesEvent) {
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (advancedPatternInputHatchBlockEntity, direction) -> advancedPatternInputHatchBlockEntity.getBufferInventory());
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.FluidHandler.BLOCK, (BlockEntityType)ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (advancedPatternInputHatchBlockEntity, direction) -> advancedPatternInputHatchBlockEntity.getFluidHandler());
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.THREAD_WAREHOUSE.get(), (threadWarehouseBlockEntity, direction) -> threadWarehouseBlockEntity.getInventory().itemStorage.itemHandler);
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.CROSS_THREAD_PARALLEL_WAREHOUSE.get(), (crossThreadParallelWarehouseBlockEntity, direction) -> crossThreadParallelWarehouseBlockEntity.getInventory().itemStorage.itemHandler);
        registerCapabilitiesEvent.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, (BlockEntityType)ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (advancedPatternInputHatchBlockEntity, void_) -> advancedPatternInputHatchBlockEntity);
        if (ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            registerCapabilitiesEvent.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (extendedPatternInputHatchBlockEntity, direction) -> extendedPatternInputHatchBlockEntity.getBufferInventory());
            registerCapabilitiesEvent.registerBlockEntity(Capabilities.FluidHandler.BLOCK, (BlockEntityType)ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (extendedPatternInputHatchBlockEntity, direction) -> extendedPatternInputHatchBlockEntity.getFluidHandler());
            registerCapabilitiesEvent.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, (BlockEntityType)ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (extendedPatternInputHatchBlockEntity, void_) -> extendedPatternInputHatchBlockEntity);
        }
        registerCapabilitiesEvent.registerBlockEntity(AECapabilities.ME_STORAGE, (BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), (mEOutputHatchBlockEntity, direction) -> new MEOutputHatchStorage((MEOutputHatchBlockEntity)((Object)mEOutputHatchBlockEntity)));
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), (mEOutputHatchBlockEntity, direction) -> mEOutputHatchBlockEntity.getBufferInventory());
        registerCapabilitiesEvent.registerBlockEntity(Capabilities.FluidHandler.BLOCK, (BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), (mEOutputHatchBlockEntity, direction) -> mEOutputHatchBlockEntity.getFluidHandler());
        registerCapabilitiesEvent.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, (BlockEntityType)ModBlockEntities.ME_OUTPUT_HATCH.get(), (mEOutputHatchBlockEntity, void_) -> mEOutputHatchBlockEntity);
    }
}

