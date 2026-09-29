package aeind.datagen;

import aeind.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
   public ModBlockStateProvider(PackOutput var1, ExistingFileHelper var2) {
      super(var1, "aeind", var2);
   }

   @Override
   protected void registerStatesAndModels() {
      this.simpleBlockWithItem(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get(), this.cubeAll(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()));
      if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
         this.simpleBlockWithItem(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get(), this.cubeAll(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()));
      }

      this.simpleBlockWithItem(ModBlocks.ME_OUTPUT_HATCH.get(), this.cubeAll(ModBlocks.ME_OUTPUT_HATCH.get()));
   }
}
