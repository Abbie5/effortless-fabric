package dev.huskcasaca.effortless.network.protocol;

import dev.huskcasaca.effortless.core.network.ClientPacketListener;
import dev.huskcasaca.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;

public interface ClientEffortlessPacketListener extends ClientPacketListener {

    void handle(ClientboundPlayerSettingsPacket packet);

}
