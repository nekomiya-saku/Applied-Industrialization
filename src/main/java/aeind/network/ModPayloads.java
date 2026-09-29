/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package aeind.network;

import aeind.gui.ExtendedPatternProviderMenu;
import aeind.gui.HatchPatternProviderMenu;
import aeind.network.ServerboundReturnMaterialPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid="aeind", bus=EventBusSubscriber.Bus.MOD)
public class ModPayloads {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent registerPayloadHandlersEvent) {
        PayloadRegistrar payloadRegistrar = registerPayloadHandlersEvent.registrar("1");
        payloadRegistrar.playToServer(ServerboundReturnMaterialPayload.TYPE, ServerboundReturnMaterialPayload.STREAM_CODEC, ModPayloads::handleReturnMaterial);
    }

    private static void handleReturnMaterial(ServerboundReturnMaterialPayload serverboundReturnMaterialPayload, IPayloadContext iPayloadContext) {
        AbstractContainerMenu abstractContainerMenu;
        AbstractContainerMenu abstractContainerMenu2;
        Player player = iPayloadContext.player();
        if (player instanceof ServerPlayer && (abstractContainerMenu2 = player.containerMenu) instanceof HatchPatternProviderMenu) {
            HatchPatternProviderMenu hatchPatternProviderMenu = (HatchPatternProviderMenu)abstractContainerMenu2;
            hatchPatternProviderMenu.returnMaterial();
        } else if (player instanceof ServerPlayer && (abstractContainerMenu = player.containerMenu) instanceof ExtendedPatternProviderMenu) {
            ExtendedPatternProviderMenu extendedPatternProviderMenu = (ExtendedPatternProviderMenu)abstractContainerMenu;
            extendedPatternProviderMenu.returnMaterial();
        }
    }
}

