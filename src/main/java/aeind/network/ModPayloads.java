package aeind.network;

import aeind.gui.ExtendedPatternProviderMenu;
import aeind.gui.HatchPatternProviderMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "aeind")
public class ModPayloads {
   @SubscribeEvent
   public static void register(RegisterPayloadHandlersEvent var0) {
      PayloadRegistrar var1 = var0.registrar("1");
      var1.playToServer(ServerboundReturnMaterialPayload.TYPE, ServerboundReturnMaterialPayload.STREAM_CODEC, ModPayloads::handleReturnMaterial);
   }

   private static void handleReturnMaterial(ServerboundReturnMaterialPayload var0, IPayloadContext var1) {
      Player var3 = var1.player();
      if (var3 instanceof ServerPlayer) {
         AbstractContainerMenu var2 = var3.containerMenu;
         if (var3.containerMenu instanceof HatchPatternProviderMenu) {
            HatchPatternProviderMenu var6 = (HatchPatternProviderMenu)var2;
            var6.returnMaterial();
            return;
         }
      }

      if (var3 instanceof ServerPlayer && var3.containerMenu instanceof ExtendedPatternProviderMenu var4) {
         var4.returnMaterial();
      }
   }
}
