package com.snog.temporalengineering.common.blockentity;

import com.snog.temporalengineering.api.ITemporalAffectable;
import com.snog.temporalengineering.common.registry.ModItems;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.config.TemporalConfig;
import com.snog.temporalengineering.common.menu.TemporalProcessorMenu;
import com.snog.temporalengineering.common.temporal.TemporalTime;
import com.snog.temporalengineering.api.ITemporalStatusHud;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Temporal Processor Block Entity — implements ITemporalAffectable.
 */
public class TemporalProcessorBlockEntity extends BlockEntity implements MenuProvider, ITemporalAffectable {

    // Heat
    private int heat    = 0;
    private int maxHeat = 100;

    // Fluids (water tank)
    private final FluidTank tank = new FluidTank(1000);
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    // Item inventory: one output slot
    private final ItemStackHandler itemHandler = new ItemStackHandler(1);
    private final LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> itemHandler);

    // Work system
    public float workProgress           = 0f;
    public float workMultiplier         = 1.0f;
    public int multiplierTicksRemaining = 0;

    // hud 
    private float hudRequestedMultiplier = 1.0f;
    private float hudEffectiveMultiplier = 1.0f;
    private int hudStatusBits            = 0;

    // Status bit constants
    private static final int STATUS_FIELD_ACTIVE    = 0x1;
    private static final int STATUS_CAPPED          = 0x2;
    private static final int STATUS_ADAPTER_APPLIED = 0x4

    // DATA SYNC
    
    private final ContainerData data = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            return switch (index)
            {
                case 0 -> heat;
                case 1 -> tank.getFluidAmount();
                case 2 -> Math.round(workProgress * 100.0f); // scaled to int for UI
                case 3 -> Math.round(hudEffectiveMultiplier * 100.0f);
                case 4 -> Math.round(hudRequestedMultiplier * 100.0f);
                case 5 -> hudStatusBits;
                default -> 0;
            };
        }

        
        @Override
        public void set(int index, int value)
        {
            switch (index)
            {
                case 0 -> heat = value;
                case 1 ->
                {
                    if (value <= 0) tank.setFluid(net.minecraftforge.fluids.FluidStack.EMPTY);
                    else tank.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, value));
                }
                case 2 -> workProgress = value / 100.0f;
                case 3 -> hudEffectiveMultiplier = value / 100.0f;
                case 4 -> hudRequestedMultiplier = value / 100.0f;
                case 5 -> hudStatusBits = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };
    
    // ---- Optional HUD interface implementation ----
    @Override
    public void notifyTemporalEffect(float requested, float effective, int durationTicks, String source)
    {
        this.hudRequestedMultiplier = requested;
        this.hudEffectiveMultiplier = effective;

        int bits = 0;
        if ("Field".equalsIgnoreCase(source)) bits |= STATUS_FIELD_ACTIVE;
        if (effective + 1e-6f < requested)    bits |= STATUS_CAPPED;
        if ("Adapter".equalsIgnoreCase(source)) bits |= STATUS_ADAPTER_APPLIED;

        this.hudStatusBits = bits;

        // also keep the gameplay multiplier behavior just like before:
        applyTimeMultiplier(effective, durationTicks); // ensures timer + workMultiplier are set
        setChanged();
    }

    public TemporalProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPORAL_PROCESSOR.get(), pos, state);
    }

    /**
     * Server-side tick. Deterministic accumulation model.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TemporalProcessorBlockEntity be)
    {
        if (level.isClientSide) return;

        // Read config values
        int heatThreshold = TemporalConfig.EM_THRESHOLD.get();
        float baseWork = TemporalConfig.BASE_WORK_PER_TICK.get().floatValue();
        float workThreshold = TemporalConfig.PROCESSOR_WORK_THRESHOLD.get().floatValue();
        int coolRate = TemporalConfig.COOL_RATE.get();
        int tankDrain = TemporalConfig.TANK_DRAIN_PER_SECOND.get();
        int heatRate = TemporalConfig.HEAT_RATE.get();

        // === Cooling / heating ===
        if (be.tank.getFluidAmount() > 0 &&
            be.tank.getFluid().getFluid() == net.minecraft.world.level.material.Fluids.WATER)
        {
            be.heat = Math.max(0, be.heat - coolRate);
            if (level.getGameTime() % 20 == 0)
            {
                be.tank.drain(tankDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        else
        {
            be.heat = Math.min(be.maxHeat, be.heat + heatRate);
        }

        // Determine if output is full (slot 0 is the output)
        ItemStack out = be.itemHandler.getStackInSlot(0);
        boolean outputFull = !out.isEmpty() && out.getCount() >= out.getMaxStackSize();

        // === Work accumulation model with delta clamp ===
        if (be.heat >= heatThreshold)
        {
            if (!outputFull)
            {
                // Centralized delta clamp: never allow massive jumps in a single tick
                float rawDelta = baseWork * be.workMultiplier;
                float clampedDelta = TemporalTime.clampDeltaPerTick(be, rawDelta);
                be.workProgress += clampedDelta;

                boolean allowMulti = TemporalConfig.ALLOW_MULTI_CYCLE_IN_SINGLE_TICK.get();

                if (be.workProgress >= workThreshold)
                {
                    if (allowMulti)
                    {
                        int cycles = (int) (be.workProgress / workThreshold);
                        for (int i = 0; i < cycles; i++)
                        {
                            boolean produced = be.tryGenerateExoticMatter();
                            if (!produced)
                            {
                                be.workProgress = Math.min(be.workProgress, workThreshold - 0.0001f);
                                break;
                            }
                            be.workProgress -= workThreshold;
                        }
                    }
                    else
                    {
                        // Single cycle resolution only
                        boolean produced = be.tryGenerateExoticMatter();
                        if (produced)
                        {
                            be.workProgress -= workThreshold;
                        }
                        else
                        {
                            // Output became full mid-cycle: clamp below threshold
                            be.workProgress = Math.min(be.workProgress, workThreshold - 0.0001f);
                        }
                    }

                    if (be.workProgress < 0f) be.workProgress = 0f;
                }
            }
        }
        else
        {
            // output full: pause accumulation and clamp progress so no instant burst later
            be.workProgress = Math.min(be.workProgress, workThreshold - 0.0001f);
        }

        // === Decay multiplier timer ===
        if (be.multiplierTicksRemaining > 0)
        {
            be.multiplierTicksRemaining--;
            if (be.multiplierTicksRemaining <= 0)
            {
                be.workMultiplier = 1.0f;
            }
        }

        be.setChanged();
    }


    /* ========== MenuProvider ========== */
    @Override
    public Component getDisplayName() {
        return new TextComponent("Temporal Processor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new TemporalProcessorMenu(id, playerInv, this);
    }

    public ContainerData getData() {
        return data;
    }

    /* ========== EM generation ========== */
    private boolean tryGenerateExoticMatter() {
        // Do not produce if slot is full
        ItemStack current = itemHandler.getStackInSlot(0);
        if (!current.isEmpty() && current.getCount() >= current.getMaxStackSize()) {
            return false;
        }

        ItemStack em = new ItemStack(ModItems.VOLATILE_EXOTIC_MATTER.get(), 1);

        // simulate insertion
        ItemStack remainder = itemHandler.insertItem(0, em.copy(), true);
        if (!remainder.isEmpty()) {
            return false;
        }

        // actually insert
        itemHandler.insertItem(0, em, false);
        setChanged();
        return true;
    }

    /* ========== NBT Persistence ========== */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.heat = tag.getInt("Heat");
        if (tag.contains("MaxHeat")) this.maxHeat = tag.getInt("MaxHeat");

        if (tag.contains("Tank")) {
            this.tank.readFromNBT(tag.getCompound("Tank"));
        }

        if (tag.contains("Inventory")) {
            this.itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }

        if (tag.contains("WorkProgress")) this.workProgress = tag.getFloat("WorkProgress");
        if (tag.contains("WorkMultiplier")) this.workMultiplier = tag.getFloat("WorkMultiplier");
        if (tag.contains("MultiplierTicks")) this.multiplierTicksRemaining = tag.getInt("MultiplierTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Heat", this.heat);
        tag.putInt("MaxHeat", this.maxHeat);

        CompoundTag tankTag = new CompoundTag();
        this.tank.writeToNBT(tankTag);
        tag.put("Tank", tankTag);

        tag.put("Inventory", this.itemHandler.serializeNBT());

        tag.putFloat("WorkProgress", this.workProgress);
        tag.putFloat("WorkMultiplier", this.workMultiplier);
        tag.putInt("MultiplierTicks", this.multiplierTicksRemaining);
    }

    /* ========== Capabilities ========== */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
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

    // Programmatic filling helper (accept only water)
    public int fillFromPlayer(FluidStack stack, IFluidHandler.FluidAction action) {
        if (stack == null || stack.getFluid() != net.minecraft.world.level.material.Fluids.WATER) return 0;
        return tank.fill(stack, action);
    }

    /**
     * API method — allow external sources to apply a multiplier to this processor.
     * Implements ITemporalAffectable.
     */
    @Override
    public void applyTimeMultiplier(float multiplier, int durationTicks) {
        this.workMultiplier = Math.max(this.workMultiplier, multiplier);
        this.multiplierTicksRemaining = Math.max(this.multiplierTicksRemaining, durationTicks);
        setChanged();
    }
}
