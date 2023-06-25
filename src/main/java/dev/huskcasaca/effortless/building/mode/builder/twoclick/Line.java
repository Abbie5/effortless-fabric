package dev.huskcasaca.effortless.building.mode.builder.twoclick;

import dev.huskcasaca.effortless.building.Context;
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

public class Line extends TwoClickBuilder {

    public static BlockHitResult traceLine(Player player, Context context) {
        var center = context.firstPos().getCenter();
        var reach = context.maxReachDistance();
        var skipRaytrace = context.skipRaytrace();

        return Stream.of(
                        new NearestLineCriteria(Direction.Axis.X, player, center, reach, skipRaytrace),
                        new NearestLineCriteria(Direction.Axis.Y, player, center, reach, skipRaytrace),
                        new NearestLineCriteria(Direction.Axis.Z, player, center, reach, skipRaytrace)
                )
                .filter(AxisCriteria::isInRange)
                .min(Comparator.comparing(NearestLineCriteria::distanceToLineSqr))
                .map(AxisCriteria::traceLine)
                .orElse(null);

//        if (criteriaList.size() > 1) {
//            //Select the one that is closest (from wall position to its line counterpart)
//            for (int i = 1; i < criteriaList.size(); i++) {
//                NearestLineCriteria criteria = criteriaList.get(i);
//                if (criteria.distanceToLineSqr() < 2.0 && selected.distanceToLineSqr() < 2.0) {
//                    //Both very close to line, choose closest to player
//                    if (criteria.distanceToEyeSqr() < selected.distanceToEyeSqr())
//                        selected = criteria;
//                } else {
//                    //Pick closest to line
//                    if (criteria.distanceToLineSqr() < selected.distanceToLineSqr())
//                        selected = criteria;
//                }
//            }
//        }

    }

    public static Stream<BlockPos> collectLineBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (x1 != x2) {
            addXLineBlocks(list, x1, x2, y1, z1);
        } else if (y1 != y2) {
            addYLineBlocks(list, y1, y2, x1, z1);
        } else {
            addZLineBlocks(list, z1, z2, x1, y1);
        }

        return list.stream();
    }

    public static void addXLineBlocks(List<BlockPos> list, int x1, int x2, int y, int z) {
        for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {
            list.add(new BlockPos(x, y, z));
        }
    }

    public static void addYLineBlocks(List<BlockPos> list, int y1, int y2, int x, int z) {
        for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
            list.add(new BlockPos(x, y, z));
        }
    }

    public static void addZLineBlocks(List<BlockPos> list, int z1, int z2, int x, int y) {
        for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {
            list.add(new BlockPos(x, y, z));
        }
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return Single.traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, Context context) {
        return traceLine(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectLineBlocks(context);
    }

    static class NearestLineCriteria extends AxisCriteria {


        public NearestLineCriteria(Direction.Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            super(axis, entity, center, reach, skipRaytrace);
        }

        @Override
        public Vec3 lineVec() {
            var pos = new BlockPos(center);
            var bound = new BlockPos(planeVec());
            var firstToSecond = bound.subtract(pos);

            firstToSecond = new BlockPos(Math.abs(firstToSecond.getX()), Math.abs(firstToSecond.getY()), Math.abs(firstToSecond.getZ()));
            int longest = Math.max(firstToSecond.getX(), Math.max(firstToSecond.getY(), firstToSecond.getZ()));
            if (longest == firstToSecond.getX()) {
                return new Vec3(bound.getX(), pos.getY(), pos.getZ());
            }
            if (longest == firstToSecond.getY()) {
                return new Vec3(pos.getX(), bound.getY(), pos.getZ());
            }
            if (longest == firstToSecond.getZ()) {
                return new Vec3(pos.getX(), pos.getY(), bound.getZ());
            }
            return null;
        }
    }
}
