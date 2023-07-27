package dev.effortless.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.function.Function;

public abstract class NetworkChannel<S extends PacketListener, C extends PacketListener> {

    protected final ResourceLocation channelName;
    protected final ServerHandlerCreator<S> serverPacketHandlerCreator;
    protected final ClientHandlerCreator<C> clientPacketHandlerCreator;

    protected final PacketSet<S> serverPacketSet = new PacketSet<>();
    protected final PacketSet<C> clientPacketSet = new PacketSet<>();

    public NetworkChannel(ResourceLocation channelName, ServerHandlerCreator<S> serverPacketHandlerCreator, ClientHandlerCreator<C> clientPacketHandlerCreator) {
        this.channelName = channelName;
        this.serverPacketHandlerCreator = serverPacketHandlerCreator;
        this.clientPacketHandlerCreator = clientPacketHandlerCreator;
    }

    public abstract void registerServer();

    public abstract void registerClient();

    @SuppressWarnings("unchecked")
    public Packet<S> createC2SPacket(int i, FriendlyByteBuf friendlyByteBuf) {
        return (Packet<S>) serverPacketSet.createPacket(i, friendlyByteBuf);
    }

    @SuppressWarnings("unchecked")
    public Packet<C> createS2CPacket(int i, FriendlyByteBuf friendlyByteBuf) {
        return (Packet<C>) clientPacketSet.createPacket(i, friendlyByteBuf);
    }

    public FriendlyByteBuf createClientBoundBuf(Packet<C> packet) {
        return clientPacketSet.createBuf(packet);
    }

    public FriendlyByteBuf createServerBoundBuf(Packet<S> packet) {
        return serverPacketSet.createBuf(packet);
    }

    public <T extends Packet<S>> void registerServerBoundPacket(Class<T> clazz, Function<FriendlyByteBuf, T> deserializer) {
        try {
            serverPacketSet.addPacket(clazz, deserializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends Packet<C>> void registerClientBoundPacket(Class<T> clazz, Function<FriendlyByteBuf, T> deserializer) {
        try {
            clientPacketSet.addPacket(clazz, deserializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void sendToClient(Packet<C> packet, ServerPlayer player);

    public abstract void sendToClients(Packet<C> packet, Iterable<ServerPlayer> players);

    public abstract void sendToServer(Packet<S> packet);

    @FunctionalInterface
    public interface ServerHandlerCreator<T extends PacketListener> {
        T create(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener);
    }

    @FunctionalInterface
    public interface ClientHandlerCreator<T extends PacketListener> {
        T create(Minecraft client, PacketListener listener);
    }

    private static class PacketSet<P extends PacketListener> extends ConnectionProtocol.PacketSet<P> {

        public FriendlyByteBuf createBuf(Packet<P> packet) {
            var id = getId(packet.getClass());
            if (id == -1) {
                throw new IllegalArgumentException("Packet " + packet.getClass() + " is not registered");
            }
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(id);
            packet.write(buf);
            return buf;
        }

    }

}
