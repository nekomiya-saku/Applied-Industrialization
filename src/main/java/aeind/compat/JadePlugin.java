package aeind.compat;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aeind.block.AdvancedPatternInputHatchBlock;
import aeind.block.ExtendedPatternInputHatchBlock;
import aeind.block.MEOutputHatchBlock;
import aeind.blockentity.AdvancedPatternInputHatchBlockEntity;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import aeind.blockentity.MEOutputHatchBlockEntity;
import aeind.blockentity.MEPatternInputHatchBlockEntity;
import aeind.cross_thread.CrossThreadControllerAccess;
import aeind.isolation.IsolatedInputProvider;
import aeind.isolation.CatalystStorageHost;
import aeind.isolation.ThreadIsolationRoom;
import aeind.isolation.ThreadIsolationAccess;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

@WailaPlugin
public final class JadePlugin implements IWailaPlugin {
   private static final JadePlugin.DeviceStatusProvider PROVIDER = new JadePlugin.DeviceStatusProvider();
   private static final JadePlugin.CrossThreadStatusProvider CROSS_THREAD_PROVIDER = new JadePlugin.CrossThreadStatusProvider();
   private static final JadePlugin.IsolatedItemStorageProvider INPUT_ITEM_PROVIDER = new JadePlugin.IsolatedItemStorageProvider();
   private static final JadePlugin.IsolatedFluidStorageProvider INPUT_FLUID_PROVIDER = new JadePlugin.IsolatedFluidStorageProvider();

