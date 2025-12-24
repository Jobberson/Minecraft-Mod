package com.snog.temporalengineering;

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

    // Client constructor
    public TemporalProcessorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv,
                (TemporalProcessorBlockEntity) playerInv.player.level.getBlockEntity(buf.readBlockPos()));
    }

    // Server constructor
    public TemporalProcessorMenu(int id, Inventory playerInv, TemporalProcessorBlockEntity be) {
        super(ModMenuTypes.TEMPORAL_PROCESSOR_MENU.get(), id);
        this.blockEntity = be;

        // === DATA SYNC (THIS IS THE IMPORTANT PART) ===
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getHeat();
                    case 1 -> blockEntity.getFluidAmount();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> blockEntity.setHeat(value);
                    case 1 -> blockEntity.setFluidAmount(value);
                }
            }

            @Override
            public int getCount() {
                return 2; // heat + water
            }
        };

        this.addDataSlots(this.data);

        // === OUTPUT SLOT (Exotic Matter) ===
        IItemHandler items = blockEntity.getItemHandlerView();
        this.addSlot(new SlotItemHandler(items, 0, 134, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // output only
            }
        });

        // === PLAYER INVENTORY ===
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // === Inventory layout helpers ===
    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col,
                    8 + col * 18,
                    142));
        }
    }

    public int getHeat() {
        return data.get(0);
    }

    public int getWater() {
        return data.get(1);
    }
}
