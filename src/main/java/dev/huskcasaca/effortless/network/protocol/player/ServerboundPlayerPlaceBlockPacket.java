package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Sends a message to the server indicating that a player wants to place a block.
 * Received clientside: server has placed blocks and its letting the client know.
 */
public record ServerboundPlayerPlaceBlockPacket(
        BlockHitResult blockHitResult
) implements Packet<ServerEffortlessPacketListener> {


    public ServerboundPlayerPlaceBlockPacket() {
        this(BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO));
    }

    public ServerboundPlayerPlaceBlockPacket(BlockHitResult result, boolean placeStartPos) {
        this(result);
    }

    public ServerboundPlayerPlaceBlockPacket(FriendlyByteBuf friendlyByteBuf) {
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
