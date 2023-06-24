package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.core.network.FabricNetworkChannel;
import dev.huskcasaca.effortless.core.network.NetworkChannel;
import dev.huskcasaca.effortless.network.protocol.ClientEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.ServerEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.resources.ResourceLocation;

public class Packets {

    private static final NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel = createChannel();

    private static NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> createChannel() {
        var clientPacketHandlerCreator = (NetworkChannel.ClientHandlerCreator<ClientEffortlessPacketListener>) null;
        if (FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.CLIENT) {
            clientPacketHandlerCreator = ClientEffortlessPacketHandler::new;
        }

        return new FabricNetworkChannel<>(
                new ResourceLocation(Effortless.MOD_ID, "default_channel"),
                ServerEffortlessPacketHandler::new,
                clientPacketHandlerCreator
        );
    }

    public static NetworkChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel() {
        return channel;
    }

    public static void registerServer() {
        channel.registerServer();
        channel.registerServerBoundPacket(ServerboundPlayerActionPacket.class, ServerboundPlayerActionPacket::new);
        channel.registerServerBoundPacket(ServerboundPlayerSettingsPacket.class, ServerboundPlayerSettingsPacket::new);
        channel.registerServerBoundPacket(ServerboundPlayerBuildPacket.class, ServerboundPlayerBuildPacket::new);
    }

    public static void registerClient() {
        channel.registerClient();
        channel.registerClientBoundPacket(ClientboundPlayerSettingsPacket.class, ClientboundPlayerSettingsPacket::new);
    }

}
