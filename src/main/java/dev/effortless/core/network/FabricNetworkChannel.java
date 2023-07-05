package dev.effortless.core.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

public class FabricNetworkChannel<S extends PacketListener, C extends PacketListener> extends NetworkChannel<S, C> {

    public FabricNetworkChannel(ResourceLocation channelName, ServerHandlerCreator<S> serverPacketHandlerCreator, ClientHandlerCreator<C> clientPacketHandlerCreator) {
        super(channelName, serverPacketHandlerCreator, clientPacketHandlerCreator);
    }

    @Override
    public void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(channelName, (client, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var packet = (Packet<C>) null;
            try {
                packet = createS2CPacket(id, buf);
                Objects.requireNonNull(packet);
            } catch (Exception e) {
                throw new RuntimeException("Could not create S2C packet in channel '" + channelName + "' with id " + id, e);
            }
            try {
                packet.handle(clientPacketHandlerCreator.create(client, handler));
            } catch (RunningOnDifferentThreadException exception) {
            } catch (RejectedExecutionException exception) {
            } catch (ClassCastException exception) {
            }
        });

    }

    public void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(channelName, (server, player, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var packet = (Packet<S>) null;
            try {
                packet = createC2SPacket(id, buf);
                Objects.requireNonNull(packet);
            } catch (Exception e) {
                throw new RuntimeException("Could not create C2S packet in channel '" + channelName + "' with id " + id, e);
            }
            try {
                packet.handle(serverPacketHandlerCreator.create(server, player, handler));
            } catch (RunningOnDifferentThreadException exception) {
            } catch (RejectedExecutionException exception) {
            } catch (ClassCastException exception) {
            }
        });
    }

    @Override
    public void sendToClient(Packet<C> packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, channelName, createClientBoundBuf(packet));
    }

    @Override
    public void sendToClients(Packet<C> packet, Iterable<ServerPlayer> players) {
        Packet<?> vanillaPacket = createS2CVanillaPacket(channelName, packet);
        for (ServerPlayer player : players) {
            ServerPlayNetworking.getSender(player).sendPacket(vanillaPacket);
        }
    }

    @Override
    public void sendToServer(Packet<S> packet) {
        ClientPlayNetworking.send(channelName, createServerBoundBuf(packet));
    }

    private Packet<?> createS2CVanillaPacket(ResourceLocation identifier, Packet<C> packet) {
        var buf = createClientBoundBuf(packet);
        return ClientPlayNetworking.createC2SPacket(identifier, buf);
    }

}
