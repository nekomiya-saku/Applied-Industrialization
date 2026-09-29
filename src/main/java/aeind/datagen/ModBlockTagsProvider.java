package aeind.datagen;

import aeind.block.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
   public ModBlockTagsProvider(PackOutput var1, CompletableFuture<HolderLookup.Provider> var2, @Nullable ExistingFileHelper var3) {
      super(var1, var2, "aeind", var3);
   }

   @Override
   protected void addTags(HolderLookup.Provider var1) {
      IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> var2 = this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
         .add(ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get())
         .add(ModBlocks.ME_OUTPUT_HATCH.get());
      if (ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
         var2.add(ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get());
      }
   }
}
