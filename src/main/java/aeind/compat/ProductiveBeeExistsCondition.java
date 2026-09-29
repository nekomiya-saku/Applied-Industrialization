package aeind.compat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "aeind")
public record ProductiveBeeExistsCondition(ResourceLocation bee) implements ICondition {
   public static final MapCodec<ProductiveBeeExistsCondition> CODEC = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(ResourceLocation.CODEC.fieldOf("bee").forGetter(ProductiveBeeExistsCondition::bee)).apply(var0, ProductiveBeeExistsCondition::new)
   );

   @SubscribeEvent
   public static void register(RegisterEvent var0) {
      var0.register(
         NeoForgeRegistries.Keys.CONDITION_CODECS, var0x -> var0x.register(ResourceLocation.fromNamespaceAndPath("aeind", "productive_bee_exists"), CODEC)
      );
   }

   @Override
   public boolean test(ICondition.IContext var1) {
      try {
         Class<?> var3 = Class.forName(
            "cy.jdkdigital.productivebees.common.crafting.conditions.BeeExistsCondition", false, ProductiveBeeExistsCondition.class.getClassLoader()
         );
         Object var4 = var3.getConstructor(ResourceLocation.class).newInstance(this.bee);
         ICondition var2;
         return var4 instanceof ICondition && (var2 = (ICondition)var4).test(var1);
      } catch (LinkageError | ReflectiveOperationException var5) {
         return false;
      }
   }

   @Override
   public MapCodec<? extends ICondition> codec() {
      return CODEC;
   }
}
