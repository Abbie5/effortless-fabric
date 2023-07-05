package dev.effortless.network.protocol;


import dev.effortless.core.network.ServerPacketListener;
import dev.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;

public interface ServerEffortlessPacketListener extends ServerPacketListener {

    void handle(ServerboundPlayerActionPacket packet);

    void handle(ServerboundPlayerBuildPacket packet);

    void handle(ServerboundPlayerSettingsPacket packet);

}
