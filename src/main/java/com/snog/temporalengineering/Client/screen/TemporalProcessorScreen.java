package com.snog.temporalengineering.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.snog.temporalengineering.common.menu.TemporalProcessorMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TemporalProcessorScreen extends AbstractContainerScreen<TemporalProcessorMenu> {

    public TemporalProcessorScreen(TemporalProcessorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    
    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY)
    {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        GuiComponent.fill(ms, x, y, x + imageWidth, y + imageHeight, 0xFF202020);

        int heat = menu.getHeat();
        int maxHeat = menu.getMaxHeat();
        int water = menu.getWater();
        int maxWater = menu.getMaxWater();
        int heatWidth = Math.round((heat / (float)Math.max(1, maxHeat)) * 80);
        int waterWidth = Math.round((water / (float)Math.max(1, maxWater)) * 80);
        int barX = x + 20;
        int heatY = y + 20;
        int waterY = y + 40;

        // --- UI pulse background (faint glow)
        int pulseTicks = menu.getData().get(6); // we’ll add a simple getter if you prefer
        if (pulseTicks > 0)
        {
            // fade from goldish to transparent
            int alpha = Math.min(255, pulseTicks * 12);
            int glowColor = (alpha << 24) | 0x00FFD480;
            GuiComponent.fill(ms, x + 16, y + 16, x + imageWidth - 16, y + 60, glowColor);
        }

        GuiComponent.fill(ms, barX - 1, heatY - 1, barX + 81, heatY + 9, 0xFF404040);
        GuiComponent.fill(ms, barX - 1, waterY - 1, barX + 81, waterY + 9, 0xFF404040);
        GuiComponent.fill(ms, barX, heatY, barX + heatWidth, heatY + 8, 0xFFFF4444);
        GuiComponent.fill(ms, barX, waterY, barX + waterWidth, waterY + 8, 0xFF44AAFF);

        font.draw(ms, "Heat: " + heat + " / " + maxHeat, barX, heatY - 10, 0xFFFFFF);
        font.draw(ms, "Water: " + water + " mB", barX, waterY - 10, 0xFFFFFF);

        // Multiplier + status (already added in Sprint B)
        int effX100 = menu.getMultiplierEffectiveX100();
        int reqX100 = menu.getMultiplierRequestedX100();
        float eff = effX100 / 100f;
        float req = reqX100 / 100f;
        font.draw(ms, String.format("Multiplier: %.2fx (req %.2fx)", eff, req), barX, y + 64, 0xCCCCCC);

        String status = "";
        if (menu.isFieldActive())     status = "Temporal Field Active";
        if (menu.isAdapterApplied())  status = status.isEmpty() ? "Adapter Applied" : status + " • Adapter Applied";
        if (menu.isCappedByConfig())  status = status.isEmpty() ? "Capped by Config" : status + " • Capped by Config";
        if (status.isEmpty())         status = "Idle";

        font.draw(ms, status, barX, y + 76, 0xAAAAAA);

        // Energy and instability (Sprint C)
        int energy = menu.getData().get(7);
        int instabilityX100 = menu.getData().get(8);
        float instability = instabilityX100 / 100f;
        font.draw(ms, "Energy: " + energy, barX, y + 96, 0xEEDD88);
        font.draw(ms, String.format("Instability: %.2f", instability), barX, y + 108, 0xDD88EE);
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }
}
