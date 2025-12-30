package com.snog.temporalengineering.common.blockentity;

import com.snog.temporalengineering.common.config.TemporalConfig;
import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import com.snog.temporalengineering.common.blockentity.TemporalFieldGeneratorBlockEntity;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.registry.ModItems;
import com.snog.temporalengineering.api.TemporalAdapterRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemporalFieldGeneratorBlockEntity extends BlockEntity {

    // inventory: one slot for Volatile Exotic Matter ONLY
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == ModItems.VOLATILE_EXOTIC_MATTER.get();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerCap =
            LazyOptional.of(() -> itemHandler);

    public TemporalFieldGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPORAL_FIELD_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  TemporalFieldGeneratorBlockEntity be) {
        if (level.isClientSide) return;

        int consumeInterval = TemporalConfig.GENERATOR_CONSUME_INTERVAL_TICKS.get();
        if (consumeInterval <= 0) return;
        if (level.getGameTime() % consumeInterval != 0) return;

        ItemStack stack = be.itemHandler.getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() == ModItems.VOLATILE_EXOTIC_MATTER.get()) {
            ItemStack extracted = be.itemHandler.extractItem(0, 1, false);
            if (!extracted.isEmpty()) {
                int radius = TemporalConfig.FIELD_RADIUS.get();
                float speed = TemporalConfig.GENERATOR_SPEED_MULTIPLIER.get().floatValue();
                int duration = TemporalConfig.GENERATOR_EFFECT_DURATION_TICKS.get();
                applySpeedToNearby(level, pos, radius, speed, duration);
                be.setChanged();
            }
        }
    }

    private static void applySpeedToNearby(Level level, BlockPos center, int radius, float speed, int duration){
        int r = Math.max(0, radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos check = center.offset(dx, dy, dz);
                    if (!level.isLoaded(check)) continue;

                    BlockEntity be = level.getBlockEntity(check);
                    if (be == null) continue;

                    // Centralized application via registry (covers native + adapters)
                    // Explicit source = "Field" for HUD clarity
                    TemporalAdapterRegistry.tryApplyWithSource(be, speed, duration, "Field");
                }
            }
        }
    }

    /* ========== NBT persistence ========== */

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    /* ========== capabilities ========== */

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap,
                                             @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        itemHandlerCap.invalidate();
    }

    public IItemHandler getItemHandlerView() {
        return itemHandler;
    }
}
