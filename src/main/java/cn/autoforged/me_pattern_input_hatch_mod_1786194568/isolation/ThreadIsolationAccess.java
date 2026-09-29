/*
 * Decompiled with CFR 0.152.
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation;

import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;
import java.util.List;

public interface ThreadIsolationAccess {
    public boolean aeind$isolationEnabled();

    public List<ThreadIsolationRoom> aeind$isolationRooms();

    public boolean aeind$crossThreadEnabled();

    public int aeind$maxParallelPerThread();

    public boolean aeind$overdriveBlocked();
}

