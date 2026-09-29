package aeind.isolation;

import java.util.List;

public interface ThreadIsolationAccess {
   boolean aeind$isolationEnabled();

   List<ThreadIsolationRoom> aeind$isolationRooms();

   boolean aeind$crossThreadEnabled();

   int aeind$maxParallelPerThread();

   boolean aeind$overdriveBlocked();
}
