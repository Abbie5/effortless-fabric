package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.building.EffortlessServerBuilder;
import dev.huskcasaca.effortless.network.protocol.ServerEffortlessPacketListener;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerActionPacket;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.huskcasaca.effortless.network.protocol.settings.ServerboundPlayerSettingsPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ServerEffortlessPacketHandler implements ServerEffortlessPacketListener {

    private final MinecraftServer server;
    private final ServerPlayer player;
    private final ServerGamePacketListenerImpl packetListener;

    public ServerEffortlessPacketHandler(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl packetListener) {
        this.server = server;
        this.player = player;
        this.packetListener = packetListener;
    }

    @Override
    public void handle(ServerboundPlayerActionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, player.getLevel());
    }

    @Override
    public void handle(ServerboundPlayerBuildPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, player.getLevel());
        EffortlessServerBuilder.getInstance().perform(player, packet.buildContext());
    }

    @Override
    public void handle(ServerboundPlayerSettingsPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, player.getLevel());
    }

    @Override
    public void onDisconnect(Component component) {
        packetListener.onDisconnect(component);
    }

    @Override
    public Connection getConnection() {
        return packetListener.getConnection();
    }

    @Override
    public boolean shouldPropagateHandlingExceptions() {
        return packetListener.shouldPropagateHandlingExceptions();
    }
}
