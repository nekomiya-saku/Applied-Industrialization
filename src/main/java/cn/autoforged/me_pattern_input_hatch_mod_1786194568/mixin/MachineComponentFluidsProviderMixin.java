/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.api.machine.component.FluidAccess;
import aztech.modern_industrialization.compat.jade.server.MachineComponentProvider;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={MachineComponentProvider.Fluids.class})
public abstract class MachineComponentFluidsProviderMixin {
    @ModifyArg(method={"getGroups"}, at=@At(value="INVOKE", target="Laztech/modern_industrialization/compat/jade/server/MachineComponentProvider$Fluids;addFluids(Lsnownee/jade/api/view/ViewGroup;Ljava/util/List;)V", ordinal=2), index=1, remap=false)
    private List<? extends FluidAccess> autoforge$filterEmptyFluidStacks(List<? extends FluidAccess> stacks) {
        return stacks.stream().filter(s -> s.getAmount() > 0L).toList();
    }
}

