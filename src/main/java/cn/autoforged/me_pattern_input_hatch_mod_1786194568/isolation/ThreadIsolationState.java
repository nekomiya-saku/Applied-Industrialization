/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;

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

