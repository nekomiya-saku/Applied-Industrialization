/*
 * Decompiled with CFR 0.152.
 */
package aeind.isolation;

import aeind.isolation.ThreadIsolationRoom;
import java.util.List;

public interface ThreadIsolationAccess {
    public boolean aeind$isolationEnabled();

    public List<ThreadIsolationRoom> aeind$isolationRooms();

    public boolean aeind$crossThreadEnabled();

    public int aeind$maxParallelPerThread();

    public boolean aeind$overdriveBlocked();
}

