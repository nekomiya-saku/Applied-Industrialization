/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundReturnMaterialPayload() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ServerboundReturnMaterialPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"aeind", (String)"return_material"));
    public static final StreamCodec<ByteBuf, ServerboundReturnMaterialPayload> STREAM_CODEC = StreamCodec.unit((Object)new ServerboundReturnMaterialPayload());

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

