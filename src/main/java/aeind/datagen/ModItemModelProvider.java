/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.neoforged.neoforge.client.model.generators.ItemModelProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package aeind.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider
extends ItemModelProvider {
    public ModItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, "aeind", existingFileHelper);
    }

    protected void registerModels() {
    }
}

