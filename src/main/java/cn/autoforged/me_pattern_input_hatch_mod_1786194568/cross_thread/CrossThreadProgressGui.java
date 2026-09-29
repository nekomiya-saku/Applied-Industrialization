/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aztech.modern_industrialization.machines.gui.GuiComponentServer
 *  aztech.modern_industrialization.machines.gui.GuiComponentServer$Type
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Unit
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread;

import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.cross_thread.CrossThreadRecipeManager;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

public final class CrossThreadProgressGui
implements GuiComponentServer<Unit, Data> {
    public static final GuiComponentServer.Type<Unit, Data> TYPE = new GuiComponentServer.Type(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"cross_thread_progress_gui"), StreamCodec.unit((Object)Unit.INSTANCE), Data.STREAM_CODEC);
    private final CrossThreadRecipeManager manager;

    public CrossThreadProgressGui(CrossThreadRecipeManager crossThreadRecipeManager) {
        this.manager = crossThreadRecipeManager;
    }

    public Unit getParams() {
        return Unit.INSTANCE;
    }

    public Data extractData() {
        List<ThreadData> list = this.manager.getProgressSnapshot().threads().stream().map(threadProgress -> new ThreadData(threadProgress.progress(), threadProgress.parallel(), threadProgress.outputsReady())).toList();
        return new Data(list);
    }

    public GuiComponentServer.Type<Unit, Data> getType() {
        return TYPE;
    }

    public record Data(List<ThreadData> threads) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = ThreadData.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Data::new, Data::threads);
    }

    public record ThreadData(float progress, int parallel, boolean outputsReady) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ThreadData> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.FLOAT, ThreadData::progress, (StreamCodec)ByteBufCodecs.VAR_INT, ThreadData::parallel, (StreamCodec)ByteBufCodecs.BOOL, ThreadData::outputsReady, ThreadData::new);
    }
}

