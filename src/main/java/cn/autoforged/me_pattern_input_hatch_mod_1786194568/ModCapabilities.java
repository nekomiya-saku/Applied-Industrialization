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
package cn.autoforged.me_pattern_input_hatch_mod_1786194568;

import appeng.api.AECapabilities;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchStorage;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid="aeind")
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ME_PATTERN_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getBufferInventory());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ME_PATTERN_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler());
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.ME_PATTERN_INPUT_HATCH.get(),
                (blockEntity, context) -> blockEntity);
        event.registerBlockEntity(AECapabilities.ME_STORAGE, ModBlockEntities.ME_OUTPUT_HATCH.get(),
                (blockEntity, side) -> new MEOutputHatchStorage(blockEntity));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ME_OUTPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getBufferInventory());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ME_OUTPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler());
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.ME_OUTPUT_HATCH.get(),
                (blockEntity, context) -> blockEntity);
    }
}
