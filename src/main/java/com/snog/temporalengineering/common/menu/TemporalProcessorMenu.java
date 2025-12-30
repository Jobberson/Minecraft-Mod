package com.snog.temporalengineering.common.menu;

import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.registry.ModMenuTypes;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TemporalProcessorMenu extends AbstractContainerMenu {

    private final TemporalProcessorBlockEntity blockEntity;
    private final ContainerData data;

    // slot counts and indices
    private static final int CONTAINER_OUTPUT_SLOTS = 1;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLUMNS = 9;
    private static final int PLAYER_INV_SIZE = PLAYER_INV_ROWS * PLAYER_INV_COLUMNS; // 27
    private static final int HOTBAR_SIZE = 9;

    // derived indices
    private static final int INDEX_OUTPUT = 0;
    private static final int INDEX_PLAYER_INV_START = INDEX_OUTPUT + CONTAINER_OUTPUT_SLOTS; // 1
    private static final int INDEX_PLAYER_INV_END = INDEX_PLAYER_INV_START + PLAYER_INV_SIZE; // 28
    private static final int INDEX_HOTBAR_START = INDEX_PLAYER_INV_END; // 28
    private static final int INDEX_HOTBAR_END = INDEX_HOTBAR_START + HOTBAR_SIZE; // 37

    // Client constructor (called by network open)
    public TemporalProcessorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv,
                (TemporalProcessorBlockEntity) Objects.requireNonNull(
                        playerInv.player.level.getBlockEntity(buf.readBlockPos()),
                        "BlockEntity not found for TemporalProcessorMenu"));
    }

    // Server constructor
    public TemporalProcessorMenu(int id, Inventory playerInv, TemporalProcessorBlockEntity be) {
        super(ModMenuTypes.TEMPORAL_PROCESSOR_MENU.get(), id);
        this.blockEntity = be;

        // === container slots ===
        IItemHandler items = blockEntity.getItemHandlerView();

        // Output slot (index 0) - output only
        this.addSlot(new SlotItemHandler(items, 0, 134, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // output only
            }
        });

        // === player inventory (3 rows x 9) ===
        int leftCol = 8;
        int topRow = 84;
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLUMNS; col++) {
                this.addSlot(new Slot(playerInv, col + row * PLAYER_INV_COLUMNS + 9,
                        leftCol + col * 18,
                        topRow + row * 18));
            }
        }

        // === player hotbar ===
        for (int col = 0; col < HOTBAR_SIZE; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        // === data slots ===
        this.data = be.getData();
        this.addDataSlots(this.data);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null || player == null) return false;
            final var level = this.blockEntity.getLevel();
            if (level == null) return false;

            final var pos = this.blockEntity.getBlockPos();
            // 8 blocks radius (64 squared), matches vanilla containers
            boolean inRange = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;

            // Ensure the block is still our processor
            boolean isSameBlock = level.getBlockState(pos).is(com.snog.temporalengineering.common.registry.ModBlocks.TEMPORAL_PROCESSOR.get());

            return inRange && isSameBlock;
    }

    /**
     * Handle shift-click (quickMove). Robustly moves output -> player.
     * Does NOT allow shift-click from player -> output (output-only).
     */
    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // If the clicked slot is the output slot, move into player inventory/hotbar
            if (index == INDEX_OUTPUT) {
                if (!this.moveItemStackTo(slotStack, INDEX_PLAYER_INV_START, INDEX_HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else {
                // Clicked somewhere in the player inventory/hotbar
                // Do NOT allow moving into the output slot (output-only).
                // We could allow inserting into additional input slots if they existed.
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    // Convenience getters so the screen can query values safely
    public int getHeat() {
        // If you have ContainerData 'data' that mirrors BE values, use it:
        try {
            return this.data.get(0);
        } catch (Exception e) {
            // Fallback to block entity if available
            if (this.blockEntity != null) return this.blockEntity.getHeat();
            return 0;
        }
    }

    public int getMaxHeat() {
        if (this.blockEntity != null) return this.blockEntity.getMaxHeat();
        return 100; // fallback consistent with your BE
    }

    public int getWater() {
        try {
            return this.data.get(1);
        } catch (Exception e) {
            if (this.blockEntity != null) return this.blockEntity.getFluidAmount();
            return 0;
        }
    }

    public int getMaxWater() {
        return 1000; // matches your FluidTank capacity; or route to config if you have one
    }
     
    public int getMultiplierEffectiveX100()
    {
        try { return this.data.get(3); } catch (Exception ignored) {}
        return Math.round((this.blockEntity != null ? this.blockEntity.workMultiplier : 1.0f) * 100f);
    }

    public int getMultiplierRequestedX100()
    {
        try { return this.data.get(4); } catch (Exception ignored) {}
        return Math.round(100f); // fallback = 1.0x requested
    }

    public int getStatusBits()
    {
        try { return this.data.get(5); } catch (Exception ignored) {}
        return 0;
    }

    public boolean isFieldActive()
    {
        return (getStatusBits() & 0x1) != 0;
    }

    public boolean isCappedByConfig()
    {
        return (getStatusBits() & 0x2) != 0;
    }

    public boolean isAdapterApplied()
    {
        return (getStatusBits() & 0x4) != 0;
    }
}