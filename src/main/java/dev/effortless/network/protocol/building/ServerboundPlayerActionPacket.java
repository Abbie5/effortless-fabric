package dev.effortless.network.protocol.building;

import dev.effortless.building.mode.SingleAction;
import dev.effortless.network.protocol.ServerEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundPlayerActionPacket(
        SingleAction action
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerActionPacket(FriendlyByteBuf friendlyByteBuf) {
        this(SingleAction.values()[friendlyByteBuf.readInt()]);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(action.ordinal());
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
