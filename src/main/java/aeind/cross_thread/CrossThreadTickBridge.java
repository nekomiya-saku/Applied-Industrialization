package aeind.cross_thread;

import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationRoom;
import aeind.mixin.CrafterComponentAccessor;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.MultipliedCrafterComponent;

/** Shared tick interception for machine classes that own a CrafterComponent. */
public final class CrossThreadTickBridge {
    private CrossThreadTickBridge() {
    }

    public static boolean tickNormal(
            CrossThreadRecipeManager manager,
            MachineBlockEntity machine,
            CrafterComponent component
    ) {
        if (!(component.getInventory() instanceof ThreadIsolationAccess access)) {
            return component.tickRecipe();
        }

        boolean crossThreadEnabled = access.aeind$crossThreadEnabled();
        boolean hasMapStorageRooms = access.aeind$isolationRooms().stream().anyMatch(ThreadIsolationRoom::hasMapStorage);
        if (crossThreadEnabled || hasMapStorageRooms) {
            CrafterComponentAccessor accessor = (CrafterComponentAccessor) component;
            if (!manager.hasWork() && accessor.aeind$getUsedEnergy() > 0L) {
                return component.tickRecipe();
            }

            if (accessor.aeind$getUsedEnergy() == 0L) {
                resetIdleLegacyCrafter(accessor);
            }

            return manager.tick(machine, component, access, crossThreadEnabled);
        }

        return manager.hasWork()
                ? manager.tick(machine, component, access, false)
                : component.tickRecipe();
    }

    public static boolean resumeNormal(CrossThreadRecipeManager manager, CrafterComponent component) {
        if (!(component.getInventory() instanceof ThreadIsolationAccess access)) {
            return component.tryContinueRecipe();
        }

        // A saved cross-thread state owns the in-flight recipe. If no such state
        // exists, keep MI's native resume path for an ordinary in-flight recipe.
        if (manager.hasWork()) {
            return true;
        }
        return component.tryContinueRecipe();
    }

    public static boolean tickTesseract(
            CrossThreadRecipeManager manager,
            MachineBlockEntity machine,
            MultipliedCrafterComponent component
    ) {
        if (!(component.getInventory() instanceof ThreadIsolationAccess access)) {
            return component.tickRecipe();
        }

        boolean mapRooms = access.aeind$isolationRooms().stream().anyMatch(ThreadIsolationRoom::hasMapStorage);
        if (access.aeind$crossThreadEnabled() || mapRooms || manager.hasWork()) {
            return manager.tickTesseract(machine, component, access, access.aeind$crossThreadEnabled());
        }
        return component.tickRecipe();
    }

    public static boolean resumeTesseract(CrossThreadRecipeManager manager, MultipliedCrafterComponent component) {
        if (!(component.getInventory() instanceof ThreadIsolationAccess access)) {
            return component.tryContinueRecipe();
        }

        boolean mapRooms = access.aeind$isolationRooms().stream().anyMatch(ThreadIsolationRoom::hasMapStorage);
        if (access.aeind$crossThreadEnabled() || mapRooms || manager.hasWork()) {
            // The custom manager owns the in-flight recipe after a reload. The
            // native Tesseract resume state must not run in parallel with it.
            return true;
        }
        return component.tryContinueRecipe();
    }

    private static void resetIdleLegacyCrafter(CrafterComponentAccessor accessor) {
        accessor.aeind$setActiveRecipe(null);
        accessor.aeind$setDelayedActiveRecipe(null);
        accessor.aeind$setUsedEnergy(0L);
        accessor.aeind$setRecipeEnergy(0L);
        accessor.aeind$setRecipeMaxEu(0L);
        accessor.aeind$setEfficiencyTicks(0);
        accessor.aeind$setMaxEfficiencyTicks(0);
    }
}