   @Override
   public void register(IWailaCommonRegistration var1) {
      var1.registerBlockDataProvider(PROVIDER, MEPatternInputHatchBlockEntity.class);
      var1.registerBlockDataProvider(PROVIDER, MEOutputHatchBlockEntity.class);
      var1.registerItemStorage(INPUT_ITEM_PROVIDER, AdvancedPatternInputHatchBlockEntity.class);
      var1.registerFluidStorage(INPUT_FLUID_PROVIDER, AdvancedPatternInputHatchBlockEntity.class);
      if (ModList.get().isLoaded("extendedae")) {
         var1.registerBlockDataProvider(PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
         var1.registerItemStorage(INPUT_ITEM_PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
         var1.registerFluidStorage(INPUT_FLUID_PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
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

      var1.registerItemStorageClient(INPUT_ITEM_PROVIDER);
      var1.registerFluidStorageClient(INPUT_FLUID_PROVIDER);
      var1.registerBlockComponent(CROSS_THREAD_PROVIDER, MachineBlock.class);
   }

   private static IsolatedInputProvider getIsolatedInputs(Accessor<?> accessor) {
      Object target = accessor.getTarget();
      if (target instanceof IsolatedInputProvider provider) {
         return provider;
      }
      if (accessor instanceof BlockAccessor blockAccessor
         && blockAccessor.getBlockEntity() instanceof IsolatedInputProvider provider) {
         return provider;
      }
      return null;
   }

   private static CatalystStorageHost getCatalystHost(Accessor<?> accessor) {
      Object target = accessor.getTarget();
      if (target instanceof CatalystStorageHost host) {
         return host;
      }
      if (accessor instanceof BlockAccessor blockAccessor
         && blockAccessor.getBlockEntity() instanceof CatalystStorageHost host) {
         return host;
      }
      return null;
   }

   private static Component roomTitle(String roomId) {
      return Component.translatable("jade.aeind.input_room", roomId == null ? "?" : roomId);
   }

   private static String formatAmount(long amount) {
      return NumberFormat.getIntegerInstance().format(amount);
   }

   private static final class IsolatedItemStorageProvider
      implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {
      private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("aeind", "isolated_input_items");
      private static final String AMOUNTS = "aeind_amounts";

      @Override
      public ResourceLocation getUid() {
         return UID;
      }

      @Override
      public int getDefaultPriority() {
         return -1000;
      }

      @Override
      public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
         IsolatedInputProvider provider = getIsolatedInputs(accessor);
         if (provider == null) {
            return null;
         }

         List<ViewGroup<ItemStack>> groups = new ArrayList<>();
         List<ThreadIsolationRoom> rooms = provider.aeind$isolatedInputRooms();
         for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            ThreadIsolationRoom room = rooms.get(roomIndex);
            if (!room.hasMapStorage()) {
               continue;
            }

            List<ItemStack> items = new ArrayList<>();
            List<Long> amounts = new ArrayList<>();
            for (Map.Entry<appeng.api.stacks.AEKey, Long> entry : room.inputStorage().snapshot().entrySet()) {
               if (entry.getKey() instanceof AEItemKey itemKey && entry.getValue() > 0L) {
                  items.add(itemKey.toStack(1));
                  amounts.add(entry.getValue());
               }
            }
            if (items.isEmpty()) {
               continue;
            }

            ViewGroup<ItemStack> group = new ViewGroup<>(items);
            group.id = Integer.toString(roomIndex + 1);
            long[] serializedAmounts = new long[amounts.size()];
            for (int i = 0; i < amounts.size(); i++) {
               serializedAmounts[i] = amounts.get(i);
            }
            group.getExtraData().putLongArray(AMOUNTS, serializedAmounts);
            groups.add(group);
         }
         CatalystStorageHost catalystHost = getCatalystHost(accessor);
         if (catalystHost != null) {
            List<ItemStack> items = new ArrayList<>();
            List<Long> amounts = new ArrayList<>();
            for (GenericStack stack : catalystHost.aeind$catalystStorage().toList()) {
               if (stack != null && stack.what() instanceof AEItemKey itemKey && stack.amount() > 0L) {
                  items.add(itemKey.toStack(1));
                  amounts.add(stack.amount());
               }
            }
            if (!items.isEmpty()) {
               ViewGroup<ItemStack> group = new ViewGroup<>(items);
               group.id = "catalysts";
               long[] serializedAmounts = new long[amounts.size()];
               for (int i = 0; i < amounts.size(); i++) serializedAmounts[i] = amounts.get(i);
               group.getExtraData().putLongArray(AMOUNTS, serializedAmounts);
               groups.add(group);
            }
         }
         return groups;
      }

      @Override
      public List<ClientViewGroup<ItemView>> getClientGroups(
         Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
         return ClientViewGroup.map(groups, ItemView::new, (group, clientGroup) -> {
            clientGroup.title = group.id.equals("catalysts")
               ? Component.translatable("jade.aeind.catalysts")
               : roomTitle(group.id);
            long[] amounts = group.getExtraData().getLongArray(AMOUNTS);
            for (int i = 0; i < clientGroup.views.size() && i < amounts.length; i++) {
               clientGroup.views.get(i).amountText(formatAmount(amounts[i]));
            }
         });
      }
   }

   private static final class IsolatedFluidStorageProvider
      implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
      private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("aeind", "isolated_input_fluids");
      private static final String AMOUNTS = "aeind_amounts";

      @Override
      public ResourceLocation getUid() {
         return UID;
      }

      @Override
      public int getDefaultPriority() {
         return -1000;
      }

      @Override
      public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
         IsolatedInputProvider provider = getIsolatedInputs(accessor);
         if (provider == null) {
            return null;
         }

         List<ViewGroup<CompoundTag>> groups = new ArrayList<>();
         List<ThreadIsolationRoom> rooms = provider.aeind$isolatedInputRooms();
         for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            ThreadIsolationRoom room = rooms.get(roomIndex);
            if (!room.hasMapStorage()) {
               continue;
            }

            List<CompoundTag> fluids = new ArrayList<>();
            List<Long> amounts = new ArrayList<>();
            for (Map.Entry<appeng.api.stacks.AEKey, Long> entry : room.inputStorage().snapshot().entrySet()) {
               if (entry.getKey() instanceof AEFluidKey fluidKey && entry.getValue() > 0L) {
                  long amount = entry.getValue();
                  fluids.add(FluidView.writeDefault(
                     JadeFluidObject.of(fluidKey.getFluid(), amount, fluidKey.toStack(1).getComponentsPatch()), amount));
                  amounts.add(amount);
               }
            }
            if (fluids.isEmpty()) {
               continue;
            }

            ViewGroup<CompoundTag> group = new ViewGroup<>(fluids);
            group.id = Integer.toString(roomIndex + 1);
            long[] serializedAmounts = new long[amounts.size()];
            for (int i = 0; i < amounts.size(); i++) {
               serializedAmounts[i] = amounts.get(i);
            }
            group.getExtraData().putLongArray(AMOUNTS, serializedAmounts);
            groups.add(group);
         }
         CatalystStorageHost catalystHost = getCatalystHost(accessor);
         if (catalystHost != null) {
            List<CompoundTag> fluids = new ArrayList<>();
            List<Long> amounts = new ArrayList<>();
            for (GenericStack stack : catalystHost.aeind$catalystStorage().toList()) {
               if (stack != null && stack.what() instanceof AEFluidKey fluidKey && stack.amount() > 0L) {
                  long amount = stack.amount();
                  fluids.add(FluidView.writeDefault(
                     JadeFluidObject.of(fluidKey.getFluid(), amount, fluidKey.toStack(1).getComponentsPatch()), amount));
                  amounts.add(amount);
               }
            }
            if (!fluids.isEmpty()) {
               ViewGroup<CompoundTag> group = new ViewGroup<>(fluids);
               group.id = "catalysts";
               long[] serializedAmounts = new long[amounts.size()];
               for (int i = 0; i < amounts.size(); i++) serializedAmounts[i] = amounts.get(i);
               group.getExtraData().putLongArray(AMOUNTS, serializedAmounts);
               groups.add(group);
            }
         }
         return groups;
      }

      @Override
      public List<ClientViewGroup<FluidView>> getClientGroups(
         Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
         return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {
            clientGroup.title = group.id.equals("catalysts")
               ? Component.translatable("jade.aeind.catalysts")
               : roomTitle(group.id);
            long[] amounts = group.getExtraData().getLongArray(AMOUNTS);
            for (int i = 0; i < clientGroup.views.size() && i < amounts.length; i++) {
               clientGroup.views.get(i).overrideText = Component.literal(formatAmount(amounts[i]) + " mB");
            }
         });
      }
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
