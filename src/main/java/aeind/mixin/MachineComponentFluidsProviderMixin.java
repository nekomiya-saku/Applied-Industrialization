package aeind.mixin;

import aztech.modern_industrialization.api.machine.component.FluidAccess;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "aztech.modern_industrialization.compat.jade.server.MachineComponentProvider$Fluids", remap = false)
public abstract class MachineComponentFluidsProviderMixin {
   @ModifyArg(
      method = "getGroups",
      at = @At(
         value = "INVOKE",
         target = "Laztech/modern_industrialization/compat/jade/server/MachineComponentProvider$Fluids;addFluids(Lsnownee/jade/api/view/ViewGroup;Ljava/util/List;)V",
         ordinal = 2
      ),
      index = 1,
      remap = false
   )
   private List<? extends FluidAccess> aeind$filterEmptyFluidStacks(List<? extends FluidAccess> var1) {
      return var1.stream().filter(var0 -> var0.getAmount() > 0L).toList();
   }
}
