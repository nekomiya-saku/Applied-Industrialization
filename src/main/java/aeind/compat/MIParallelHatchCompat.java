package aeind.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.neoforged.fml.ModList;

public final class MIParallelHatchCompat {
   public static final String MOD_ID = "miparallelhatch";
   private static final int HARD_PARALLEL_LIMIT = 1024;
   private static final MIParallelHatchCompat.Bridge DISABLED = new MIParallelHatchCompat.Bridge(null, null, null, null);
   private static volatile MIParallelHatchCompat.Bridge bridge;

   private MIParallelHatchCompat() {
   }

   public static int getParallelLimit(Object var0) {
      if (var0 == null) {
         return 1;
      }

      MIParallelHatchCompat.Bridge var1 = getBridge();
      if (!var1.isAvailableFor(var0)) {
         return 1;
      }

      try {
         if (var1.parallelCountMethod.invoke(var0) instanceof Number var3) {
            return Math.clamp(var3.intValue(), 1, 1024);
         }
      } catch (ReflectiveOperationException | RuntimeException var4) {
      }

      return 1;
   }

   public static double getEnergyFactor(Object var0, int var1) {
      int var2 = Math.max(1, var1);
      MIParallelHatchCompat.Bridge var3 = getBridge();
      if (!var3.isAvailableFor(var0)) {
         return var2;
      }

      double var4 = var3.getExtraEnergyMultiplier();
      double var6 = 1.0 + (var2 - 1.0) * var4;
      return Double.isFinite(var6) && !(var6 < 0.0) ? Math.max(var2, var6) : var2;
   }

   public static long scaleEnergy(long var0, int var2, double var3) {
      if (var0 <= 0L) {
         return 0L;
      } else {
         long var5 = saturatedMultiply(var0, Math.max(1, var2));
         if (Double.isFinite(var3) && !(var3 <= 0.0)) {
            double var7 = var0 * var3;
            long var9 = Double.isFinite(var7) && !(var7 >= 9.223372E18F) ? Math.max(0L, Math.round(var7)) : Long.MAX_VALUE;
            return Math.max(var5, var9);
         } else {
            return var5;
         }
      }
   }

   private static MIParallelHatchCompat.Bridge getBridge() {
      MIParallelHatchCompat.Bridge var0 = bridge;
      if (var0 != null) {
         return var0;
      }

      synchronized (MIParallelHatchCompat.class) {
         var0 = bridge;
         if (var0 == null) {
            var0 = createBridge();
            bridge = var0;
         }

         return var0;
      }
   }

   private static MIParallelHatchCompat.Bridge createBridge() {
      if (!ModList.get().isLoaded("miparallelhatch")) {
         return DISABLED;
      }

      try {
         ClassLoader var0 = MIParallelHatchCompat.class.getClassLoader();
         Class<?> var1 = Class.forName("icu.kudikan.miparallelhatch.api.machine.IMultiblockMachineParallelData", false, var0);
         Method var2 = var1.getMethod("miParallelHatch$getParallelCount");
         Object var3 = null;
         Method var4 = null;

         try {
            Class<?> var5 = Class.forName("icu.kudikan.miparallelhatch.MiParallelHatchConfig", false, var0);
            Field var6 = var5.getField("INSTANCE");
            Object var7 = var6.get(null);
            Field var8 = var5.getField("parallelExtraEnergyMultiplier");
            var3 = var8.get(var7);
            var4 = var3.getClass().getMethod("getAsDouble");
         } catch (ReflectiveOperationException | LinkageError | RuntimeException var9) {
         }

         return new MIParallelHatchCompat.Bridge(var1, var2, var3, var4);
      } catch (ReflectiveOperationException | LinkageError | RuntimeException var10) {
         return DISABLED;
      }
   }

   private static long saturatedMultiply(long var0, int var2) {
      if (var0 <= 0L || var2 <= 0) {
         return 0L;
      } else {
         return var0 > Long.MAX_VALUE / var2 ? Long.MAX_VALUE : var0 * var2;
      }
   }

   private record Bridge(Class<?> parallelDataClass, Method parallelCountMethod, Object multiplierValue, Method getAsDoubleMethod) {
      private boolean isAvailableFor(Object var1) {
         return this.parallelDataClass != null && this.parallelCountMethod != null && this.parallelDataClass.isInstance(var1);
      }

      private double getExtraEnergyMultiplier() {
         if (this.multiplierValue != null && this.getAsDoubleMethod != null) {
            try {
               if (this.getAsDoubleMethod.invoke(this.multiplierValue) instanceof Number var2) {
                  double var3 = var2.doubleValue();
                  return Double.isFinite(var3) && var3 >= 0.0 ? var3 : 1.0;
               }
            } catch (ReflectiveOperationException | RuntimeException var5) {
            }

            return 1.0;
         } else {
            return 1.0;
         }
      }
   }
}
