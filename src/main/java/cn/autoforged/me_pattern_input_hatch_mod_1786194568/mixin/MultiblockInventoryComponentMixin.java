/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.inventory.ConfigurableFluidStack
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 *  aztech.modern_industrialization.machines.components.MultiblockInventoryComponent
 *  aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity
 *  aztech.modern_industrialization.machines.multiblocks.ShapeMatcher
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.CrossThreadParallelHatch;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.IsolatedInputProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationHatch;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiblockInventoryComponent.class})
public abstract class MultiblockInventoryComponentMixin
implements ThreadIsolationAccess {
    private boolean aeind$isolationEnabled;
    private boolean aeind$crossThreadEnabled;
    private boolean aeind$overdriveBlocked;
    private int aeind$maxParallelPerThread = 1;
    private List<ThreadIsolationRoom> aeind$rooms = List.of();

    @Inject(method={"rebuild"}, at={@At(value="RETURN")})
    private void aeind$rebuildRooms(ShapeMatcher shapeMatcher, CallbackInfo callbackInfo) {
        List list = shapeMatcher.getMatchedHatches();
        boolean bl = list.stream().anyMatch(hatchBlockEntity -> hatchBlockEntity instanceof ThreadIsolationHatch);
        boolean bl2 = list.stream().anyMatch(hatchBlockEntity -> hatchBlockEntity instanceof IsolatedInputProvider);
        this.aeind$crossThreadEnabled = list.stream().anyMatch(hatchBlockEntity -> hatchBlockEntity instanceof CrossThreadParallelHatch);
        this.aeind$overdriveBlocked = bl || bl2 || this.aeind$crossThreadEnabled;
        this.aeind$maxParallelPerThread = list.stream().filter(CrossThreadParallelHatch.class::isInstance).map(CrossThreadParallelHatch.class::cast).mapToInt(CrossThreadParallelHatch::aeind$maxParallelPerThread).max().orElse(1);
        boolean bl3 = this.aeind$isolationEnabled = bl || bl2;
        if (!this.aeind$isolationEnabled) {
            this.aeind$rooms = List.of();
            this.aeind$overdriveBlocked = false;
            return;
        }
        ArrayList<ThreadIsolationRoom> arrayList = new ArrayList<ThreadIsolationRoom>(list.size());
        ArrayList<ConfigurableItemStack> arrayList2 = new ArrayList<ConfigurableItemStack>();
        ArrayList<ConfigurableFluidStack> arrayList3 = new ArrayList<ConfigurableFluidStack>();
        for (HatchBlockEntity hatchBlockEntity2 : list) {
            Object object;
            if (hatchBlockEntity2 instanceof IsolatedInputProvider) {
                object = (IsolatedInputProvider)hatchBlockEntity2;
                arrayList.addAll(object.aeind$isolatedInputRooms());
                continue;
            }
            object = new ArrayList();
            ArrayList<ConfigurableFluidStack> arrayList4 = new ArrayList<ConfigurableFluidStack>();
            hatchBlockEntity2.appendItemInputs((List)object);
            hatchBlockEntity2.appendFluidInputs(arrayList4);
            if (bl) {
                if (object.isEmpty() && arrayList4.isEmpty()) continue;
                arrayList.add(new ThreadIsolationRoom("hatch:" + hatchBlockEntity2.getBlockPos().asLong(), (List<ConfigurableItemStack>)object, arrayList4));
                continue;
            }
            arrayList2.addAll((Collection<ConfigurableItemStack>)object);
            arrayList3.addAll(arrayList4);
        }
        if (!(bl || arrayList2.isEmpty() && arrayList3.isEmpty())) {
            arrayList.add(new ThreadIsolationRoom("ordinary", arrayList2, arrayList3));
        }
        this.aeind$rooms = List.copyOf(arrayList);
    }

    @Inject(method={"getItemInputs"}, at={@At(value="RETURN")}, cancellable=true)
    private void aeind$activeItemInputs(CallbackInfoReturnable<List<ConfigurableItemStack>> callbackInfoReturnable) {
        ThreadIsolationRoom threadIsolationRoom = ThreadIsolationState.get();
        if (threadIsolationRoom != null) {
            callbackInfoReturnable.setReturnValue(threadIsolationRoom.itemInputs());
        }
    }

    @Inject(method={"getFluidInputs"}, at={@At(value="RETURN")}, cancellable=true)
    private void aeind$activeFluidInputs(CallbackInfoReturnable<List<ConfigurableFluidStack>> callbackInfoReturnable) {
        ThreadIsolationRoom threadIsolationRoom = ThreadIsolationState.get();
        if (threadIsolationRoom != null) {
            callbackInfoReturnable.setReturnValue(threadIsolationRoom.fluidInputs());
        }
    }

    @Override
    public boolean aeind$isolationEnabled() {
        return this.aeind$isolationEnabled;
    }

    @Override
    public List<ThreadIsolationRoom> aeind$isolationRooms() {
        return Collections.unmodifiableList(this.aeind$rooms);
    }

    @Override
    public boolean aeind$crossThreadEnabled() {
        return this.aeind$crossThreadEnabled;
    }

    @Override
    public int aeind$maxParallelPerThread() {
        return this.aeind$maxParallelPerThread;
    }

    @Override
    public boolean aeind$overdriveBlocked() {
        return this.aeind$overdriveBlocked;
    }
}

