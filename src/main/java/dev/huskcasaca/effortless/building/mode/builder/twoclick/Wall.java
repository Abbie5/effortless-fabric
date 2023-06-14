package dev.huskcasaca.effortless.building.mode.builder.twoclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.builder.TwoClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Wall extends TwoClickBuilder {

    public static BlockHitResult traceWall(Player player, BuildContext context) {
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

    public static Stream<BlockPos> collectWallBlocks(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (x1 == x2) {
            if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL)
                addXWallBlocks(list, x1, y1, y2, z1, z2);
            else
                addXHollowWallBlocks(list, x1, y1, y2, z1, z2);
        } else {
            if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL)
                addZWallBlocks(list, x1, x2, y1, y2, z1);
            else
                addZHollowWallBlocks(list, x1, x2, y1, y2, z1);
        }

        return list.stream();
    }

    public static void addXWallBlocks(List<BlockPos> list, int x, int y1, int y2, int z1, int z2) {

        for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {

            for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
                list.add(new BlockPos(x, y, z));
            }
        }
    }

    public static void addZWallBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z) {

        for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {

            for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
                list.add(new BlockPos(x, y, z));
            }
        }
    }

    public static void addXHollowWallBlocks(List<BlockPos> list, int x, int y1, int y2, int z1, int z2) {
        Line.addZLineBlocks(list, z1, z2, x, y1);
        Line.addZLineBlocks(list, z1, z2, x, y2);
        Line.addYLineBlocks(list, y1, y2, x, z1);
        Line.addYLineBlocks(list, y1, y2, x, z2);
    }

    public static void addZHollowWallBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z) {
        Line.addXLineBlocks(list, x1, x2, y1, z);
        Line.addXLineBlocks(list, x1, x2, y2, z);
        Line.addYLineBlocks(list, y1, y2, x1, z);
        Line.addYLineBlocks(list, y1, y2, x2, z);
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, BuildContext context) {
        return Single.traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, BuildContext context) {
        return traceWall(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
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
