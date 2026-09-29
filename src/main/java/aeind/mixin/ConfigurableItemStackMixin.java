package aeind.mixin;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aeind.blockentity.LongOutputItemStack;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ConfigurableItemStack.class)
public abstract class ConfigurableItemStackMixin {
    @Inject(method = "copyList", at = @At("RETURN"))
    private static void aeind$preserveLongOutputSlots(
            List<ConfigurableItemStack> source,
            CallbackInfoReturnable<ArrayList<ConfigurableItemStack>> callback) {
        ArrayList<ConfigurableItemStack> copy = callback.getReturnValue();
        for (int i = 0; i < Math.min(source.size(), copy.size()); ++i) {
            if (source.get(i) instanceof LongOutputItemStack) {
                copy.set(i, new LongOutputItemStack(copy.get(i)));
            }
        }
    }
}
