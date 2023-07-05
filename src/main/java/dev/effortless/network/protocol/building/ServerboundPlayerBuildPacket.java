package dev.effortless.network.protocol.building;

import dev.effortless.building.Context;
import dev.effortless.network.protocol.ServerEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundPlayerBuildPacket(
        Context context
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerBuildPacket(FriendlyByteBuf friendlyByteBuf) {
        this(Context.decodeBuf(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        Context.write(friendlyByteBuf, context);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
