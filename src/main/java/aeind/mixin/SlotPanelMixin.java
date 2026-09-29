package aeind.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.gui.GuiComponent.MenuFacade;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel.SlotType;
import aeind.isolation.OverdriveBlocker;
import java.util.List;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlotPanel.class)
public abstract class SlotPanelMixin {
   @Shadow
   @Final
   private MachineBlockEntity machine;
   @Shadow
   @Final
   private List<Consumer<MenuFacade>> slotFactories;
   @Shadow
   @Final
   private List<SlotType> slotTypes;

   @Unique
   private void aeind$removeBlockedOverdriveSlot() {
      if (OverdriveBlocker.isBlocked(this.machine)) {
         for (int var1 = this.slotTypes.size() - 1; var1 >= 0; var1--) {
            if (this.slotTypes.get(var1) == SlotType.OVERDRIVE_MODULE) {
               this.slotTypes.remove(var1);
               this.slotFactories.remove(var1);
            }
         }
      }
   }

   @Inject(method = "getParams", at = @At("HEAD"))
   private void aeind$hideBlockedOverdriveSlot(CallbackInfoReturnable<List<SlotType>> var1) {
      this.aeind$removeBlockedOverdriveSlot();
   }

   @Inject(method = "setupMenu", at = @At("HEAD"))
   private void aeind$removeBlockedOverdriveSlotFromMenu(CallbackInfo var1) {
      this.aeind$removeBlockedOverdriveSlot();
   }
}
