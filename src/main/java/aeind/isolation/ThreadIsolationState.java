package aeind.isolation;

public final class ThreadIsolationState {
   private static final ThreadLocal<ThreadIsolationRoom> ACTIVE = new ThreadLocal<>();

   private ThreadIsolationState() {
   }

   public static void set(ThreadIsolationRoom var0) {
      ACTIVE.set(var0);
   }

   public static ThreadIsolationRoom get() {
      return ACTIVE.get();
   }

   public static void clear() {
      ACTIVE.remove();
   }
}
