package aeind.isolation;

import appeng.api.storage.MEStorage;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import java.util.List;

/** One isolated recipe room backed by either legacy MI slots or AE-keyed storage. */
public final class ThreadIsolationRoom {
    private final String id;
    private final List<ConfigurableItemStack> itemInputs;
    private final List<ConfigurableFluidStack> fluidInputs;
   private final RoomInputStorage inputStorage;
   private final MEStorage catalystStorage;

    public ThreadIsolationRoom(
            String id,
            List<ConfigurableItemStack> itemInputs,
            List<ConfigurableFluidStack> fluidInputs) {
        this.id = id;
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.inputStorage = null;
        this.catalystStorage = null;
   }

   public ThreadIsolationRoom(String id, RoomInputStorage inputStorage) {
      this(id, inputStorage, null);
   }

   public ThreadIsolationRoom(String id, RoomInputStorage inputStorage, MEStorage catalystStorage) {
      this.id = id;
      this.itemInputs = List.of();
      this.fluidInputs = List.of();
      this.inputStorage = inputStorage;
      this.catalystStorage = catalystStorage;
    }

    public String id() {
        return this.id;
    }

    public List<ConfigurableItemStack> itemInputs() {
        return this.itemInputs;
    }

    public List<ConfigurableFluidStack> fluidInputs() {
        return this.fluidInputs;
    }

    public RoomInputStorage inputStorage() {
        return this.inputStorage;
    }

   public boolean hasMapStorage() {
      return this.inputStorage != null;
   }

   public MEStorage catalystStorage() {
      return this.catalystStorage;
   }

   public boolean hasCatalystStorage() {
      return this.catalystStorage != null;
   }
}
