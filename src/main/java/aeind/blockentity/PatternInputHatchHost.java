/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.stacks.AEKey
 */
package aeind.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;

public interface PatternInputHatchHost {
    public IManagedGridNode getMainNode();

    public boolean canAcceptOrder();

    public long insertBuffer(AEKey var1, long var2, Actionable var4);

    public boolean returnUnusedBufferToNetwork();

    public boolean returnAllBufferToNetwork();

    public void returnToNetwork(AEKey var1, long var2);

    public void saveChanges();
}

