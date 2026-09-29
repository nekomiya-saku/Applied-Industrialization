/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.components.CrafterComponent
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package aeind.mixin;

import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CrafterComponent.class})
public interface CrafterComponentAccessor {
    @Accessor(value="usedEnergy")
    public long aeind$getUsedEnergy();

    @Accessor(value="usedEnergy")
    public void aeind$setUsedEnergy(long var1);

    @Accessor(value="recipeEnergy")
    public void aeind$setRecipeEnergy(long var1);

    @Accessor(value="recipeMaxEu")
    public void aeind$setRecipeMaxEu(long var1);

    @Accessor(value="efficiencyTicks")
    public void aeind$setEfficiencyTicks(int var1);

    @Accessor(value="maxEfficiencyTicks")
    public void aeind$setMaxEfficiencyTicks(int var1);

    @Accessor(value="activeRecipe")
    public void aeind$setActiveRecipe(RecipeHolder<MachineRecipe> var1);

    @Accessor(value="delayedActiveRecipe")
    public void aeind$setDelayedActiveRecipe(ResourceLocation var1);
}

