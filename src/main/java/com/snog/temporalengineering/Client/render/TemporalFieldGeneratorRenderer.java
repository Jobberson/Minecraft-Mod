package com.snog.temporalengineering.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.snog.temporalengineering.common.blockentity.TemporalFieldGeneratorBlockEntity;
import com.snog.temporalengineering.common.config.TemporalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class TemporalFieldGeneratorRenderer implements BlockEntityRenderer<TemporalFieldGeneratorBlockEntity>
{
    public TemporalFieldGeneratorRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(TemporalFieldGeneratorBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        if (be == null || be.getLevel() == null)
        {
            return;
        }

        // Only render when the player enabled it via the generator UI toggle.
        if (!be.getShowArea())
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
        {
            return;
        }

        // Optional: distance cull for performance (very cheap and helpful)
        Vec3 cam = mc.player.position();
        double dx = (be.getBlockPos().getX() + 0.5) - cam.x;
        double dy = (be.getBlockPos().getY() + 0.5) - cam.y;
        double dz = (be.getBlockPos().getZ() + 0.5) - cam.z;
        double distSqr = dx * dx + dy * dy + dz * dz;

        // If the player is very far, don't bother rendering the outline.
        // Tune this as you like.
        double maxDist = 96.0;
        if (distSqr > maxDist * maxDist)
        {
            return;
        }

        int radius = TemporalConfig.FIELD_RADIUS.get();
        int r = Math.max(0, radius);

        if (r == 0)
        {
            return;
        }

        // Color: subtle blue, faint alpha
        float cr = 0.25f;
        float cg = 0.65f;
        float cb = 1.00f;
        float ca = 0.18f;

        // RenderType.lines() is depth-tested, so the outline will be occluded by blocks.
        var consumer = buffer.getBuffer(RenderType.lines());

        poseStack.pushPose();

        // The BE renderer origin is the block's corner. We want the cube centered on the block center,
        // because your gameplay loop is symmetric around the generator's block position.
        poseStack.translate(0.5, 0.5, 0.5);

        // Gameplay applies to all offsets dx/dy/dz in [-r..r]. That affects a cube of blocks.
        // To draw a boundary around the *outer faces* of those blocks, use min=-r and max= r+1.
        float min = -r;
        float max = r + 1;

        // Slight expansion reduces z-fighting when lines overlap block faces.
        float eps = 0.002f;
        min -= eps;
        max += eps;

        drawWireCube(poseStack, consumer, min, min, min, max, max, max, cr, cg, cb, ca);

        poseStack.popPose();
    }

    private void drawWireCube(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                              float minX, float minY, float minZ,
                              float maxX, float maxY, float maxZ,
                              float r, float g, float b, float a)
    {
        // Bottom rectangle (Y = minY)
        line(poseStack, consumer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(poseStack, consumer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(poseStack, consumer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(poseStack, consumer, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top rectangle (Y = maxY)
        line(poseStack, consumer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(poseStack, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(poseStack, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(poseStack, consumer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Vertical edges
        line(poseStack, consumer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(poseStack, consumer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(poseStack, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(poseStack, consumer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private void line(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float r, float g, float b, float a)
    {
        var pose = poseStack.last().pose();
        var normal = poseStack.last().normal();

        // Note: Normal isn't super meaningful for lines, but Forge expects it.
        consumer.vertex(pose, x0, y0, z0).color(r, g, b, a).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose, x1, y1, z1).color(r, g, b, a).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
    }
}