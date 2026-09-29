package aeind.compat;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aeind.block.AdvancedPatternInputHatchBlock;
import aeind.block.ExtendedPatternInputHatchBlock;
import aeind.block.MEOutputHatchBlock;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import aeind.blockentity.MEOutputHatchBlockEntity;
import aeind.blockentity.MEPatternInputHatchBlockEntity;
import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.isolation.ThreadIsolationAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class JadePlugin implements IWailaPlugin {
   private static final JadePlugin.DeviceStatusProvider PROVIDER = new JadePlugin.DeviceStatusProvider();
   private static final JadePlugin.CrossThreadStatusProvider CROSS_THREAD_PROVIDER = new JadePlugin.CrossThreadStatusProvider();

   @Override
   public void register(IWailaCommonRegistration var1) {
      var1.registerBlockDataProvider(PROVIDER, MEPatternInputHatchBlockEntity.class);
      var1.registerBlockDataProvider(PROVIDER, MEOutputHatchBlockEntity.class);
      if (ModList.get().isLoaded("extendedae")) {
         var1.registerBlockDataProvider(PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
      }

      var1.registerBlockDataProvider(CROSS_THREAD_PROVIDER, MachineBlockEntity.class);
   }

   @Override
   public void registerClient(IWailaClientRegistration var1) {
      var1.registerBlockComponent(PROVIDER, AdvancedPatternInputHatchBlock.class);
      var1.registerBlockComponent(PROVIDER, MEOutputHatchBlock.class);
      if (ModList.get().isLoaded("extendedae")) {
         var1.registerBlockComponent(PROVIDER, ExtendedPatternInputHatchBlock.class);
      }

      var1.registerBlockComponent(CROSS_THREAD_PROVIDER, MachineBlock.class);
   }

   private static final class CrossThreadStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
      private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("aeind", "cross_thread_status");
      private static final String ENABLED = "aeind_cross_thread_enabled";
      private static final String THREADS = "aeind_cross_thread_threads";
      private static final String PER_THREAD_LIMIT = "aeind_cross_thread_per_thread_limit";

      @Override
      public ResourceLocation getUid() {
         return UID;
      }

      public void appendTooltip(ITooltip var1, BlockAccessor var2, IPluginConfig var3) {
         CompoundTag var4 = var2.getServerData();
         if (var4.getBoolean("aeind_cross_thread_enabled")) {
            var1.add(
               Component.translatable(
                     "jade.aeind.cross_thread_parallel", var4.getInt("aeind_cross_thread_threads"), var4.getInt("aeind_cross_thread_per_thread_limit")
                  )
                  .withStyle(ChatFormatting.GRAY)
            );
         }
      }

      public void appendServerData(CompoundTag var1, BlockAccessor var2) {
         BlockEntity var3 = var2.getBlockEntity();
         if (var3 instanceof CrossThreadControllerAccess var4
            && var3 instanceof MultiblockInventoryComponentHolder var5
            && var5.getMultiblockInventoryComponent() instanceof ThreadIsolationAccess var6
            && var6.aeind$crossThreadEnabled()) {
            int var8 = Math.max(var6.aeind$maxParallelPerThread(), MIParallelHatchCompat.getParallelLimit(var3));
            var1.putBoolean("aeind_cross_thread_enabled", true);
            var1.putInt("aeind_cross_thread_threads", var4.aeind$getCrossThreadManager().getActiveThreadCount());
            var1.putInt("aeind_cross_thread_per_thread_limit", Math.max(1, var8));
         }
      }
   }

   private static final class DeviceStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
      private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("aeind", "grid_node_state");

      @Override
      public ResourceLocation getUid() {
         return UID;
      }

      public void appendTooltip(ITooltip var1, BlockAccessor var2, IPluginConfig var3) {
         String var4 = var2.getServerData().getBoolean("ae2_active") ? "waila.ae2.DeviceOnline" : "waila.ae2.DeviceOffline";
         var1.add(Component.translatable(var4).withStyle(ChatFormatting.GRAY));
      }

      public void appendServerData(CompoundTag var1, BlockAccessor var2) {
         BlockEntity var3 = var2.getBlockEntity();
         boolean var4 = false;
         if (var3 instanceof IInWorldGridNodeHost var5) {
            IGridNode var6 = var5.getGridNode(null);
            var4 = var6 != null && var6.isActive();
         }

         var1.putBoolean("ae2_active", var4);
      }
   }
}
