package dev.huskcasaca.effortless.buildmode.twoclick;

import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.TwoClickBuildable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Line extends TwoClickBuildable {

    public static BlockPos findLine(Player player, BlockPos firstPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var eye = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        var criteriaList = new ArrayList<NearestLineCriteria>(3);

        criteriaList.add(new NearestLineCriteria(firstPos.getCenter(), eye, look, Direction.Axis.X));
        criteriaList.add(new NearestLineCriteria(firstPos.getCenter(), eye, look, Direction.Axis.Y));
        criteriaList.add(new NearestLineCriteria(firstPos.getCenter(), eye, look, Direction.Axis.Z));

        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isInRange(player, reach, skipRaytrace));

        if (criteriaList.isEmpty()) return null;

        var selected = criteriaList.get(0);

        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                NearestLineCriteria criteria = criteriaList.get(i);
                if (criteria.distanceToLineSqr() < 2.0 && selected.distanceToLineSqr() < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distanceToEyeSqr() < selected.distanceToEyeSqr())
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distanceToLineSqr() < selected.distanceToLineSqr())
                        selected = criteria;
                }
            }
        }

        return selected.traceLine();
    }

    public static List<BlockPos> getLineBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        List<BlockPos> list = new ArrayList<>();

        if (x1 != x2) {
            addXLineBlocks(list, x1, x2, y1, z1);
        } else if (y1 != y2) {
            addYLineBlocks(list, y1, y2, x1, z1);
        } else {
            addZLineBlocks(list, z1, z2, x1, y1);
        }

        return list;
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
    protected BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace) {
        return findLine(player, firstPos, skipRaytrace);
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return getLineBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    static class NearestLineCriteria extends AxisCriteria {

        public NearestLineCriteria(Vec3 center, Vec3 eye, Vec3 look, Direction.Axis axis) {
            super(center, eye, look, axis);
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
