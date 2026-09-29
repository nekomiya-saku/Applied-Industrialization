package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;

public interface PatternInputHatchHost {
   IManagedGridNode getMainNode();

   boolean canAcceptOrder();

   long insertBuffer(AEKey var1, long var2, Actionable var4);

   boolean returnUnusedBufferToNetwork();

   boolean returnAllBufferToNetwork();

   void returnToNetwork(AEKey var1, long var2);

   void saveChanges();
}
