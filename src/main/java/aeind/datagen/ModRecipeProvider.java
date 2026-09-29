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
package aeind.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public class ModRecipeProvider
extends RecipeProvider {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, completableFuture);
    }

    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Recipes are maintained as hand-authored JSON so optional-mod conditions
        // and the MI/ExtendedAE recipe serializers are preserved during datagen.
    }
}
