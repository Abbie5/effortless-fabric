package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.core.network.SimpleChannel;
import dev.huskcasaca.effortless.network.protocol.ClientEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.ServerEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;
import net.minecraft.resources.ResourceLocation;

public class Packets {

    private static final SimpleChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel = new SimpleChannel<>(
            new ResourceLocation(Effortless.MOD_ID, "default_channel"),
            (server, player, listener, sender) -> new ServerEffortlessPacketHandler(server, player, listener),
            (client, listener, sender) -> new ClientEffortlessPacketHandler(client, listener));

    public static SimpleChannel<ServerEffortlessPacketListener, ClientEffortlessPacketListener> channel() {
        return channel;
    }

    public static void register() {
        channel.register();
        channel.registerC2SPacket(ServerboundPlayerActionPacket.class, ServerboundPlayerActionPacket::new);
        channel.registerC2SPacket(ServerboundPlayerSettingsPacket.class, ServerboundPlayerSettingsPacket::new);
        channel.registerC2SPacket(ServerboundPlayerBuildPacket.class, ServerboundPlayerBuildPacket::new);
    }

    public static void registerClient() {
        channel.registerClient();
        channel.registerS2CPacket(ClientboundPlayerSettingsPacket.class, ClientboundPlayerSettingsPacket::new);
    }

}
