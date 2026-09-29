/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.config.Actionable
 *  appeng.api.config.Settings
 *  appeng.api.config.YesNo
 *  appeng.api.networking.IManagedGridNode
 *  appeng.api.networking.security.IActionHost
 *  appeng.api.networking.security.IActionSource
 *  appeng.api.stacks.AEFluidKey
 *  appeng.api.stacks.AEItemKey
 *  appeng.api.stacks.AEKey
 *  appeng.api.storage.MEStorage
 *  appeng.me.helpers.MachineSource
 *  aztech.modern_industrialization.inventory.ConfigurableItemStack
 *  aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.AdvancedPatternInputHatchBlock;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.AdvancedHatchPatternProviderLogic;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.IsolatedInputProvider;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.isolation.ThreadIsolationRoom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class AdvancedPatternInputHatchBlockEntity
extends MEPatternInputHatchBlockEntity
implements IsolatedInputProvider {
    public static final int PATTERN_SLOTS = 9;
    public static final int SLOTS_PER_ROOM = 9;
    private static final int TOTAL_SLOTS = 81;

    public AdvancedPatternInputHatchBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState, (BlockEntityType)ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get(), 81, true);
    }

    @Override
    public Component getName() {
        return this.getCustomName() != null ? this.getCustomName() : Component.translatable((String)"block.aeind.advanced_pattern_input_hatch");
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get());
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of((ItemLike)((AdvancedPatternInputHatchBlock)((Object)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get())).asItem());
    }

    public AdvancedHatchPatternProviderLogic getAdvancedPatternLogic() {
        return (AdvancedHatchPatternProviderLogic)this.getPatternLogic();
    }

    public long insertBuffer(int n, AEKey aEKey, long l, Actionable actionable) {
        if (n < 0 || n >= 9 || l <= 0L) {
            return 0L;
        }
        int n2 = n * 9;
        int n3 = n2 + 9;
        if (aEKey instanceof AEItemKey) {
            AEItemKey aEItemKey = (AEItemKey)aEKey;
            return AdvancedPatternInputHatchBlockEntity.insertItemsInto(this.getBuffer().getItemStacks().subList(n2, n3), aEItemKey, l, actionable);
        }
        if (aEKey instanceof AEFluidKey) {
            AEFluidKey aEFluidKey = (AEFluidKey)aEKey;
            return AdvancedPatternInputHatchBlockEntity.insertFluidInto(this.getBuffer().getFluidStacks().subList(n2, n3), aEFluidKey, l, actionable != Actionable.MODULATE);
        }
        return 0L;
    }

    public boolean roomHasContent(int n) {
        int n2 = n * 9;
        int n3 = n2 + 9;
        for (ConfigurableItemStack configurableItemStack : this.getBuffer().getItemStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            return true;
        }
        for (ConfigurableFluidStack configurableItemStack : this.getBuffer().getFluidStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            return true;
        }
        return false;
    }

    public boolean canAcceptOrder(int n) {
        if (!this.passesRedstone()) {
            return false;
        }
        boolean bl = this.getAdvancedPatternLogic().getConfigManager().getSetting(Settings.BLOCKING_MODE) == YesNo.YES || this.getBlockingMode() != 0;
        return !bl || !this.roomHasContent(n);
    }

    public void returnRoomToNetwork(int n) {
        if (this.getLevel() == null || this.getLevel().isClientSide || n < 0 || n >= 9) {
            return;
        }
        IManagedGridNode iManagedGridNode = this.getMainNode();
        if (iManagedGridNode.getNode() == null || !iManagedGridNode.getNode().isActive()) {
            return;
        }
        MEStorage mEStorage = iManagedGridNode.getNode().getGrid().getStorageService().getInventory();
        int n2 = n * 9;
        int n3 = n2 + 9;
        for (ConfigurableItemStack configurableItemStack : this.getBuffer().getItemStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            long l = mEStorage.insert((AEKey)AEItemKey.of(((ItemVariant)configurableItemStack.getResource()).toStack(1)), configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)new MachineSource((IActionHost)this));
            configurableItemStack.decrement(l);
        }
        for (ConfigurableFluidStack configurableItemStack : this.getBuffer().getFluidStacks().subList(n2, n3)) {
            if (configurableItemStack.isEmpty()) continue;
            AEFluidKey aEFluidKey = AEFluidKey.of((Fluid)((FluidVariant)configurableItemStack.getResource()).getFluid());
            long l = mEStorage.insert((AEKey)aEFluidKey, configurableItemStack.getAmount(), Actionable.MODULATE, (IActionSource)new MachineSource((IActionHost)this));
            configurableItemStack.decrement(l);
        }
        this.setChanged();
    }

    @Override
    public List<ThreadIsolationRoom> aeind$isolatedInputRooms() {
        ArrayList<ThreadIsolationRoom> arrayList = new ArrayList<ThreadIsolationRoom>(9);
        for (int i = 0; i < 9; ++i) {
            int n = i * 9;
            int n2 = n + 9;
            arrayList.add(new ThreadIsolationRoom("pattern:" + this.getBlockPos().asLong() + ":" + i, this.getBuffer().getItemStacks().subList(n, n2), this.getBuffer().getFluidStacks().subList(n, n2)));
        }
        return arrayList;
    }
}
