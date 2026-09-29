/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.gui.GuiComponent$MenuFacade
 *  aztech.modern_industrialization.machines.guicomponents.SlotPanel
 *  aztech.modern_industrialization.machines.guicomponents.SlotPanel$SlotType
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.OverdriveBlocker;
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

@Mixin(value={SlotPanel.class})
public abstract class SlotPanelMixin {
    @Shadow
    @Final
    private MachineBlockEntity machine;
    @Shadow
    @Final
    private List<Consumer<GuiComponent.MenuFacade>> slotFactories;
    @Shadow
    @Final
    private List<SlotPanel.SlotType> slotTypes;

    @Unique
    private void aeind$removeBlockedOverdriveSlot() {
        if (!OverdriveBlocker.isBlocked(this.machine)) {
            return;
        }
        for (int i = this.slotTypes.size() - 1; i >= 0; --i) {
            if (this.slotTypes.get(i) != SlotPanel.SlotType.OVERDRIVE_MODULE) continue;
            this.slotTypes.remove(i);
            this.slotFactories.remove(i);
        }
    }

    @Inject(method={"getParams"}, at={@At(value="HEAD")})
    private void aeind$hideBlockedOverdriveSlot(CallbackInfoReturnable<List<SlotPanel.SlotType>> callbackInfoReturnable) {
        this.aeind$removeBlockedOverdriveSlot();
    }

    @Inject(method={"setupMenu"}, at={@At(value="HEAD")})
    private void aeind$removeBlockedOverdriveSlotFromMenu(CallbackInfo callbackInfo) {
        this.aeind$removeBlockedOverdriveSlot();
    }
}

