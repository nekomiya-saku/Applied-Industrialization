package aeind.isolation;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import java.util.List;

/** One isolated recipe room backed by either legacy MI slots or AE-keyed storage. */
public final class ThreadIsolationRoom {
    private final String id;
    private final List<ConfigurableItemStack> itemInputs;
    private final List<ConfigurableFluidStack> fluidInputs;
    private final RoomInputStorage inputStorage;

    public ThreadIsolationRoom(
            String id,
            List<ConfigurableItemStack> itemInputs,
            List<ConfigurableFluidStack> fluidInputs) {
        this.id = id;
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.inputStorage = null;
    }

    public ThreadIsolationRoom(String id, RoomInputStorage inputStorage) {
        this.id = id;
        this.itemInputs = List.of();
        this.fluidInputs = List.of();
        this.inputStorage = inputStorage;
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
}
