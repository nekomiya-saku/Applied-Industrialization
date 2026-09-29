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
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
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
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.AdvancedPatternInputHatchBlockEntity;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.MEPatternInputHatchBlockEntity;
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

public class AdvancedPatternInputHatchBlock
extends BaseEntityBlock {
    public static final MapCodec<AdvancedPatternInputHatchBlock> CODEC = AdvancedPatternInputHatchBlock.simpleCodec(AdvancedPatternInputHatchBlock::new);

    public AdvancedPatternInputHatchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public MapCodec<AdvancedPatternInputHatchBlock> codec() {
        return CODEC;
    }

    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AdvancedPatternInputHatchBlockEntity(blockPos, blockState);
    }

    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        Component component = (Component)itemStack.get(DataComponents.CUSTOM_NAME);
        if (component != null && (blockEntity = level.getBlockEntity(blockPos)) instanceof AdvancedPatternInputHatchBlockEntity) {
            AdvancedPatternInputHatchBlockEntity advancedPatternInputHatchBlockEntity = (AdvancedPatternInputHatchBlockEntity)blockEntity;
            advancedPatternInputHatchBlockEntity.setCustomName(component);
        }
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof AdvancedPatternInputHatchBlockEntity) {
                AdvancedPatternInputHatchBlockEntity advancedPatternInputHatchBlockEntity = (AdvancedPatternInputHatchBlockEntity)blockEntity;
                MenuOpener.open((MenuType)((MenuType)ModMenuTypes.ADVANCED_PATTERN_INPUT_HATCH.get()), (Player)serverPlayer, (MenuHostLocator)MenuLocators.forBlockEntity((BlockEntity)advancedPatternInputHatchBlockEntity));
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        BlockEntity blockEntity;
        if (!blockState.is(blockState2.getBlock()) && (blockEntity = level.getBlockEntity(blockPos)) instanceof AdvancedPatternInputHatchBlockEntity) {
            AdvancedPatternInputHatchBlockEntity advancedPatternInputHatchBlockEntity = (AdvancedPatternInputHatchBlockEntity)blockEntity;
            advancedPatternInputHatchBlockEntity.returnAllBufferToNetwork();
            Containers.dropContents((Level)level, (BlockPos)blockPos, advancedPatternInputHatchBlockEntity.getDropItems());
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    public boolean onDestroyedByPlayer(BlockState blockState, Level level, BlockPos blockPos, Player player, boolean bl, FluidState fluidState) {
        AdvancedPatternInputHatchBlockEntity advancedPatternInputHatchBlockEntity;
        BlockEntity blockEntity;
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(blockPos)) instanceof AdvancedPatternInputHatchBlockEntity && (advancedPatternInputHatchBlockEntity = (AdvancedPatternInputHatchBlockEntity)blockEntity).hasStoredMaterials() && !player.isShiftKeyDown()) {
            player.sendSystemMessage((Component)Component.literal((String)"\u4f60\u6b63\u5728\u5c1d\u8bd5\u7834\u574f\u9ad8\u7ea7\u6837\u677f\u8f93\u5165\u4ed3\uff0c\u8bf7\u5148\u4fdd\u8bc1 AE \u7f51\u7edc\u6709\u8db3\u591f\u5bb9\u91cf\u63a5\u6536\u7f13\u5b58\u6750\u6599\uff1b\u786e\u8ba4\u540e\u6f5c\u884c\u7834\u574f\u3002").withStyle(ChatFormatting.RED));
            return false;
        }
        return super.onDestroyedByPlayer(blockState, level, blockPos, player, bl, fluidState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return AdvancedPatternInputHatchBlock.createTickerHelper(blockEntityType, (BlockEntityType)((BlockEntityType)ModBlockEntities.ADVANCED_PATTERN_INPUT_HATCH.get()), MEPatternInputHatchBlockEntity::serverTick);
    }
}

