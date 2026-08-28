/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  appeng.menu.MenuOpener
 *  appeng.menu.locator.MenuHostLocator
 *  appeng.menu.locator.MenuLocators
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package cn.autoforged.me_pattern_input_hatch_mod_1786194568.block;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ExtendedPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.ModMenuTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ExtendedPatternInputHatchBlock
extends BaseEntityBlock {
    public static final MapCodec<ExtendedPatternInputHatchBlock> CODEC = ExtendedPatternInputHatchBlock.simpleCodec(ExtendedPatternInputHatchBlock::new);

    public ExtendedPatternInputHatchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public MapCodec<ExtendedPatternInputHatchBlock> codec() {
        return CODEC;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtendedPatternInputHatchBlockEntity(pos, state);
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null && level.getBlockEntity(pos) instanceof ExtendedPatternInputHatchBlockEntity blockEntity) {
            blockEntity.setCustomName(customName);
        }
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (level.getBlockEntity(pos) instanceof ExtendedPatternInputHatchBlockEntity) {
                MenuOpener.open((MenuType)((MenuType)ModMenuTypes.EXTENDED_PATTERN_INPUT_HATCH.get()), (Player)serverPlayer, (MenuHostLocator)MenuLocators.forBlockEntity((BlockEntity)level.getBlockEntity(pos)));
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity blockEntity;
        if (!state.is(newState.getBlock()) && (blockEntity = level.getBlockEntity(pos)) instanceof ExtendedPatternInputHatchBlockEntity) {
            ExtendedPatternInputHatchBlockEntity blockEntity2 = (ExtendedPatternInputHatchBlockEntity)blockEntity;
            blockEntity2.returnAllBufferToNetwork();
            Containers.dropContents((Level)level, (BlockPos)pos, blockEntity2.getDropItems());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        ExtendedPatternInputHatchBlockEntity blockEntity;
        BlockEntity blockEntity2;
        if (!level.isClientSide && (blockEntity2 = level.getBlockEntity(pos)) instanceof ExtendedPatternInputHatchBlockEntity && (blockEntity = (ExtendedPatternInputHatchBlockEntity)blockEntity2).hasStoredMaterials() && !player.isShiftKeyDown()) {
            player.sendSystemMessage((Component)Component.literal((String)"\u4f60\u6b63\u5728\u5c1d\u8bd5\u7834\u574fME\u8f93\u5165\u4ed3\uff0c\u8bf7\u4fdd\u8bc1\u8fde\u63a5ae\u7f51\u7edc\u7684\u540c\u65f6\uff0cae\u7684\u5b58\u50a8\u5bb9\u91cf\u80fd\u591f\u5bb9\u4e0b\u8fd4\u56de\u7684\u539f\u6599\u3002\u5426\u5219\u6e38\u620f\u5f88\u6709\u53ef\u80fd\u56e0\u4e3a\u5927\u91cf\u6389\u843d\u7269\u5d29\u6e83\uff01\u68c0\u67e5\u540e\u82e5\u8981\u7ee7\u7eed\u7834\u574f\uff0c\u8bf7\u5728\u6f5c\u884c\u65f6\u8fdb\u884c\u7834\u574f").withStyle(ChatFormatting.RED));
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return ExtendedPatternInputHatchBlock.createTickerHelper(type, (BlockEntityType)((BlockEntityType)ModBlockEntities.EXTENDED_PATTERN_INPUT_HATCH.get()), ExtendedPatternInputHatchBlockEntity::serverTick);
    }
}
