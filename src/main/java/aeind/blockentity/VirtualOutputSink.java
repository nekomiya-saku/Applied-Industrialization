package aeind.blockentity;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;

/** Receives output written to a virtual MI output slot. */
public interface VirtualOutputSink {
    void acceptItemOutput(ItemVariant key, long amount);

    void acceptFluidOutput(FluidVariant key, long amount);
}
