package dev.effortless.network;

import dev.effortless.Effortless;
import dev.effortless.core.network.FabricNetworkChannel;
import dev.effortless.core.network.NetworkChannel;
import dev.effortless.network.protocol.ClientEffortlessPacketListener;
import dev.effortless.network.protocol.ServerEffortlessPacketListener;
import dev.effortless.network.protocol.building.ClientboundPlayerBuildPacket;
import dev.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;
import dev.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

public class Packets {

    private static boolean isClientSide() {
        return FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.CLIENT;
    }

    private static NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> createChannel() {
        var clientPacketHandlerCreator = (NetworkChannel.ClientHandlerCreator<ClientEffortlessPacketListener>) null;
        if (isClientSide()) {
            clientPacketHandlerCreator = ClientEffortlessPacketHandler::new;
        }

        return new FabricNetworkChannel<>(
                Effortless.asResource("default_channel"),
                ServerEffortlessPacketHandler::new,
                clientPacketHandlerCreator
        );
    }

    private static final NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel = createChannel();

    public static NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel() {
        return channel;
    }

    public static void register() {
        channel.registerServer();
        if (isClientSide()) {
            channel.registerClient();
        }

        channel.registerServerBoundPacket(ServerboundPlayerActionPacket.class, ServerboundPlayerActionPacket::new);
        channel.registerServerBoundPacket(ServerboundPlayerSettingsPacket.class, ServerboundPlayerSettingsPacket::new);
        channel.registerServerBoundPacket(ServerboundPlayerBuildPacket.class, ServerboundPlayerBuildPacket::new);
        channel.registerClientBoundPacket(ClientboundPlayerBuildPacket.class, ClientboundPlayerBuildPacket::new);
        channel.registerClientBoundPacket(ClientboundPlayerSettingsPacket.class, ClientboundPlayerSettingsPacket::new);
    }

}
