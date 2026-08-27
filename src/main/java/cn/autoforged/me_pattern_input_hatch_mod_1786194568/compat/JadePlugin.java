/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEOutputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.MEPatternInputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEOutputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat.JadePlugin$DeviceStatusProvider;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class JadePlugin
implements IWailaPlugin {
    private static final IBlockComponentProvider PROVIDER = new JadePlugin$DeviceStatusProvider();

    public void register(IWailaCommonRegistration iWailaCommonRegistration) {
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEPatternInputHatchBlockEntity.class);
        iWailaCommonRegistration.registerBlockDataProvider((IServerDataProvider)PROVIDER, MEOutputHatchBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration iWailaClientRegistration) {
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, MEPatternInputHatchBlock.class);
        iWailaClientRegistration.registerBlockComponent((IComponentProvider)PROVIDER, MEOutputHatchBlock.class);
    }
}

