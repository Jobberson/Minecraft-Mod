package com.snog.temporalengineering.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.snog.temporalengineering.TemporalProcessorMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TemporalProcessorScreen extends AbstractContainerScreen<TemporalProcessorMenu> {

    //private static final ResourceLocation BACKGROUND = new ResourceLocation("Demo_background", "textures/gui/container/Demo_background.png"); // placeholder

    public TemporalProcessorScreen(TemporalProcessorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        // Draw a simple background box (no custom texture required)
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        GuiComponent.fill(ms, x, y, x + imageWidth, y + imageHeight, 0xFF202020);

        // Draw heat and water bars as filled rectangles
        int heat = menu.getHeat();
        int maxHeat = 100; // your BE stores maxHeat; hardcoded for now or extend ContainerData if needed
        int heatWidth = Math.round((heat / (float)maxHeat) * 80); // bar width 80
        
        int water = menu.getWater();
        int maxWater = 1000;
        int waterWidth = Math.round((water / (float)maxWater) * 80);

        int barX = x + 20;
        int heatY = y + 20;
        int waterY = y + 40;

        // background of bars
        GuiComponent.fill(ms, barX - 1, heatY - 1, barX + 81, heatY + 9, 0xFF404040);
        GuiComponent.fill(ms, barX - 1, waterY - 1, barX + 81, waterY + 9, 0xFF404040);

        // heat bar (red-ish)
        GuiComponent.fill(ms, barX, heatY, barX + heatWidth, heatY + 8, 0xFFFF4444);

        // water bar (blue-ish)
        GuiComponent.fill(ms, barX, waterY, barX + waterWidth, waterY + 8, 0xFF44AAFF);

        // Draw labels
        font.draw(ms, "Heat: " + heat + " / " + maxHeat, barX, heatY - 10, 0xFFFFFF);
        font.draw(ms, "Water: " + water + " mB", barX, waterY - 10, 0xFFFFFF);
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }
}
