package dev.effortless.network;

import dev.effortless.building.EffortlessBuilder;
import dev.effortless.network.protocol.ClientEffortlessPacketListener;
import dev.effortless.network.protocol.building.ClientboundPlayerBuildPacket;
import dev.effortless.network.protocol.settings.ClientboundPlayerSettingsPacket;
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
    public void handle(ClientboundPlayerBuildPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            var player = level.getPlayerByUUID(packet.playerId());
            if (player != null) {
                EffortlessBuilder.getInstance().onContextReceived(player, packet.context());
            }
        }
    }

    @Override
    public void handle(ClientboundPlayerSettingsPacket packet) {
    }

    @Override
    public void onDisconnect(Component component) {
        packetHandler.onDisconnect(component);
    }

    @Override
    public boolean isAcceptingMessages() {
        return packetHandler.isAcceptingMessages();
    }

    @Override
    public boolean shouldPropagateHandlingExceptions() {
        return packetHandler.shouldPropagateHandlingExceptions();
    }

}
