package dev.huskcasaca.effortless.core.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

public class SimpleChannel<S extends PacketListener, C extends PacketListener> {

    private final ResourceLocation channelName;
    private final ServerHandlerCreator<S> serverPacketHandlerCreator;
    private final ClientHandlerCreator<C> clientPacketHandlerCreator;

    private final PacketSet<S> c2sPacketSet = new PacketSet<>();
    private final PacketSet<C> s2cPacketSet = new PacketSet<>();

    public SimpleChannel(ResourceLocation channelName, ServerHandlerCreator<S> serverPacketHandlerCreator, ClientHandlerCreator<C> clientPacketHandlerCreator) {
        this.channelName = channelName;
        this.serverPacketHandlerCreator = serverPacketHandlerCreator;
        this.clientPacketHandlerCreator = clientPacketHandlerCreator;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        ServerPlayNetworking.registerGlobalReceiver(channelName, (server, player, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var packet = (Packet<S>) null;
            try {
                packet = (Packet<S>) c2sPacketSet.createPacket(id, buf);
                Objects.requireNonNull(packet);
            } catch (Exception e) {
                throw new RuntimeException("Could not create C2S packet in channel '" + channelName + "' with id " + id, e);
            }
            try {
                packet.handle(serverPacketHandlerCreator.create(server, player, handler, responseSender));
            } catch (RunningOnDifferentThreadException exception) {
            } catch (RejectedExecutionException exception) {
            } catch (ClassCastException exception) {
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(channelName, (client, handler, buf, responseSender) -> {
            var id = buf.readVarInt();
            var packet = (Packet<C>) null;
            try {
                packet = (Packet<C>) s2cPacketSet.createPacket(id, buf);
                Objects.requireNonNull(packet);
            } catch (Exception e) {
                throw new RuntimeException("Could not create S2C packet in channel '" + channelName + "' with id " + id, e);
            }
            try {
                packet.handle(clientPacketHandlerCreator.create(client, handler, responseSender));
            } catch (RunningOnDifferentThreadException exception) {
            } catch (RejectedExecutionException exception) {
            } catch (ClassCastException exception) {
            }
        });

    }

    public <T extends Packet<S>> void registerC2SPacket(Class<T> clazz, Function<FriendlyByteBuf, T> deserializer) {
        c2sPacketSet.addPacket(clazz, deserializer);
    }

    public <T extends Packet<C>> void registerS2CPacket(Class<T> clazz, Function<FriendlyByteBuf, T> deserializer) {
        s2cPacketSet.addPacket(clazz, deserializer);
    }

    @Environment(EnvType.CLIENT)
    public void sendToServer(Packet<S> packet) {
        ClientPlayNetworking.send(channelName, c2sPacketSet.createBuf(packet));
    }

    public void sendToClient(Packet<C> packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, channelName, s2cPacketSet.createBuf(packet));
    }

    public void sendToClients(Packet<C> packet, Iterable<ServerPlayer> players) {
        Packet<?> vanillaPacket = s2cPacketSet.createVanillaPacket(channelName, packet);

        for (ServerPlayer player : players) {
            ServerPlayNetworking.getSender(player).sendPacket(vanillaPacket);
        }

    }

    @FunctionalInterface
    public interface ServerHandlerCreator<T extends PacketListener> {
        T create(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender sender);
    }

    @FunctionalInterface
    public interface ClientHandlerCreator<T extends PacketListener> {
        T create(Minecraft client, PacketListener listener, PacketSender sender);
    }

//    public void sendToClientsInServer(Packet<C> packet, MinecraftServer server) {
//        this.sendToClients(packet, PlayerLookup.all(server));
//    }
//
//    public void sendToClientsInCurrentServer(Packet<C> packet) {
//        this.sendToClientsInServer(packet, SimpleNetworking.getCurrentServer());
//    }
//
//    public void sendToClientsInWorld(Packet<C> packet, ServerLevel world) {
//        this.sendToClients(packet, PlayerLookup.world(world));
//    }
//
//    public void sendToClientsTracking(Packet<C> packet, ServerLevel world, BlockPos pos) {
//        this.sendToClients(packet, PlayerLookup.tracking(world, pos));
//    }
//
//    public void sendToClientsTracking(Packet<C> packet, ServerLevel world, ChunkPos pos) {
//        this.sendToClients(packet, PlayerLookup.tracking(world, pos));
//    }
//
//    public void sendToClientsTracking(Packet<C> packet, Entity entity) {
//        this.sendToClients(packet, PlayerLookup.tracking(entity));
//    }
//
//    public void sendToClientsTracking(Packet<C> packet, BlockEntity blockEntity) {
//        this.sendToClients(packet, PlayerLookup.tracking(blockEntity));
//    }
//
//    public void sendToClientsTrackingAndSelf(Packet<C> packet, Entity entity) {
//        Collection<ServerPlayer> clients = PlayerLookup.tracking(entity);
//        if (entity instanceof ServerPlayer player) {
//            if (!((Collection)clients).contains(player)) {
//                clients = new ArrayList((Collection)clients);
//                ((Collection)clients).add(player);
//            }
//        }
//
//        this.sendToClients(packet, (Iterable)clients);
//    }
//
//    public void sendToClientsAround(Packet<C> packet, ServerLevel world, Vec3 pos, double radius) {
//        this.sendToClients(packet, PlayerLookup.around(world, pos, radius));
//    }
//
//    public void sendToClientsAround(Packet<C> packet, ServerLevel world, Vec3i pos, double radius) {
//        this.sendToClients(packet, PlayerLookup.around(world, pos, radius));
//    }

    private static class PacketSet<T extends PacketListener> extends ConnectionProtocol.PacketSet<T> {

        public FriendlyByteBuf createBuf(Packet<T> packet) {
            var id = getId(packet.getClass());
            if (id == null) {
                throw new IllegalArgumentException("Packet " + packet.getClass() + " is not registered");
            }

            var buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(id);
            packet.write(buf);
            return buf;
        }

        public Packet<?> createVanillaPacket(ResourceLocation identifier, Packet<T> packet) {
            var buf = this.createBuf(packet);
            return ClientPlayNetworking.createC2SPacket(identifier, buf);
        }
    }

}
