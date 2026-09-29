package aeind.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
   public ModItemModelProvider(PackOutput var1, ExistingFileHelper var2) {
      super(var1, "aeind", var2);
   }

   @Override
   protected void registerModels() {
   }
}
