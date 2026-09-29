package aeind.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModItemTagsProvider extends ItemTagsProvider {
   public ModItemTagsProvider(
      PackOutput var1, CompletableFuture<HolderLookup.Provider> var2, CompletableFuture<TagsProvider.TagLookup<Block>> var3, @Nullable ExistingFileHelper var4
   ) {
      super(var1, var2, var3, "aeind", var4);
   }

   @Override
   protected void addTags(HolderLookup.Provider var1) {
   }
}
