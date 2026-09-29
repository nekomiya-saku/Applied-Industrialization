/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.inventory.ConfigurableFluidStack
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 */
package aeind.isolation;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import java.util.List;

public record ThreadIsolationRoom(String id, List<ConfigurableItemStack> itemInputs, List<ConfigurableFluidStack> fluidInputs) {
}

