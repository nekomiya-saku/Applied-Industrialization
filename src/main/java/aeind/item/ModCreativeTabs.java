package aeind.item;

import aeind.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "aeind");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> APPLIED_INDUSTRIALIZATION_TAB = CREATIVE_TABS.register(
      "applied_industrialization_tab",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.aeind"))
         .icon(() -> new ItemStack(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()))
         .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
         .displayItems((var0, var1) -> {
            var1.accept(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
            var1.accept(ModBlocks.ME_OUTPUT_HATCH.get());
            var1.accept(ModBlocks.THREAD_WAREHOUSE.get());
            var1.accept(ModBlocks.CROSS_THREAD_PARALLEL_WAREHOUSE.get());
            if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
               var1.accept(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
            }
         })
         .build()
   );

   public static void register(IEventBus var0) {
      CREATIVE_TABS.register(var0);
   }
}
