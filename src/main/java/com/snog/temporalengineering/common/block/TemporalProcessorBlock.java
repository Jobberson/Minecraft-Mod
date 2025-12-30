package com.snog.temporalengineering.common.block;
import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import net.minecraftforge.network.NetworkHooks;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class TemporalProcessorBlock extends BaseEntityBlock  {
    public TemporalProcessorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Hook the server-side ticker for this block entity.
     * We use createTickerHelper so generics are safe.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only provide server tick (TemporalProcessorBlockEntity::serverTick). createTickerHelper returns null on client or type mismatch.
        return createTickerHelper(type, ModBlockEntities.TEMPORAL_PROCESSOR.get(), TemporalProcessorBlockEntity::serverTick);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemporalProcessorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        // First handle water bucket fill like before
        if (!level.isClientSide && held.getItem() == Items.WATER_BUCKET) {
            TemporalProcessorBlockEntity be = (TemporalProcessorBlockEntity) level.getBlockEntity(pos);
            if (be == null) return InteractionResult.PASS;
            int filled = be.fillFromPlayer(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                if (!player.isCreative()) {
                    held.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                }
                be.setChanged();
                return InteractionResult.SUCCESS;
            }
        }

        // Then, open GUI
        if (!level.isClientSide) {
            TemporalProcessorBlockEntity be = (TemporalProcessorBlockEntity) level.getBlockEntity(pos);
            if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) || be == null) return InteractionResult.PASS;
            net.minecraftforge.network.NetworkHooks.openGui(serverPlayer, be, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TemporalProcessorBlockEntity tpbe) {
                IItemHandler handler = tpbe.getItemHandlerView();
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
