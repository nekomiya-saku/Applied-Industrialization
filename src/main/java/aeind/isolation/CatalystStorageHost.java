package aeind.isolation;

import appeng.helpers.externalstorage.GenericStackInv;

/** Exposes one shared, non-consumable storage for all rooms owned by a hatch. */
public interface CatalystStorageHost {
   GenericStackInv aeind$catalystStorage();
}
