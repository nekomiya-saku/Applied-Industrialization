package aeind.datagen;

import appeng.core.definitions.AEItems;
import aeind.block.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends RecipeProvider {
   public ModRecipeProvider(PackOutput var1, CompletableFuture<HolderLookup.Provider> var2) {
      super(var1, var2);
   }

   @Override
   protected void buildRecipes(RecipeOutput var1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get())
         .pattern("III")
         .pattern("IPI")
         .pattern("III")
         .define('I', Items.IRON_INGOT)
         .define('P', AEItems.CALCULATION_PROCESSOR)
         .unlockedBy("has_calculation_processor", has(AEItems.CALCULATION_PROCESSOR))
         .save(var1);
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ME_OUTPUT_HATCH.get())
         .pattern("III")
         .pattern("IEI")
         .pattern("III")
         .define('I', Items.IRON_INGOT)
         .define('E', AEItems.ENGINEERING_PROCESSOR)
         .unlockedBy("has_engineering_processor", has(AEItems.ENGINEERING_PROCESSOR))
         .save(var1);
   }
}
