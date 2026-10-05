package aeind.mixin;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.AbstractModularCrafterComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractModularCrafterComponent.class)
public interface AbstractModularCrafterComponentAccessor {
    @Accessor("activeRecipe")
    Object aeind$getActiveRecipe();

    @Accessor("activeRecipe")
    void aeind$setActiveRecipe(Object recipe);
}
