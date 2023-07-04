package dev.huskcasaca.effortless.building.mode.builder.twoclick;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.mode.builder.DoubleClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

public class Wall extends DoubleClickBuilder {

    public static BlockHitResult traceWall(Player player, Context context) {
        var center = context.firstPos().getCenter();
        var reach = context.maxReachDistance();
        var skipRaytrace = context.skipRaytrace();

        return Stream.of(
                        new WallCriteria(Direction.Axis.X, player, center, reach, skipRaytrace),
                        new WallCriteria(Direction.Axis.Z, player, center, reach, skipRaytrace)
                )
                .filter(AxisCriteria::isInRange)
                .min(Comparator.comparing(WallCriteria::distanceAngle))
                .map(AxisCriteria::tracePlane)
                .orElse(null);
    }

    public static Stream<BlockPos> collectWallBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (x1 == x2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> Square.addFullSquareBlocksX(list, x1, y1, y2, z1, z2);
                case PLANE_HOLLOW -> Square.addHollowSquareBlocksX(list, x1, y1, y2, z1, z2);
            }
        } else if (z1 == z2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> Square.addFullSquareBlocksZ(list, x1, x2, y1, y2, z1);
                case PLANE_HOLLOW -> Square.addHollowSquareBlocksZ(list, x1, x2, y1, y2, z1);
            }
        }

        return list.stream();
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return Single.traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, Context context) {
        return traceWall(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectWallBlocks(context);
    }

    public static class WallCriteria extends AxisCriteria {

        public WallCriteria(Direction.Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            super(axis, entity, center, reach, skipRaytrace);
        }

        public double angle() {
            var wall = planeVec().subtract(startVec());
            return wall.x * look.x + wall.z * look.z;
        }

        public double distanceAngle() {
            return distanceToEyeSqr() * angle();
        }

    }

}
