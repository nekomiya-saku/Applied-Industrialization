package aeind.cross_thread;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHooks;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEfficiency;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.machine.EfficiencyMIHookContext;
import aeind.mixin.AbstractModularCrafterComponentAccessor;
import aeind.mixin.CrafterComponentAccessor;
import aeind.mixin.MultipliedCrafterComponentAccessor;

import java.util.function.Supplier;

/** Applies Tesseract/MI efficiency hooks to a recipe owned by one isolated thread. */
public final class CrossThreadEfficiencyBridge {
    private CrossThreadEfficiencyBridge() {
    }

    public static int tickStart(MachineBlockEntity machine, CrafterComponent crafter,
                                RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                                int efficiencyTicks, long recipeMaxEu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onTickStart);
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    public static int tickStart(MachineBlockEntity machine, MultipliedCrafterComponent crafter,
                                RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                                int efficiencyTicks, long recipeMaxEu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onTickStart);
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    public static int tickEnd(MachineBlockEntity machine, CrafterComponent crafter,
                              RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                              int efficiencyTicks, long recipeMaxEu, long eu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, (hook, value) -> hook.onTickEnd(value, eu));
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    public static int tickEnd(MachineBlockEntity machine, MultipliedCrafterComponent crafter,
                              RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                              int efficiencyTicks, long recipeMaxEu, long eu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, (hook, value) -> hook.onTickEnd(value, eu));
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    public static int decrease(MachineBlockEntity machine, CrafterComponent crafter,
                               RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                               int efficiencyTicks, long recipeMaxEu) {
        return change(machine, crafter, recipe, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu, false);
    }

    public static int decrease(MachineBlockEntity machine, MultipliedCrafterComponent crafter,
                               RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                               int efficiencyTicks, long recipeMaxEu) {
        return change(machine, crafter, recipe, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu, false);
    }

    public static long maxRecipeEu(MachineBlockEntity machine, CrafterComponent crafter,
                                   RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                                   int efficiencyTicks, long rawMaxRecipeEu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, rawMaxRecipeEu);
            MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onGetRecipeMaxEu);
            return Math.max(1L, context.getMaxRecipeEu());
        });
    }

    public static long maxRecipeEu(MachineBlockEntity machine, MultipliedCrafterComponent crafter,
                                   RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                                   int efficiencyTicks, long rawMaxRecipeEu, int parallel) {
        MultipliedCrafterComponentAccessor multiplier = (MultipliedCrafterComponentAccessor) (Object) crafter;
        int previous = multiplier.aeind$getRecipeMultiplier();
        multiplier.aeind$setRecipeMultiplier(parallel);
        try {
            return invoke(crafter, recipe, () -> {
                EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, rawMaxRecipeEu);
                MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onGetRecipeMaxEu);
                return Math.max(1L, context.getMaxRecipeEu());
            });
        } finally {
            multiplier.aeind$setRecipeMultiplier(previous);
        }
    }

    public static int readNbt(MachineBlockEntity machine, CrafterComponent crafter,
                              RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                              int efficiencyTicks, long recipeMaxEu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onReadNbt);
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    public static int readNbt(MachineBlockEntity machine, MultipliedCrafterComponent crafter,
                              RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                              int efficiencyTicks, long recipeMaxEu) {
        return invoke(crafter, recipe, () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onReadNbt);
            return clamp(context.getEfficiencyTicks(), maxEfficiencyTicks);
        });
    }

    private static int change(MachineBlockEntity machine, Object crafter,
                              RecipeHolder<MachineRecipe> recipe, int maxEfficiencyTicks,
                              int efficiencyTicks, long recipeMaxEu, boolean increase) {
        Supplier<Integer> action = () -> {
            EfficiencyMIHookContext context = context(machine, recipe != null, maxEfficiencyTicks, efficiencyTicks, recipeMaxEu);
            if(increase) {
                MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onIncreaseEfficiencyTicks);
            } else {
                MIHooks.triggerHookEfficiencyListeners(context, MIHookEfficiency::onDecreaseEfficiencyTicks);
            }
            if(context.isCancelled()) return clamp(efficiencyTicks, maxEfficiencyTicks);
            return increase ? clamp(efficiencyTicks + 1, maxEfficiencyTicks) : Math.max(0, efficiencyTicks - 1);
        };
        if(crafter instanceof CrafterComponent normal) return invoke(normal, recipe, action);
        return invoke((MultipliedCrafterComponent) crafter, recipe, action);
    }

    private static EfficiencyMIHookContext context(MachineBlockEntity machine, boolean active,
                                                   int maxEfficiencyTicks, int efficiencyTicks,
                                                   long recipeMaxEu) {
        return new EfficiencyMIHookContext(machine, active, Math.max(0, maxEfficiencyTicks),
                Math.max(0, efficiencyTicks), Math.max(0, recipeMaxEu));
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(Math.max(0, max), value));
    }

    private static <T> T invoke(CrafterComponent crafter, RecipeHolder<MachineRecipe> recipe, Supplier<T> action) {
        CrafterComponentAccessor accessor = (CrafterComponentAccessor) crafter;
        RecipeHolder<MachineRecipe> previous = accessor.aeind$getActiveRecipe();
        accessor.aeind$setActiveRecipe(recipe);
        try {
            return action.get();
        } finally {
            accessor.aeind$setActiveRecipe(previous);
        }
    }

    private static <T> T invoke(MultipliedCrafterComponent crafter, RecipeHolder<MachineRecipe> recipe, Supplier<T> action) {
        AbstractModularCrafterComponentAccessor accessor = (AbstractModularCrafterComponentAccessor) (Object) crafter;
        Object previous = accessor.aeind$getActiveRecipe();
        accessor.aeind$setActiveRecipe(recipe);
        try {
            return action.get();
        } finally {
            accessor.aeind$setActiveRecipe(previous);
        }
    }
}
