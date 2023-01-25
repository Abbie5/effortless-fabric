package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Sends a message to the server indicating that a player wants to break a block
 */
public record ServerboundPlayerBreakBlockPacket(
        BlockHitResult blockHitResult
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerBreakBlockPacket() {
        this(BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO));
    }

    public ServerboundPlayerBreakBlockPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBlockHitResult());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockHitResult(blockHitResult);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
