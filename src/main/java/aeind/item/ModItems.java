package aeind.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("aeind");

   public static void register(IEventBus var0) {
      ITEMS.register(var0);
   }
}
