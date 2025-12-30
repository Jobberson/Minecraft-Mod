package com.snog.temporalengineering.common.block;

import com.snog.temporalengineering.common.blockentity.TemporalFieldGeneratorBlockEntity;
import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.registry.ModItems;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class TemporalFieldGeneratorBlock extends BaseEntityBlock {

    public TemporalFieldGeneratorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemporalFieldGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide) return null;
        return createTickerHelper(
                type,
                ModBlockEntities.TEMPORAL_FIELD_GENERATOR.get(),
                TemporalFieldGeneratorBlockEntity::serverTick
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /* ========== PLAYER INTERACTION ========== */

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TemporalFieldGeneratorBlockEntity generator)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // INSERT: right-click with Volatile Exotic Matter
        if (!player.isShiftKeyDown()
                && held.getItem() == ModItems.VOLATILE_EXOTIC_MATTER.get()) {

            ItemStack one = new ItemStack(ModItems.VOLATILE_EXOTIC_MATTER.get(), 1);
            ItemStack remainder =
                    generator.getItemHandlerView().insertItem(0, one, true);

            if (remainder.isEmpty()) {
                generator.getItemHandlerView().insertItem(0, one, false);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                generator.setChanged();
                return InteractionResult.SUCCESS;
            }
        }

        // EXTRACT: sneak + right-click
        if (player.isShiftKeyDown()) {
            ItemStack extracted =
                    generator.getItemHandlerView().extractItem(0, 1, false);

            if (!extracted.isEmpty()) {
                if (!player.addItem(extracted)) {
                    player.drop(extracted, false);
                }
                generator.setChanged();
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TemporalFieldGeneratorBlockEntity gen) {
                IItemHandler handler = gen.getItemHandlerView();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, false);
                    if (!stack.isEmpty()) {
                        ItemEntity ent = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                        level.addFreshEntity(ent);
                    }
                }
            }
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
