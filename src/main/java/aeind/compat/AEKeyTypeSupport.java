package aeind.compat;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import java.util.Set;

/** Provides the AE key types that are registered by the currently loaded AE2 integrations. */
public final class AEKeyTypeSupport {
   private AEKeyTypeSupport() {
   }

   /**
    * Snapshot the registered types when a block entity creates its inventory.
    * This is intentionally not called from a tick loop.
    */
   public static Set<AEKeyType> registeredTypes() {
      return Set.copyOf(AEKeyTypes.getAll());
   }
}
