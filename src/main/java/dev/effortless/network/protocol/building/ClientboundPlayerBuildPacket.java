package dev.effortless.network.protocol.building;

import dev.effortless.building.Context;
import dev.effortless.network.protocol.ClientEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

import java.util.UUID;

public record ClientboundPlayerBuildPacket(
        UUID playerId,
        Context context
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerBuildPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUUID(), Context.decodeBuf(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(playerId);
        Context.write(friendlyByteBuf, context);
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
