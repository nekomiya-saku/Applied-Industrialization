package aeind.mixin;

import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.CrafterComponent.Behavior;
import aztech.modern_industrialization.machines.components.CrafterComponent.Inventory;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationRoom;
import aeind.isolation.ThreadIsolationState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
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

@Mixin(CrafterComponent.class)
public abstract class CrafterComponentMixin {
   @Shadow
   @Final
   private Inventory inventory;
   @Shadow
   @Final
   private Behavior behavior;
   @Shadow
   private int efficiencyTicks;
   @Shadow
   private RecipeHolder<MachineRecipe> activeRecipe;
   private final Map<MachineRecipe, ThreadIsolationRoom> aeind$recipeRooms = new IdentityHashMap<>();
   private String aeind$selectedRoomId;

   @Redirect(
      method = "updateActiveRecipe",
      at = @At(
         value = "INVOKE",
         target = "Laztech/modern_industrialization/machines/components/CrafterComponent;getRecipes()Ljava/lang/Iterable;",
         remap = false
      )
   )
   private Iterable<RecipeHolder<MachineRecipe>> aeind$isolatedRecipes(CrafterComponent var1) {
      if (this.inventory instanceof ThreadIsolationAccess var2 && var2.aeind$isolationEnabled()) {
         List<ThreadIsolationRoom> var10 = var2.aeind$isolationRooms();
         this.aeind$recipeRooms.clear();
         if (this.efficiencyTicks <= 0) {
            ArrayList<RecipeHolder<MachineRecipe>> var4 = new ArrayList<>();
            ServerLevel var5 = this.behavior.getCrafterWorld();

            for (ThreadIsolationRoom var7 : var10) {
               for (RecipeHolder<MachineRecipe> var9 : CrafterComponent.getRecipes(var5, this.behavior.recipeType(), var7.itemInputs())) {
                  if (CrafterComponent.doInputsMatch(var7.itemInputs(), var7.fluidInputs(), var9.value())
                     && this.aeind$recipeRooms.putIfAbsent(var9.value(), var7) == null) {
                     var4.add(var9);
                  }
               }
            }

            return var4;
         } else {
            if (this.activeRecipe == null) {
               return List.of();
            }

            for (ThreadIsolationRoom var12 : var10) {
               if (CrafterComponent.doInputsMatch(var12.itemInputs(), var12.fluidInputs(), this.activeRecipe.value())) {
                  this.aeind$recipeRooms.put(this.activeRecipe.value(), var12);
                  return Collections.singletonList(this.activeRecipe);
               }
            }

            return List.of();
         }
      } else {
         this.aeind$selectedRoomId = null;
         if (this.efficiencyTicks > 0) {
            return this.activeRecipe == null ? List.of() : Collections.singletonList(this.activeRecipe);
         } else {
            return CrafterComponent.getRecipes(this.behavior.getCrafterWorld(), this.behavior.recipeType(), this.inventory.getItemInputs());
         }
      }
   }

   @Inject(method = "canStartRecipe", at = @At("HEAD"))
   private void aeind$selectRoomForCheck(MachineRecipe var1, CallbackInfoReturnable<Boolean> var2) {
      this.aeind$activateRecipeRoom(var1);
   }

   @Inject(method = "tryStartRecipe", at = @At("HEAD"))
   private void aeind$selectRecipeRoom(MachineRecipe var1, CallbackInfoReturnable<Boolean> var2) {
      this.aeind$activateRecipeRoom(var1);
   }

   @Inject(method = "tryStartRecipe", at = @At("RETURN"))
   private void aeind$rememberRecipeRoom(MachineRecipe var1, CallbackInfoReturnable<Boolean> var2) {
      ThreadIsolationRoom var3 = ThreadIsolationState.get();
      if ((Boolean)var2.getReturnValue() && var3 != null) {
         this.aeind$selectedRoomId = var3.id();
      }

      ThreadIsolationState.clear();
   }

   private void aeind$activateRecipeRoom(MachineRecipe var1) {
      ThreadIsolationRoom var2 = this.aeind$recipeRooms.get(var1);
      if (var2 == null && this.aeind$selectedRoomId != null && this.inventory instanceof ThreadIsolationAccess var3) {
         for (ThreadIsolationRoom var5 : var3.aeind$isolationRooms()) {
            if (var5.id().equals(this.aeind$selectedRoomId)) {
               var2 = var5;
               break;
            }
         }
      }

      if (var2 != null) {
         ThreadIsolationState.set(var2);
      } else {
         ThreadIsolationState.clear();
      }
   }

   @Inject(method = "updateActiveRecipe", at = @At("RETURN"))
   private void aeind$clearRoom(CallbackInfoReturnable<Boolean> var1) {
      ThreadIsolationState.clear();
      this.aeind$recipeRooms.clear();
      if (!(Boolean)var1.getReturnValue() && this.activeRecipe == null) {
         this.aeind$selectedRoomId = null;
      }
   }
}
