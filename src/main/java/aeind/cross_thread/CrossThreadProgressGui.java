package aeind.cross_thread;

import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.machines.gui.GuiComponentServer.Type;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

public final class CrossThreadProgressGui implements GuiComponentServer<Unit, CrossThreadProgressGui.Data> {
   public static final Type<Unit, CrossThreadProgressGui.Data> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("aeind", "cross_thread_progress_gui"), StreamCodec.unit(Unit.INSTANCE), CrossThreadProgressGui.Data.STREAM_CODEC
   );
   private final CrossThreadRecipeManager manager;

   public CrossThreadProgressGui(CrossThreadRecipeManager var1) {
      this.manager = var1;
   }

   public Unit getParams() {
      return Unit.INSTANCE;
   }

   public CrossThreadProgressGui.Data extractData() {
      List<CrossThreadProgressGui.ThreadData> var1 = this.manager
         .getProgressSnapshot()
         .threads()
         .stream()
         .map(var0 -> new CrossThreadProgressGui.ThreadData(var0.progress(), var0.parallel(), var0.outputsReady()))
         .toList();
      return new CrossThreadProgressGui.Data(var1, this.manager.getLastEuPerTick());
   }

   @Override
   public Type<Unit, CrossThreadProgressGui.Data> getType() {
      return TYPE;
   }

   public record Data(List<CrossThreadProgressGui.ThreadData> threads, long euPerTick) {
      public static final StreamCodec<RegistryFriendlyByteBuf, CrossThreadProgressGui.Data> STREAM_CODEC = StreamCodec.composite(
         CrossThreadProgressGui.ThreadData.STREAM_CODEC.apply(ByteBufCodecs.list()),
         CrossThreadProgressGui.Data::threads,
         ByteBufCodecs.VAR_LONG,
         CrossThreadProgressGui.Data::euPerTick,
         CrossThreadProgressGui.Data::new
      );
   }

   public record ThreadData(float progress, int parallel, boolean outputsReady) {
      public static final StreamCodec<RegistryFriendlyByteBuf, CrossThreadProgressGui.ThreadData> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT,
         CrossThreadProgressGui.ThreadData::progress,
         ByteBufCodecs.VAR_INT,
         CrossThreadProgressGui.ThreadData::parallel,
         ByteBufCodecs.BOOL,
         CrossThreadProgressGui.ThreadData::outputsReady,
         CrossThreadProgressGui.ThreadData::new
      );
   }
}
