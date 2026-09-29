package aeind.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = "aeind")
public class DataGenerators {
   @SubscribeEvent
   public static void gatherData(GatherDataEvent var0) {
      DataGenerator var1 = var0.getGenerator();
      PackOutput var2 = var1.getPackOutput();
      ExistingFileHelper var3 = var0.getExistingFileHelper();
      CompletableFuture<HolderLookup.Provider> var4 = var0.getLookupProvider();
      var1.addProvider(var0.includeClient(), new ModBlockStateProvider(var2, var3));
      var1.addProvider(var0.includeClient(), new ModItemModelProvider(var2, var3));
      var1.addProvider(
         var0.includeServer(),
         new LootTableProvider(
            var2, Set.of(), List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), var4
         )
      );
      ModBlockTagsProvider var5 = new ModBlockTagsProvider(var2, var4, var3);
      var1.addProvider(var0.includeServer(), var5);
      var1.addProvider(var0.includeServer(), new ModItemTagsProvider(var2, var4, var5.contentsGetter(), var3));
      var1.addProvider(var0.includeServer(), new ModRecipeProvider(var2, var4));
   }
}
