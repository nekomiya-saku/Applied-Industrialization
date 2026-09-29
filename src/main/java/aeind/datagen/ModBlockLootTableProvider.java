package aeind.datagen;

import aeind.block.ModBlocks;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
   protected ModBlockLootTableProvider(HolderLookup.Provider var1) {
      super(Set.of(), FeatureFlags.DEFAULT_FLAGS, var1);
   }

   @Override
   protected void generate() {
      this.dropSelf(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
      if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
         this.dropSelf(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
      }

      this.dropSelf(ModBlocks.ME_OUTPUT_HATCH.get());
   }

   @Override
   protected Iterable<Block> getKnownBlocks() {
      return ModBlocks.BLOCKS.getEntries().stream().map(var0 -> (Block)var0.get()).toList();
   }
}
