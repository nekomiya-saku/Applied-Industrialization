package aeind.block;

import aeind.item.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("aeind");
   public static final DeferredBlock<AdvancedPatternInputHatchBlock> ADVANCED_PATTERN_INPUT_HATCH = registerBlock(
      "advanced_pattern_input_hatch",
      () -> new AdvancedPatternInputHatchBlock(
         BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(var0 -> 0)
      )
   );
   public static final DeferredBlock<MEOutputHatchBlock> ME_OUTPUT_HATCH = registerBlock(
      "me_output_hatch",
      () -> new MEOutputHatchBlock(
         BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(var0 -> 0)
      )
   );
   public static final DeferredBlock<ThreadWarehouseBlock> THREAD_WAREHOUSE = registerBlock(
      "thread_warehouse",
      () -> new ThreadWarehouseBlock(
         BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      )
   );
   public static final DeferredBlock<CrossThreadParallelWarehouseBlock> CROSS_THREAD_PARALLEL_WAREHOUSE = registerBlock(
      "cross_thread_parallel_warehouse",
      () -> new CrossThreadParallelWarehouseBlock(
         BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      )
   );
   public static final DeferredBlock<ExtendedPatternInputHatchBlock> ADVANCED_EXTENDED_PATTERN_INPUT_HATCH = ModList.get().isLoaded("extendedae")
      ? registerBlock(
         "advanced_extended_pattern_input_hatch",
         () -> new ExtendedPatternInputHatchBlock(
            BlockBehaviour.Properties.of()
               .mapColor(MapColor.METAL)
               .strength(2.0F, 6.0F)
               .sound(SoundType.METAL)
               .requiresCorrectToolForDrops()
               .lightLevel(var0 -> 0)
         )
      )
      : null;

   private static <T extends Block> DeferredBlock<T> registerBlock(String var0, Supplier<? extends T> var1) {
      DeferredBlock<T> var2 = BLOCKS.register(var0, var1);
      ModItems.ITEMS.registerSimpleBlockItem(var2);
      return var2;
   }

   public static void register(IEventBus var0) {
      BLOCKS.register(var0);
   }
}
