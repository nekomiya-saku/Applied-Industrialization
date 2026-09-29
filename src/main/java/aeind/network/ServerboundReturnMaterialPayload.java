package aeind.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundReturnMaterialPayload() implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<ServerboundReturnMaterialPayload> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("aeind", "return_material")
   );
   public static final StreamCodec<ByteBuf, ServerboundReturnMaterialPayload> STREAM_CODEC = StreamCodec.unit(new ServerboundReturnMaterialPayload());

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
