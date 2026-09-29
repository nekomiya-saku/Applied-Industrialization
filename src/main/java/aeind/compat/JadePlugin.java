/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.networking.IGridNode
 *  appeng.api.networking.IInWorldGridNodeHost
 *  aztech.modern_industrialization.api.machine.component.InventoryAccess
 *  aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder
 *  aztech.modern_industrialization.machines.MachineBlock
 *  aztech.modern_industrialization.machines.MachineBlockEntity
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.fml.ModList
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.IComponentProvider
 *  snownee.jade.api.IServerDataProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.IWailaClientRegistration
 *  snownee.jade.api.IWailaCommonRegistration
 *  snownee.jade.api.IWailaPlugin
 *  snownee.jade.api.WailaPlugin
 *  snownee.jade.api.config.IPluginConfig
 */
package aeind.compat;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import aztech.modern_industrialization.api.machine.component.InventoryAccess;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aeind.block.AdvancedPatternInputHatchBlock;
import aeind.block.ExtendedPatternInputHatchBlock;
import aeind.block.MEOutputHatchBlock;
import aeind.blockentity.ExtendedPatternInputHatchBlockEntity;
import aeind.blockentity.MEOutputHatchBlockEntity;
import aeind.blockentity.MEPatternInputHatchBlockEntity;
import aeind.compat.MIParallelHatchCompat;
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
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class JadePlugin
implements IWailaPlugin {
    private static final DeviceStatusProvider PROVIDER = new DeviceStatusProvider();
    private static final CrossThreadStatusProvider CROSS_THREAD_PROVIDER = new CrossThreadStatusProvider();

    public void register(IWailaCommonRegistration iWailaCommonRegistration) {
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEPatternInputHatchBlockEntity.class);
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEOutputHatchBlockEntity.class);
        if (ModList.get().isLoaded("extendedae")) {
            iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
        }
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)CROSS_THREAD_PROVIDER, MachineBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration iWailaClientRegistration) {
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, AdvancedPatternInputHatchBlock.class);
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, MEOutputHatchBlock.class);
        if (ModList.get().isLoaded("extendedae")) {
            iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, ExtendedPatternInputHatchBlock.class);
        }
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)CROSS_THREAD_PROVIDER, MachineBlock.class);
    }

    private static final class DeviceStatusProvider
    implements IBlockComponentProvider,
    IServerDataProvider<BlockAccessor> {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"grid_node_state");

        private DeviceStatusProvider() {
        }

        public ResourceLocation getUid() {
            return UID;
        }

        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            String string = blockAccessor.getServerData().getBoolean("ae2_active") ? "waila.ae2.DeviceOnline" : "waila.ae2.DeviceOffline";
            iTooltip.add((Component)Component.translatable((String)string).withStyle(ChatFormatting.GRAY));
        }

        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            BlockEntity blockEntity = blockAccessor.getBlockEntity();
            boolean bl = false;
            if (blockEntity instanceof IInWorldGridNodeHost) {
                IInWorldGridNodeHost iInWorldGridNodeHost = (IInWorldGridNodeHost)blockEntity;
                IGridNode iGridNode = iInWorldGridNodeHost.getGridNode(null);
                bl = iGridNode != null && iGridNode.isActive();
            }
            compoundTag.putBoolean("ae2_active", bl);
        }
    }

    private static final class CrossThreadStatusProvider
    implements IBlockComponentProvider,
    IServerDataProvider<BlockAccessor> {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"cross_thread_status");
        private static final String ENABLED = "aeind_cross_thread_enabled";
        private static final String THREADS = "aeind_cross_thread_threads";
        private static final String PER_THREAD_LIMIT = "aeind_cross_thread_per_thread_limit";

        private CrossThreadStatusProvider() {
        }

        public ResourceLocation getUid() {
            return UID;
        }

        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            CompoundTag compoundTag = blockAccessor.getServerData();
            if (compoundTag.getBoolean(ENABLED)) {
                iTooltip.add((Component)Component.translatable((String)"jade.aeind.cross_thread_parallel", (Object[])new Object[]{compoundTag.getInt(THREADS), compoundTag.getInt(PER_THREAD_LIMIT)}).withStyle(ChatFormatting.GRAY));
            }
        }

        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            ThreadIsolationAccess threadIsolationAccess;
            CrossThreadControllerAccess crossThreadControllerAccess;
            BlockEntity blockEntity;
            block3: {
                block2: {
                    MultiblockInventoryComponentHolder multiblockInventoryComponentHolder;
                    InventoryAccess inventoryAccess;
                    blockEntity = blockAccessor.getBlockEntity();
                    if (!(blockEntity instanceof CrossThreadControllerAccess)) break block2;
                    crossThreadControllerAccess = (CrossThreadControllerAccess)blockEntity;
                    if (blockEntity instanceof MultiblockInventoryComponentHolder && (inventoryAccess = (multiblockInventoryComponentHolder = (MultiblockInventoryComponentHolder)blockEntity).getMultiblockInventoryComponent()) instanceof ThreadIsolationAccess && (threadIsolationAccess = (ThreadIsolationAccess)inventoryAccess).aeind$crossThreadEnabled()) break block3;
                }
                return;
            }
            int n = Math.max(threadIsolationAccess.aeind$maxParallelPerThread(), MIParallelHatchCompat.getParallelLimit(blockEntity));
            compoundTag.putBoolean(ENABLED, true);
            compoundTag.putInt(THREADS, crossThreadControllerAccess.aeind$getCrossThreadManager().getActiveThreadCount());
            compoundTag.putInt(PER_THREAD_LIMIT, Math.max(1, n));
        }
    }
}

