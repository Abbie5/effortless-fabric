package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.network.protocol.ClientEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;

public class ClientEffortlessPacketHandler implements ClientEffortlessPacketListener {

    private final Minecraft minecraft;
    private final PacketListener packetHandler;

    public ClientEffortlessPacketHandler(Minecraft minecraft, PacketListener packetHandler) {
        this.minecraft = minecraft;
        this.packetHandler = packetHandler;
    }

    @Override
    public void handle(ClientboundPlayerSettingsPacket packet) {
    }

    @Override
    public void onDisconnect(Component component) {
        packetHandler.onDisconnect(component);
    }

    @Override
    public Connection getConnection() {
        return packetHandler.getConnection();
    }

    @Override
    public boolean shouldPropagateHandlingExceptions() {
        return packetHandler.shouldPropagateHandlingExceptions();
    }

}
