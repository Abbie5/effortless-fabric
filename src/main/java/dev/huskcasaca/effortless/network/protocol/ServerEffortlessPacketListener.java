package dev.huskcasaca.effortless.network.protocol;


import dev.huskcasaca.effortless.core.network.ServerPacketListener;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;

public interface ServerEffortlessPacketListener extends ServerPacketListener {

    void handle(ServerboundPlayerActionPacket packet);

    void handle(ServerboundPlayerBuildPacket packet);

    void handle(ServerboundPlayerSettingsPacket packet);

}
