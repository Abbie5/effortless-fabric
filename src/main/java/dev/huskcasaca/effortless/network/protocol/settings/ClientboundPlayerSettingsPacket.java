package dev.huskcasaca.effortless.network.protocol.settings;

import dev.huskcasaca.effortless.building.reach.ReachConfig;
import dev.huskcasaca.effortless.network.protocol.ClientEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerSettingsPacket(
        ReachConfig reachConfig
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerSettingsPacket(FriendlyByteBuf friendlyByteBuf) {
        this(
                new ReachConfig(
                        friendlyByteBuf.readInt(),
                        friendlyByteBuf.readInt(),
                        friendlyByteBuf.readInt(),
                        friendlyByteBuf.readBoolean(),
                        friendlyByteBuf.readBoolean(),
                        friendlyByteBuf.readInt()
                )
        );
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(reachConfig.maxReachDistance());
        friendlyByteBuf.writeInt(reachConfig.maxBlockPlacePerAxis());
        friendlyByteBuf.writeInt(reachConfig.maxBlockPlaceAtOnce());
        friendlyByteBuf.writeBoolean(reachConfig.canBreakFar());
        friendlyByteBuf.writeBoolean(reachConfig.enableUndoRedo());
        friendlyByteBuf.writeInt(reachConfig.undoStackSize());
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
