package com.snog.temporalengineering;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemporalProcessorBlockEntity extends BlockEntity implements MenuProvider {

    private int heat = 0;
    private int maxHeat = 100;

    // Fluids
    private final FluidTank tank = new FluidTank(1000);
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    // Items: one output slot
    private final ItemStackHandler itemHandler = new ItemStackHandler(1);
    private final LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> itemHandler);

    // Work accumulation model
    private float workProgress = 0f;

    // multiplier fields
    private float workMultiplier = 1.0f;
    private int multiplierTicksRemaining = 0;

    // DATA SYNC (heat + water)
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return heat;
                case 1: return tank.getFluidAmount();
                default: return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: heat = value; break;
                case 1:
                    if (value <= 0) {
                        tank.setFluid(FluidStack.EMPTY);
                    } else {
                        tank.setFluid(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, value));
                    }
                    break;
            }
        }

        @Override
        public int getCount() { return 2; }
    };

    public TemporalProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPORAL_PROCESSOR.get(), pos, state);
    }

    /** Server-side tick */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TemporalProcessorBlockEntity be) {
        if (level.isClientSide) return;

        // Read config values live
        int heatThreshold = TemporalConfig.EM_THRESHOLD.get();
        float baseWork = TemporalConfig.BASE_WORK_PER_TICK.get().floatValue();
        float workThreshold = (float) TemporalConfig.PROCESSOR_WORK_THRESHOLD.get();
        int coolRate = TemporalConfig.COOL_RATE.get();
        int tankDrain = TemporalConfig.TANK_DRAIN_PER_SECOND.get();

        // Cooling / heating
        if (be.tank.getFluidAmount() > 0 &&
            be.tank.getFluid().getFluid() == net.minecraft.world.level.material.Fluids.WATER) {

            be.heat = Math.max(0, be.heat - coolRate);

            if (level.getGameTime() % 20 == 0) {
                be.tank.drain(tankDrain, IFluidHandler.FluidAction.EXECUTE);
            }

        } else {
            be.heat = Math.min(be.maxHeat, be.heat + TemporalConfig.HEAT_RATE.get());
        }

        // Work accumulation model (only when hot enough)
        if (be.heat >= heatThreshold) {
            float workThisTick = baseWork * be.workMultiplier;
            be.workProgress += workThisTick;

            if (be.workProgress >= workThreshold) {
                int cycles = (int)(be.workProgress / workThreshold);
                for (int i = 0; i < cycles; i++) {
                    boolean produced = be.tryGenerateExoticMatter();
                    if (!produced) {
                        // output full -> stop applying further work this tick
                        break;
                    }
                    be.workProgress -= workThreshold;
                }
                if (be.workProgress < 0f) be.workProgress = 0f;
            }
        }

        // Decay multiplier timer
        if (be.multiplierTicksRemaining > 0) {
            be.multiplierTicksRemaining--;
            if (be.multiplierTicksRemaining == 0) {
                be.workMultiplier = 1.0f;
            }
        }

        if (level.getGameTime() % 20 == 0) {
            be.setChanged();
        }
    }

    /* ========== MenuProvider ========== */

    @Override
    public Component getDisplayName() {
        return new net.minecraft.network.chat.TranslatableComponent(
            "container.temporalengineering.temporal_processor"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new TemporalProcessorMenu(id, playerInv, this);
    }

    public ContainerData getData() { return data; }

    /* ========== EM Generation ========== */

    private boolean tryGenerateExoticMatter() {
        ItemStack em = new ItemStack(ModItems.VOLATILE_EXOTIC_MATTER.get());

        ItemStack remainder = itemHandler.insertItem(0, em.copy(), true);
        if (remainder.isEmpty()) {
            itemHandler.insertItem(0, em, false);
            setChanged();
            return true;
        } else {
            return false;
        }
    }

    /* ========== NBT ========== */

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.contains("Heat") ? tag.getInt("Heat") : 0;
        maxHeat = tag.contains("MaxHeat") ? tag.getInt("MaxHeat") : 100;

        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }

        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }

        if (tag.contains("WorkMultiplier")) {
            workMultiplier = tag.getFloat("WorkMultiplier");
        }
        if (tag.contains("MultiplierTicks")) {
            multiplierTicksRemaining = tag.getInt("MultiplierTicks");
        }
        if (tag.contains("WorkProgress")) {
            workProgress = tag.getFloat("WorkProgress");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Heat", heat);
        tag.putInt("MaxHeat", maxHeat);

        CompoundTag tankTag = new CompoundTag();
        tank.writeToNBT(tankTag);
        tag.put("Tank", tankTag);

        tag.put("Inventory", itemHandler.serializeNBT());

        tag.putFloat("WorkMultiplier", workMultiplier);
        tag.putInt("MultiplierTicks", multiplierTicksRemaining);
        tag.putFloat("WorkProgress", workProgress);
    }

    /* ========== Capabilities ========== */

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(
            @Nonnull net.minecraftforge.common.capabilities.Capability<T> cap,
            @Nullable Direction side
    ) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandler.cast();
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        fluidHandler.invalidate();
        itemHandlerCap.invalidate();
    }

    /* ========== Helpers ========== */

    public int getHeat() { return heat; }
    public int getMaxHeat() { return maxHeat; }
    public int getFluidAmount() { return tank.getFluidAmount(); }
    public FluidStack getFluid() { return tank.getFluid(); }
    public IItemHandler getItemHandlerView() { return itemHandler; }

    public void setHeat(int heat) { this.heat = heat; }
    public void setFluidAmount(int amount) {
        if (amount <= 0) {
            tank.setFluid(FluidStack.EMPTY);
        } else {
            tank.setFluid(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, amount));
        }
    }

    public int fillFromPlayer(FluidStack stack, IFluidHandler.FluidAction action) {
        if (stack == null || stack.getFluid() != net.minecraft.world.level.material.Fluids.WATER) {
            return 0;
        }
        return tank.fill(stack, action);
    }

    public void applyTimeMultiplier(float multiplier, int durationTicks) {
        this.workMultiplier = multiplier;
        this.multiplierTicksRemaining = Math.max(this.multiplierTicksRemaining, durationTicks);
        setChanged();
    }
}
