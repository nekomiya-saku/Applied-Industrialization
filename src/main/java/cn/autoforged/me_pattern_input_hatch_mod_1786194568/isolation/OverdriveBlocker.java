/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.api.machine.component.InventoryAccess
 *  aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  aztech.modern_industrialization.machines.components.OverdriveComponent
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation;

import aztech.modern_industrialization.api.machine.component.InventoryAccess;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OverdriveComponent;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.OverdriveComponentAccess;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationAccess;

public final class OverdriveBlocker {
    private OverdriveBlocker() {
    }

    public static boolean isBlocked(MachineBlockEntity machineBlockEntity) {
        ThreadIsolationAccess threadIsolationAccess;
        MultiblockInventoryComponentHolder multiblockInventoryComponentHolder;
        InventoryAccess inventoryAccess;
        return machineBlockEntity instanceof MultiblockInventoryComponentHolder && (inventoryAccess = (multiblockInventoryComponentHolder = (MultiblockInventoryComponentHolder)machineBlockEntity).getMultiblockInventoryComponent()) instanceof ThreadIsolationAccess && (threadIsolationAccess = (ThreadIsolationAccess)inventoryAccess).aeind$overdriveBlocked();
    }

    public static boolean clear(MachineBlockEntity machineBlockEntity) {
        boolean[] blArray = new boolean[]{false};
        machineBlockEntity.components.forType(OverdriveComponent.class, overdriveComponent -> {
            if (!overdriveComponent.getDrop().isEmpty() && overdriveComponent instanceof OverdriveComponentAccess) {
                OverdriveComponentAccess overdriveComponentAccess = (OverdriveComponentAccess)overdriveComponent;
                overdriveComponentAccess.aeind$clear();
                blArray[0] = true;
            }
        });
        return blArray[0];
    }
}

