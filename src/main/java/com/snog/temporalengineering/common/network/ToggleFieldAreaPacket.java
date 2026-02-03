package com.snog.temporalengineering.common.network;

import com.snog.temporalengineering.common.blockentity.TemporalFieldGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleFieldAreaPacket
{
    private final BlockPos pos;
    private final boolean show;

    public ToggleFieldAreaPacket(BlockPos pos, boolean show)
    {
        this.pos = pos;
        this.show = show;
    }

    public static void encode(ToggleFieldAreaPacket msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.show);
    }

    public static ToggleFieldAreaPacket decode(FriendlyByteBuf buf)
    {
        return new ToggleFieldAreaPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(ToggleFieldAreaPacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            var sender = ctx.get().getSender();
            if (sender == null)
            {
                return;
            }

            var level = sender.level;
            if (!level.isLoaded(msg.pos))
            {
                return;
            }

            var be = level.getBlockEntity(msg.pos);
            if (be instanceof TemporalFieldGeneratorBlockEntity gen)
            {
                double distSqr = sender.distanceToSqr(
                    msg.pos.getX() + 0.5,
                    msg.pos.getY() + 0.5,
                    msg.pos.getZ() + 0.5
                );

                if (distSqr <= 64.0)
                {
                    gen.setShowArea(msg.show);
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}