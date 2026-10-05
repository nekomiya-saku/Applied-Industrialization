package aeind.mixin;

import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultipliedCrafterComponent.class)
public interface MultipliedCrafterComponentAccessor {
    @Accessor("recipeMultiplier")
    int aeind$getRecipeMultiplier();

    @Accessor("recipeMultiplier")
    void aeind$setRecipeMultiplier(int value);
}
