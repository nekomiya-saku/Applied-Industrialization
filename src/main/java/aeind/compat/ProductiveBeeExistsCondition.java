/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.common.conditions.ICondition
 *  net.neoforged.neoforge.common.conditions.ICondition$IContext
 *  net.neoforged.neoforge.registries.NeoForgeRegistries$Keys
 *  net.neoforged.neoforge.registries.RegisterEvent
 */
package aeind.compat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="aeind", bus=EventBusSubscriber.Bus.MOD)
public record ProductiveBeeExistsCondition(ResourceLocation bee) implements ICondition
{
    public static final MapCodec<ProductiveBeeExistsCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("bee").forGetter(ProductiveBeeExistsCondition::bee)).apply((Applicative)instance, ProductiveBeeExistsCondition::new));

    @SubscribeEvent
    public static void register(RegisterEvent registerEvent) {
        registerEvent.register(NeoForgeRegistries.Keys.CONDITION_CODECS, registerHelper -> registerHelper.register(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"productive_bee_exists"), CODEC));
    }

    public boolean test(ICondition.IContext iContext) {
        try {
            ICondition iCondition;
            Class<?> clazz = Class.forName("cy.jdkdigital.productivebees.common.crafting.conditions.BeeExistsCondition", false, ProductiveBeeExistsCondition.class.getClassLoader());
            Object obj = clazz.getConstructor(ResourceLocation.class).newInstance(this.bee);
            return obj instanceof ICondition && (iCondition = (ICondition)obj).test(iContext);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return false;
        }
    }

    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

