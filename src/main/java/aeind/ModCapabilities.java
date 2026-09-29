package aeind;

import appeng.api.AECapabilities;
import aeind.blockentity.MEOutputHatchStorage;
import aeind.blockentity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = "aeind")
public class ModCapabilities {
   @SubscribeEvent
   public static void registerCapabilities(RegisterCapabilitiesEvent var0) {
      var0.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x.getBufferInventory());
      var0.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x.getFluidHandler());
      var0.registerBlockEntity(
         Capabilities.ItemHandler.BLOCK, ModBlockEntities.THREAD_WAREHOUSE.get(), (var0x, var1) -> var0x.getInventory().itemStorage.itemHandler
      );
      var0.registerBlockEntity(
         Capabilities.ItemHandler.BLOCK, ModBlockEntities.CROSS_THREAD_PARALLEL_WAREHOUSE.get(), (var0x, var1) -> var0x.getInventory().itemStorage.itemHandler
      );
      var0.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x);
      if (ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
         var0.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK, ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x.getBufferInventory()
         );
         var0.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK, ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x.getFluidHandler()
         );
         var0.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), (var0x, var1) -> var0x);
      }

      var0.registerBlockEntity(AECapabilities.ME_STORAGE, ModBlockEntities.ME_OUTPUT_HATCH.get(), (var0x, var1) -> new MEOutputHatchStorage(var0x));
      var0.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ME_OUTPUT_HATCH.get(), (var0x, var1) -> var0x.getBufferInventory());
      var0.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ME_OUTPUT_HATCH.get(), (var0x, var1) -> var0x.getFluidHandler());
      var0.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ModBlockEntities.ME_OUTPUT_HATCH.get(), (var0x, var1) -> var0x);
   }
}
