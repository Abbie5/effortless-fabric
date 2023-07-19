package dev.effortless.network.protocol.settings;

import dev.effortless.building.settings.DimensionSettings;
import dev.effortless.network.protocol.ClientEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerSettingsPacket(
        DimensionSettings dimensionSettings
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerSettingsPacket(FriendlyByteBuf friendlyByteBuf) {
        this(
                new DimensionSettings(
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
        friendlyByteBuf.writeInt(dimensionSettings.maxReachDistance());
        friendlyByteBuf.writeInt(dimensionSettings.maxBlockPlacePerAxis());
        friendlyByteBuf.writeInt(dimensionSettings.maxBlockPlaceAtOnce());
        friendlyByteBuf.writeBoolean(dimensionSettings.canBreakFar());
        friendlyByteBuf.writeBoolean(dimensionSettings.enableUndoRedo());
        friendlyByteBuf.writeInt(dimensionSettings.undoStackSize());
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
