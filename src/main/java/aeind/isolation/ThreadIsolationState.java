/*
 * Decompiled with CFR 0.152.
 */
package aeind.isolation;

import aeind.isolation.ThreadIsolationRoom;

public final class ThreadIsolationState {
    private static final ThreadLocal<ThreadIsolationRoom> ACTIVE = new ThreadLocal();

    private ThreadIsolationState() {
    }

    public static void set(ThreadIsolationRoom threadIsolationRoom) {
        ACTIVE.set(threadIsolationRoom);
    }

    public static ThreadIsolationRoom get() {
        return ACTIVE.get();
    }

    public static void clear() {
        ACTIVE.remove();
    }
}

