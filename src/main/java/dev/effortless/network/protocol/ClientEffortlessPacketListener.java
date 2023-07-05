package dev.effortless.network.protocol;

import dev.effortless.core.network.ClientPacketListener;
import dev.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;

public interface ClientEffortlessPacketListener extends ClientPacketListener {

    void handle(ClientboundPlayerSettingsPacket packet);

}
