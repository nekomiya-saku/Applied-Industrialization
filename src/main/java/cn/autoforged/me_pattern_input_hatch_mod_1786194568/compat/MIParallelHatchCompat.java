/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.ModList
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.neoforged.fml.ModList;

public final class MIParallelHatchCompat {
    public static final String MOD_ID = "miparallelhatch";
    private static final int HARD_PARALLEL_LIMIT = 1024;
    private static final Bridge DISABLED = new Bridge(null, null, null, null);
    private static volatile Bridge bridge;

    private MIParallelHatchCompat() {
    }

    public static int getParallelLimit(Object object) {
        if (object == null) {
            return 1;
        }
        Bridge bridge = MIParallelHatchCompat.getBridge();
        if (!bridge.isAvailableFor(object)) {
            return 1;
        }
        try {
            Object object2 = bridge.parallelCountMethod.invoke(object, new Object[0]);
            if (object2 instanceof Number) {
                Number number = (Number)object2;
                return Math.clamp((long)number.intValue(), (int)1, (int)1024);
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return 1;
    }

    public static double getEnergyFactor(Object object, int n) {
        int n2 = Math.max(1, n);
        Bridge bridge = MIParallelHatchCompat.getBridge();
        if (!bridge.isAvailableFor(object)) {
            return n2;
        }
        double d = bridge.getExtraEnergyMultiplier();
        double d2 = 1.0 + ((double)n2 - 1.0) * d;
        if (!Double.isFinite(d2) || d2 < 0.0) {
            return n2;
        }
        return Math.max((double)n2, d2);
    }

    public static long scaleEnergy(long l, int n, double d) {
        if (l <= 0L) {
            return 0L;
        }
        long l2 = MIParallelHatchCompat.saturatedMultiply(l, Math.max(1, n));
        if (!Double.isFinite(d) || d <= 0.0) {
            return l2;
        }
        double d2 = (double)l * d;
        long l3 = !Double.isFinite(d2) || d2 >= 9.223372036854776E18 ? Long.MAX_VALUE : Math.max(0L, Math.round(d2));
        return Math.max(l2, l3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Bridge getBridge() {
        Bridge bridge = MIParallelHatchCompat.bridge;
        if (bridge != null) {
            return bridge;
        }
        Class<MIParallelHatchCompat> clazz = MIParallelHatchCompat.class;
        synchronized (MIParallelHatchCompat.class) {
            bridge = MIParallelHatchCompat.bridge;
            if (bridge == null) {
                MIParallelHatchCompat.bridge = bridge = MIParallelHatchCompat.createBridge();
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return bridge;
        }
    }

    private static Bridge createBridge() {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return DISABLED;
        }
        try {
            ClassLoader classLoader = MIParallelHatchCompat.class.getClassLoader();
            Class<?> clazz = Class.forName("icu.kudikan.miparallelhatch.api.machine.IMultiblockMachineParallelData", false, classLoader);
            Method method = clazz.getMethod("miParallelHatch$getParallelCount", new Class[0]);
            Object object = null;
            Method method2 = null;
            try {
                Class<?> clazz2 = Class.forName("icu.kudikan.miparallelhatch.MiParallelHatchConfig", false, classLoader);
                Field field = clazz2.getField("INSTANCE");
                Object object2 = field.get(null);
                Field field2 = clazz2.getField("parallelExtraEnergyMultiplier");
                object = field2.get(object2);
                method2 = object.getClass().getMethod("getAsDouble", new Class[0]);
            }
            catch (LinkageError | ReflectiveOperationException | RuntimeException throwable) {
                // empty catch block
            }
            return new Bridge(clazz, method, object, method2);
        }
        catch (LinkageError | ReflectiveOperationException | RuntimeException throwable) {
            return DISABLED;
        }
    }

    private static long saturatedMultiply(long l, int n) {
        if (l <= 0L || n <= 0) {
            return 0L;
        }
        if (l > Long.MAX_VALUE / (long)n) {
            return Long.MAX_VALUE;
        }
        return l * (long)n;
    }

    private record Bridge(Class<?> parallelDataClass, Method parallelCountMethod, Object multiplierValue, Method getAsDoubleMethod) {
        private boolean isAvailableFor(Object object) {
            return this.parallelDataClass != null && this.parallelCountMethod != null && this.parallelDataClass.isInstance(object);
        }

        private double getExtraEnergyMultiplier() {
            if (this.multiplierValue == null || this.getAsDoubleMethod == null) {
                return 1.0;
            }
            try {
                Object object = this.getAsDoubleMethod.invoke(this.multiplierValue, new Object[0]);
                if (object instanceof Number) {
                    Number number = (Number)object;
                    double d = number.doubleValue();
                    return Double.isFinite(d) && d >= 0.0 ? d : 1.0;
                }
            }
            catch (ReflectiveOperationException | RuntimeException exception) {
                // empty catch block
            }
            return 1.0;
        }
    }
}

