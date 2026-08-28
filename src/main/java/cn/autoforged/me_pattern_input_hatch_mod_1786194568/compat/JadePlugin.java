/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.api.networking.IGridNode
 *  appeng.api.networking.IInWorldGridNodeHost
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntity
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
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEOutputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEPatternInputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ExtendedPatternInputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ExtendedPatternInputHatchBlockEntity;
import net.neoforged.fml.ModList;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    public void register(IWailaCommonRegistration iWailaCommonRegistration) {
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEPatternInputHatchBlockEntity.class);
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEOutputHatchBlockEntity.class);
        if (ModList.get().isLoaded("extendedae")) {
            iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, ExtendedPatternInputHatchBlockEntity.class);
        }
    }

    public void registerClient(IWailaClientRegistration iWailaClientRegistration) {
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, MEPatternInputHatchBlock.class);
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, MEOutputHatchBlock.class);
        if (ModList.get().isLoaded("extendedae")) {
            iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, ExtendedPatternInputHatchBlock.class);
        }
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
}
