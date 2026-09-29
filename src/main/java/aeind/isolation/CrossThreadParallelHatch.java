package aeind.isolation;

public interface CrossThreadParallelHatch {
   default int aeind$maxParallelPerThread() {
      return 1;
   }
}
