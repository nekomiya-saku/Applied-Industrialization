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
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.network;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.HatchPatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.ExtendedPatternProviderMenu;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.network.ServerboundReturnMaterialPayload;
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
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ServerboundReturnMaterialPayload.TYPE, ServerboundReturnMaterialPayload.STREAM_CODEC, ModPayloads::handleReturnMaterial);
    }

    private static void handleReturnMaterial(ServerboundReturnMaterialPayload payload, IPayloadContext context) {
        AbstractContainerMenu abstractContainerMenu;
        Player player = context.player();
        if (player instanceof ServerPlayer && (abstractContainerMenu = player.containerMenu) instanceof HatchPatternProviderMenu) {
            HatchPatternProviderMenu menu = (HatchPatternProviderMenu)abstractContainerMenu;
            menu.returnMaterial();
        } else if (player instanceof ServerPlayer && player.containerMenu instanceof ExtendedPatternProviderMenu menu) {
            menu.returnMaterial();
        }
    }
}
