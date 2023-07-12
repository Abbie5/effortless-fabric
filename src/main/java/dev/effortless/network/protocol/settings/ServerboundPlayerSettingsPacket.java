package dev.effortless.network.protocol.settings;

import dev.effortless.building.reach.ReachConfig;
import dev.effortless.network.protocol.ServerEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundPlayerSettingsPacket(
        ReachConfig reachConfig
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerSettingsPacket(FriendlyByteBuf friendlyByteBuf) {
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
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}