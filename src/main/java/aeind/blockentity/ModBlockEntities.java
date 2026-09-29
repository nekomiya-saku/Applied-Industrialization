package aeind.blockentity;

import aeind.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "aeind");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedPatternInputHatchBlockEntity>> ADVANCED_PATTERN_INPUT_HATCH = BLOCK_ENTITY_TYPES.register(
      "advanced_pattern_input_hatch",
      () -> BlockEntityType.Builder.of(AdvancedPatternInputHatchBlockEntity::new, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEOutputHatchBlockEntity>> ME_OUTPUT_HATCH = BLOCK_ENTITY_TYPES.register(
      "me_output_hatch", () -> BlockEntityType.Builder.of(MEOutputHatchBlockEntity::new, ModBlocks.ME_OUTPUT_HATCH.get()).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThreadWarehouseBlockEntity>> THREAD_WAREHOUSE = BLOCK_ENTITY_TYPES.register(
      "thread_warehouse", () -> BlockEntityType.Builder.of(ThreadWarehouseBlockEntity::new, ModBlocks.THREAD_WAREHOUSE.get()).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrossThreadParallelWarehouseBlockEntity>> CROSS_THREAD_PARALLEL_WAREHOUSE = BLOCK_ENTITY_TYPES.register(
      "cross_thread_parallel_warehouse",
      () -> BlockEntityType.Builder.of(CrossThreadParallelWarehouseBlockEntity::new, ModBlocks.CROSS_THREAD_PARALLEL_WAREHOUSE.get()).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedPatternInputHatchBlockEntity>> ADVANCED_EXTENDED_PATTERN_INPUT_HATCH = ModList.get()
         .isLoaded("extendedae")
      ? BLOCK_ENTITY_TYPES.register(
         "advanced_extended_pattern_input_hatch",
         () -> BlockEntityType.Builder.of(ExtendedPatternInputHatchBlockEntity::new, ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()).build(null)
      )
      : null;

   public static void register(IEventBus var0) {
      BLOCK_ENTITY_TYPES.register(var0);
   }
}
