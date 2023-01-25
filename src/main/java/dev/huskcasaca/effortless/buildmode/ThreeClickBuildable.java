package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.building.ReachHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class ThreeClickBuildable extends MultipleClickBuildable {

    protected Dictionary<UUID, BlockHitResult> secondHitResultTable = new Hashtable<>();

    private static BlockPos findLineByAxis(Player player, BlockPos secondPos, boolean skipRaytrace, Axis axis) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var eye = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        var criteriaList = new ArrayList<AxisLineCriteria>(3);

        for (var a : Axis.values()) {
            if (a == axis) continue;
            criteriaList.add(new AxisLineCriteria(secondPos.getCenter(), eye, look, a));
        }

        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isInRange(player, reach, skipRaytrace));

        if (criteriaList.isEmpty()) return null;
        var selected = criteriaList.get(0);

        if (criteriaList.size() > 1) {
            for (int i = 1; i < criteriaList.size(); i++) {
                var criteria = criteriaList.get(i);
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
        return selected.traceLine(axis);
    }

    public static BlockPos findLineY(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findLineByAxis(player, secondPos, skipRaytrace, Axis.Y);
    }

    public static BlockPos findLineX(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findLineByAxis(player, secondPos, skipRaytrace, Axis.X);
    }

    public static BlockPos findLineZ(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findLineByAxis(player, secondPos, skipRaytrace, Axis.Z);
    }

    private static BlockPos findPlaneByAxis(Player player, BlockPos secondPos, boolean skipRaytrace, Axis axis) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var eye = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        var criteriaList = new ArrayList<AxisLineCriteria>(3);

        criteriaList.add(new AxisLineCriteria(secondPos.getCenter(), eye, look, axis));

        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isInRange(player, reach, skipRaytrace));

        if (criteriaList.isEmpty()) return null;
        var selected = criteriaList.get(0);

        if (criteriaList.size() > 1) {
            for (int i = 1; i < criteriaList.size(); i++) {
                var criteria = criteriaList.get(i);
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
        return selected.tracePlane();
    }

    public static BlockPos findPlaneY(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findPlaneByAxis(player, secondPos, skipRaytrace, Axis.Y);
    }

    public static BlockPos findPlaneX(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findPlaneByAxis(player, secondPos, skipRaytrace, Axis.X);
    }

    public static BlockPos findPlaneZ(Player player, BlockPos secondPos, boolean skipRaytrace) {
        return findPlaneByAxis(player, secondPos, skipRaytrace, Axis.Z);
    }

    protected BlockHitResult getSecondHitResult(Player player) {
        return secondHitResultTable.get(player.getUUID());
    }

    protected BlockHitResult putSecondResult(Player player, BlockHitResult hitResult) {
        return secondHitResultTable.put(player.getUUID(), hitResult);
    }

    @Override
    public void initialize(Player player) {
        super.initialize(player);
        putSecondResult(player, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false));
    }

    @Override
    public List<BlockPos> trace(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        var blockPos = hitResult.getBlockPos();

        List<BlockPos> list = new ArrayList<>();

        int useCount = getUseCount(player);
        useCount++;
        putUseCount(player, useCount);

        if (useCount == 1) {
            //If clicking in air, reset and try again
            if (blockPos == null) {
                putUseCount(player, 0);
                return list;
            }

            //First click, remember starting position
            putFirstResult(player, hitResult);
            //Keep list empty, dont place any blocks yet
        } else if (useCount == 2) {
            //Second click, find other floor point
            var firstPos = getFirstHitResult(player).getBlockPos();
            var secondPos = findSecondPos(player, firstPos, true);

            if (secondPos == null) {
                putUseCount(player, 1);
                return list;
            }

            putSecondResult(player, hitResult.withPosition(secondPos));

        } else {
            //Third click, place diagonal wall with height
            list = preview(player, hitResult, skipRaytrace);
            putUseCount(player, 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> preview(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        int useCount = getUseCount(player);

        if (useCount == 0) {
            if (hitResult != null)
                list.add(hitResult.getBlockPos());
        } else if (useCount == 1) {
            var firstPos = getFirstHitResult(player).getBlockPos();

            var secondPos = findSecondPos(player, firstPos, true);
            if (secondPos == null) return list;

            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            //Add diagonal line from first to second
            list.addAll(getIntermediateBlocks(player, x1, y1, z1, x2, y2, z2));

        } else {
            var firstPos = getFirstHitResult(player).getBlockPos();
            var secondPos = getSecondHitResult(player).getBlockPos();

            var thirdPos = findThirdPos(player, firstPos, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX(), x3 = thirdPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY(), y3 = thirdPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ(), z3 = thirdPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            if (x3 - x1 >= axisLimit) x3 = x1 + axisLimit - 1;
            if (x1 - x3 >= axisLimit) x3 = x1 - axisLimit + 1;
            if (y3 - y1 >= axisLimit) y3 = y1 + axisLimit - 1;
            if (y1 - y3 >= axisLimit) y3 = y1 - axisLimit + 1;
            if (z3 - z1 >= axisLimit) z3 = z1 + axisLimit - 1;
            if (z1 - z3 >= axisLimit) z3 = z1 - axisLimit + 1;

            //Add diagonal line from first to third
            list.addAll(getFinalBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3));
        }

        return list;
    }

    //Finds the place of the second block pos
    protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

    //Finds the place of the third block pos
    protected abstract BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace);

    //After first and second pos are known, we want to visualize the blocks in a way (like floor for cube)
    protected abstract List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);

    //After first, second and third pos are known, we want all the blocks
    public abstract List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3);

    static class Criteria_Dep {
        Vec3 planeBound;
        Vec3 lineBound;
        double distToLineSq;
        double distToPlayerSq;

        Criteria_Dep(Vec3 planeBound, BlockPos secondPos, Vec3 start) {
            this.planeBound = planeBound;
            this.lineBound = toLongestLine(this.planeBound, secondPos);
            this.distToLineSq = this.lineBound.subtract(this.planeBound).lengthSqr();
            this.distToPlayerSq = this.planeBound.subtract(start).lengthSqr();
        }

        //Make it from a plane into a line, on y axis only
        private Vec3 toLongestLine(Vec3 boundVec, BlockPos secondPos) {
            BlockPos bound = new BlockPos(boundVec);
            return new Vec3(secondPos.getX(), bound.getY(), secondPos.getZ());
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace) {

            return BuildModeHandler.isCriteriaValid(start, look, reach, player, skipRaytrace, lineBound, planeBound, distToPlayerSq);
        }

        public double distanceToEyeSqr() {
            return distToPlayerSq;
        }

        public double distanceToLineSqr() {
            return distToLineSq;
        }
    }

    public static class AxisLineCriteria extends AxisCriteria {

        public AxisLineCriteria(Vec3 center, Vec3 eye, Vec3 look, Axis axis) {
            super(center, eye, look, axis);
        }

        @Override
        public Vec3 lineVec() {
            return lineVec(axis);
        }

        public Vec3 lineVec(Axis axis) {
            var pos = new BlockPos(center);
            var bound = new BlockPos(planeVec());
            return switch (axis) {
                case X -> new Vec3(bound.getX(), pos.getY(), pos.getZ());
                case Y -> new Vec3(pos.getX(), bound.getY(), pos.getZ());
                case Z -> new Vec3(pos.getX(), pos.getY(), bound.getZ());
            };
        }

        public BlockPos traceLine(Axis axis) {
            return new BlockPos(lineVec(axis));
        }

    }
}
