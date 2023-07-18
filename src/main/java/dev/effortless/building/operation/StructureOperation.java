package dev.effortless.building.operation;

import dev.effortless.building.Context;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class StructureOperation implements Operation<StructureResult> {

    private static void sortOnDistanceToPlayer(List<BlockOperation> blockPosStates, Player player) {
        blockPosStates.sort((lpl, rpl) -> {
            // -1 for less than, 1 for greater than, 0 for equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public abstract Level level();

    public abstract Player player();

    public abstract Context context();


}
