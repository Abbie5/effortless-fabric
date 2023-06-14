package dev.huskcasaca.effortless.network.protocol.building;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.network.protocol.ServerEffortlessPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundPlayerBuildPacket(
        BuildContext buildContext
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerBuildPacket(FriendlyByteBuf friendlyByteBuf) {
        this(BuildContext.decodeBuf(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        BuildContext.write(friendlyByteBuf, buildContext);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
