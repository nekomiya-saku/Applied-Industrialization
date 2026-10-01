package aeind.mixin;

import aeind.blockentity.VirtualOutputFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfigurableFluidStack.class)
public abstract class ConfigurableFluidStackMixin {
    @Inject(method = "copyList", at = @At("RETURN"))
    private static void aeind$preserveVirtualOutputSlots(
            List<ConfigurableFluidStack> source,
            CallbackInfoReturnable<ArrayList<ConfigurableFluidStack>> callback) {
        ArrayList<ConfigurableFluidStack> copy = callback.getReturnValue();
        for (int i = 0; i < Math.min(source.size(), copy.size()); ++i) {
            if (source.get(i) instanceof VirtualOutputFluidStack virtual) {
                copy.set(i, virtual.detached());
            }
        }
    }
}
