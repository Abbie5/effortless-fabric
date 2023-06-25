package dev.huskcasaca.effortless.building.mode.builder;

import dev.huskcasaca.effortless.building.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.stream.Stream;

public abstract class ThreeClickBuilder extends MultipleClickBuilder {

    private static BlockHitResult traceLineByAxis(Player player, Context context, Axis axis) {
        var center = context.secondPos().getCenter();
        var reach = context.maxReachDistance();
        var skipRaytrace = context.skipRaytrace();

        return Stream.of(
                        new AxisLineCriteria(Direction.Axis.X, player, center, reach, skipRaytrace),
                        new AxisLineCriteria(Direction.Axis.Y, player, center, reach, skipRaytrace),
                        new AxisLineCriteria(Direction.Axis.Z, player, center, reach, skipRaytrace)
                )
                .filter((criteria) -> criteria.axis != axis)
                .filter((criteria) -> criteria.isInRange())
                .min(Comparator.comparing(axisLineCriteria -> axisLineCriteria.distanceToLineSqr()))
                .map((criteria) -> criteria.traceLine(axis))
                .orElse(null);
    }

    public static BlockHitResult traceLineY(Player player, Context context) {
        return traceLineByAxis(player, context, Axis.Y);
    }

    public static BlockHitResult traceLineX(Player player, Context context) {
        return traceLineByAxis(player, context, Axis.X);
    }

    public static BlockHitResult traceLineZ(Player player, Context context) {
        return traceLineByAxis(player, context, Axis.Z);
    }

    private static BlockHitResult tracePlaneByAxis(Player player, Context context, Axis axis) {
        var center = context.secondPos().getCenter();
        var reach = context.maxReachDistance();
        var skipRaytrace = context.skipRaytrace();

        return Stream.of(
                        new AxisLineCriteria(Direction.Axis.X, player, center, reach, skipRaytrace),
                        new AxisLineCriteria(Direction.Axis.Y, player, center, reach, skipRaytrace),
                        new AxisLineCriteria(Direction.Axis.Z, player, center, reach, skipRaytrace)
                )
                .filter((criteria) -> criteria.axis == axis)
                .filter((criteria) -> criteria.isInRange())
                .min(Comparator.comparing(axisLineCriteria -> axisLineCriteria.distanceToLineSqr()))
                .map((criteria) -> criteria.tracePlane())
                .orElse(null);
    }

    public static BlockHitResult tracePlaneY(Player player, Context context) {
        return tracePlaneByAxis(player, context, Axis.Y);
    }

    public static BlockHitResult tracePlaneX(Player player, Context context) {
        return tracePlaneByAxis(player, context, Axis.X);
    }

    public static BlockHitResult tracePlaneZ(Player player, Context context) {
        return tracePlaneByAxis(player, context, Axis.Z);
    }

    protected abstract BlockHitResult traceFirstHit(Player player, Context context);

    protected abstract BlockHitResult traceSecondHit(Player player, Context context);

    protected abstract BlockHitResult traceThirdHit(Player player, Context context);

    protected abstract Stream<BlockPos> collectStartBlocks(Context context);

    protected abstract Stream<BlockPos> collectInterBlocks(Context context);

    protected abstract Stream<BlockPos> collectFinalBlocks(Context context);

    @Override
    public BlockHitResult trace(Player player, Context context) {
        return switch (context.clicks()) {
            case 0 -> traceFirstHit(player, context);
            case 1 -> traceSecondHit(player, context);
            case 2 -> traceThirdHit(player, context);
            default -> BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO); // FIXME: 7/3/23
        };
    }

    @Override
    public Stream<BlockPos> collect(Context context) {
        return switch (context.clicks()) {
            case 1 -> collectStartBlocks(context);
            case 2 -> collectInterBlocks(context);
            case 3 -> collectFinalBlocks(context);
            default -> Stream.empty();
        };
    }

    @Override
    public int totalClicks(Context context) {
        return 3;
    }

    public static class AxisLineCriteria extends AxisCriteria {

        public AxisLineCriteria(Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            super(axis, entity, center, reach, skipRaytrace);
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

        public BlockHitResult traceLine(Axis axis) {
            var found = new BlockPos(lineVec(axis));
            return convert(found);
        }

    }
}
