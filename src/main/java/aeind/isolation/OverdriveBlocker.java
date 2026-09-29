package aeind.isolation;

import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OverdriveComponent;

public final class OverdriveBlocker {
   private OverdriveBlocker() {
   }

   public static boolean isBlocked(MachineBlockEntity var0) {
      return var0 instanceof MultiblockInventoryComponentHolder var2
         && var2.getMultiblockInventoryComponent() instanceof ThreadIsolationAccess var1
         && var1.aeind$overdriveBlocked();
   }

   public static boolean clear(MachineBlockEntity var0) {
      boolean[] var1 = new boolean[]{false};
      var0.components.forType(OverdriveComponent.class, var1x -> {
         if (!var1x.getDrop().isEmpty() && var1x instanceof OverdriveComponentAccess var2) {
            var2.aeind$clear();
            var1[0] = true;
         }
      });
      return var1[0];
   }
}
