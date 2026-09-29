/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.components.CrafterComponent
 *  aztech.modern_industrialization.machines.components.CrafterComponent$Behavior
 *  aztech.modern_industrialization.machines.components.CrafterComponent$Inventory
 *  aztech.modern_industrialization.machines.recipe.MachineRecipe
 *  aztech.modern_industrialization.machines.recipe.MachineRecipeType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={CrafterComponent.class})
public abstract class CrafterComponentMixin {
    @Shadow
    @Final
    private CrafterComponent.Inventory inventory;
    @Shadow
    @Final
    private CrafterComponent.Behavior behavior;
    @Shadow
    private int efficiencyTicks;
    @Shadow
    private RecipeHolder<MachineRecipe> activeRecipe;
    private final Map<MachineRecipe, ThreadIsolationRoom> aeind$recipeRooms = new IdentityHashMap<MachineRecipe, ThreadIsolationRoom>();
    private String aeind$selectedRoomId;

    @Redirect(method={"updateActiveRecipe"}, at=@At(value="INVOKE", target="Laztech/modern_industrialization/machines/components/CrafterComponent;getRecipes()Ljava/lang/Iterable;", remap=false))
    private Iterable<RecipeHolder<MachineRecipe>> aeind$isolatedRecipes(CrafterComponent crafterComponent) {
        ThreadIsolationAccess threadIsolationAccess;
        Object object = this.inventory;
        if (!(object instanceof ThreadIsolationAccess) || !(threadIsolationAccess = (ThreadIsolationAccess)object).aeind$isolationEnabled()) {
            this.aeind$selectedRoomId = null;
            if (this.efficiencyTicks > 0) {
                return this.activeRecipe == null ? List.of() : Collections.singletonList(this.activeRecipe);
            }
            return CrafterComponent.getRecipes((ServerLevel)this.behavior.getCrafterWorld(), (MachineRecipeType)this.behavior.recipeType(), (List)this.inventory.getItemInputs());
        }
        object = threadIsolationAccess.aeind$isolationRooms();
        this.aeind$recipeRooms.clear();
        if (this.efficiencyTicks > 0) {
            if (this.activeRecipe == null) {
                return List.of();
            }
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                ThreadIsolationRoom threadIsolationRoom = (ThreadIsolationRoom)iterator.next();
                if (!CrafterComponent.doInputsMatch(threadIsolationRoom.itemInputs(), threadIsolationRoom.fluidInputs(), (MachineRecipe)((MachineRecipe)this.activeRecipe.value()))) continue;
                this.aeind$recipeRooms.put((MachineRecipe)this.activeRecipe.value(), threadIsolationRoom);
                return Collections.singletonList(this.activeRecipe);
            }
            return List.of();
        }
        ArrayList<RecipeHolder<MachineRecipe>> arrayList = new ArrayList<RecipeHolder<MachineRecipe>>();
        ServerLevel serverLevel = this.behavior.getCrafterWorld();
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            ThreadIsolationRoom threadIsolationRoom = (ThreadIsolationRoom)iterator.next();
            for (RecipeHolder recipeHolder : CrafterComponent.getRecipes((ServerLevel)serverLevel, (MachineRecipeType)this.behavior.recipeType(), threadIsolationRoom.itemInputs())) {
                if (!CrafterComponent.doInputsMatch(threadIsolationRoom.itemInputs(), threadIsolationRoom.fluidInputs(), (MachineRecipe)((MachineRecipe)recipeHolder.value())) || this.aeind$recipeRooms.putIfAbsent((MachineRecipe)recipeHolder.value(), threadIsolationRoom) != null) continue;
                arrayList.add((RecipeHolder<MachineRecipe>)recipeHolder);
            }
        }
        return arrayList;
    }

    @Inject(method={"canStartRecipe"}, at={@At(value="HEAD")})
    private void aeind$selectRoomForCheck(MachineRecipe machineRecipe, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        this.aeind$activateRecipeRoom(machineRecipe);
    }

    @Inject(method={"tryStartRecipe"}, at={@At(value="HEAD")})
    private void aeind$selectRecipeRoom(MachineRecipe machineRecipe, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        this.aeind$activateRecipeRoom(machineRecipe);
    }

    @Inject(method={"tryStartRecipe"}, at={@At(value="RETURN")})
    private void aeind$rememberRecipeRoom(MachineRecipe machineRecipe, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ThreadIsolationRoom threadIsolationRoom = ThreadIsolationState.get();
        if (((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue() && threadIsolationRoom != null) {
            this.aeind$selectedRoomId = threadIsolationRoom.id();
        }
        ThreadIsolationState.clear();
    }

    private void aeind$activateRecipeRoom(MachineRecipe machineRecipe) {
        Object object;
        ThreadIsolationRoom threadIsolationRoom = this.aeind$recipeRooms.get(machineRecipe);
        if (threadIsolationRoom == null && this.aeind$selectedRoomId != null && (object = this.inventory) instanceof ThreadIsolationAccess) {
            ThreadIsolationAccess threadIsolationAccess = (ThreadIsolationAccess)object;
            for (ThreadIsolationRoom threadIsolationRoom2 : threadIsolationAccess.aeind$isolationRooms()) {
                if (!threadIsolationRoom2.id().equals(this.aeind$selectedRoomId)) continue;
                threadIsolationRoom = threadIsolationRoom2;
                break;
            }
        }
        if (threadIsolationRoom != null) {
            ThreadIsolationState.set(threadIsolationRoom);
        } else {
            ThreadIsolationState.clear();
        }
    }

    @Inject(method={"updateActiveRecipe"}, at={@At(value="RETURN")})
    private void aeind$clearRoom(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ThreadIsolationState.clear();
        this.aeind$recipeRooms.clear();
        if (!((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue() && this.activeRecipe == null) {
            this.aeind$selectedRoomId = null;
        }
    }
}

