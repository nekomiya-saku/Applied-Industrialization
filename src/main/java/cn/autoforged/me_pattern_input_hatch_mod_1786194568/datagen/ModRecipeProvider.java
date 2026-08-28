/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.core.definitions.AEItems
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.RecipeProvider
 *  net.minecraft.data.recipes.ShapedRecipeBuilder
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.datagen;

import appeng.core.definitions.AEItems;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ModRecipeProvider
extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, (ItemLike)((ItemLike)ModBlocks.ME_PATTERN_INPUT_HATCH.get())).pattern("III").pattern("IPI").pattern("III").define(Character.valueOf('I'), (ItemLike)Items.IRON_INGOT).define(Character.valueOf('P'), (ItemLike)AEItems.CALCULATION_PROCESSOR).unlockedBy("has_calculation_processor", ModRecipeProvider.has((ItemLike)AEItems.CALCULATION_PROCESSOR)).save(output);
        ShapedRecipeBuilder.shaped((RecipeCategory)RecipeCategory.MISC, (ItemLike)((ItemLike)ModBlocks.ME_OUTPUT_HATCH.get())).pattern("III").pattern("IEI").pattern("III").define(Character.valueOf('I'), (ItemLike)Items.IRON_INGOT).define(Character.valueOf('E'), (ItemLike)AEItems.ENGINEERING_PROCESSOR).unlockedBy("has_engineering_processor", ModRecipeProvider.has((ItemLike)AEItems.ENGINEERING_PROCESSOR)).save(output);
    }
}

