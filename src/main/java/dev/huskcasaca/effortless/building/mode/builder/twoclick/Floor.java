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
import java.util.stream.Stream;

public class Floor extends DoubleClickBuilder {

    public static BlockHitResult traceFloor(Player player, Context context) {
        var center = context.firstPos().getCenter();
        var reach = context.maxReachDistance();
        var skipRaytrace = context.skipRaytrace();

        return Stream.of(
                        new FloorCriteria(Direction.Axis.Y, player, center, reach, skipRaytrace)
                )
                .filter(AxisCriteria::isInRange)
                .findFirst()
                .map(AxisCriteria::tracePlane)
                .orElse(null);
    }

    public static Stream<BlockPos> collectFloorBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (y1 == y2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> Square.addFullSquareBlocksY(list, x1, x2, y1, z1, z2);
                case PLANE_HOLLOW -> Square.addHollowSquareBlocksY(list, x1, x2, y1, z1, z2);
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
        return traceFloor(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectFloorBlocks(context);
    }

    public static class FloorCriteria extends Line.NearestLineCriteria {

        public FloorCriteria(Direction.Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            super(axis, entity, center, reach, skipRaytrace);
        }
    }

}
