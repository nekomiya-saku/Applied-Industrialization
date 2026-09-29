package aeind.mixin;

import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CrafterComponent.class)
public interface CrafterComponentAccessor {
   @Accessor("usedEnergy")
   long aeind$getUsedEnergy();

   @Accessor("usedEnergy")
   void aeind$setUsedEnergy(long var1);

   @Accessor("recipeEnergy")
   void aeind$setRecipeEnergy(long var1);

   @Accessor("recipeMaxEu")
   void aeind$setRecipeMaxEu(long var1);

   @Accessor("efficiencyTicks")
   void aeind$setEfficiencyTicks(int var1);

   @Accessor("maxEfficiencyTicks")
   void aeind$setMaxEfficiencyTicks(int var1);

   @Accessor("activeRecipe")
   void aeind$setActiveRecipe(RecipeHolder<MachineRecipe> var1);

   @Accessor("delayedActiveRecipe")
   void aeind$setDelayedActiveRecipe(ResourceLocation var1);
}
