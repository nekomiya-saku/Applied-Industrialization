/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

final class JadePlugin$DeviceStatusProvider
implements IBlockComponentProvider,
IServerDataProvider {
    private JadePlugin$DeviceStatusProvider() {
    }

    public ResourceLocation getUid() {
        return new ResourceLocation("aeind", "grid_node_state");
    }

    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().getBoolean("ae2_active")) {
            iTooltip.add((Component)Component.translatable((String)"waila.ae2.DeviceOnline").withStyle(ChatFormatting.GRAY));
        } else {
            iTooltip.add((Component)Component.translatable((String)"waila.ae2.DeviceOffline").withStyle(ChatFormatting.GRAY));
        }
    }

    public void appendTooltip(ITooltip iTooltip, Accessor accessor, IPluginConfig iPluginConfig) {
        this.appendTooltip(iTooltip, (BlockAccessor)accessor, iPluginConfig);
    }

    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        IGridNode iGridNode;
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof IInWorldGridNodeHost && (iGridNode = ((IInWorldGridNodeHost)blockEntity).getGridNode(null)) != null && iGridNode.isActive()) {
            compoundTag.putBoolean("ae2_active", true);
            return;
        }
        compoundTag.putBoolean("ae2_active", false);
    }
}

