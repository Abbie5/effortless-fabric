package dev.huskcasaca.effortless.building.mode.builder.twoclick;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.mode.builder.DoubleClickBuilder;
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

import static dev.huskcasaca.effortless.building.mode.builder.oneclick.Single.traceSingle;

public class Square extends DoubleClickBuilder {

    public static void addFullSquareBlocksX(List<BlockPos> list, int x, int y1, int y2, int z1, int z2) {
        for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {
            for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
                list.add(new BlockPos(x, y, z));
            }
        }
    }

    public static void addFullSquareBlocksY(List<BlockPos> list, int x1, int x2, int y, int z1, int z2) {
        for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {
            for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {
                list.add(new BlockPos(x, y, z));
            }
        }
    }

    public static void addFullSquareBlocksZ(List<BlockPos> list, int x1, int x2, int y1, int y2, int z) {
        for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {
            for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
                list.add(new BlockPos(x, y, z));
            }
        }
    }

    public static void addHollowSquareBlocksX(List<BlockPos> list, int x, int y1, int y2, int z1, int z2) {
        Line.addZLineBlocks(list, z1, z2, x, y1);
        Line.addZLineBlocks(list, z1, z2, x, y2);
        Line.addYLineBlocks(list, y1, y2, x, z1);
        Line.addYLineBlocks(list, y1, y2, x, z2);
    }

    public static void addHollowSquareBlocksY(List<BlockPos> list, int x1, int x2, int y, int z1, int z2) {
        Line.addXLineBlocks(list, x1, x2, y, z1);
        Line.addXLineBlocks(list, x1, x2, y, z2);
        Line.addZLineBlocks(list, z1, z2, x1, y);
        Line.addZLineBlocks(list, z1, z2, x2, y);
    }

    public static void addHollowSquareBlocksZ(List<BlockPos> list, int x1, int x2, int y1, int y2, int z) {
        Line.addXLineBlocks(list, x1, x2, y1, z);
        Line.addXLineBlocks(list, x1, x2, y2, z);
        Line.addYLineBlocks(list, y1, y2, x1, z);
        Line.addYLineBlocks(list, y1, y2, x2, z);
    }

    public static void addFullSquareBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        if (y1 == y2) {
            addFullSquareBlocksY(list, x1, x2, y1, z1, z2);
        } else if (x1 == x2) {
            addFullSquareBlocksX(list, x1, y1, y2, z1, z2);
        } else if (z1 == z2) {
            addFullSquareBlocksZ(list, x1, x2, y1, y2, z1);
        }
    }

    public static void addHollowSquareBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        if (y1 == y2) {
            addHollowSquareBlocksY(list, x1, x2, y1, z1, z2);
        } else if (x1 == x2) {
            addHollowSquareBlocksX(list, x1, y1, y2, z1, z2);
        } else if (z1 == z2) {
            addHollowSquareBlocksZ(list, x1, x2, y1, y2, z1);
        }
    }

    public static Stream<BlockPos> collectSquareBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (y1 == y2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> addFullSquareBlocksY(list, x1, x2, y1, z1, z2);
                case PLANE_HOLLOW -> addHollowSquareBlocksY(list, x1, x2, y1, z1, z2);
            }
        } else if (x1 == x2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> addFullSquareBlocksX(list, x1, y1, y2, z1, z2);
                case PLANE_HOLLOW -> addHollowSquareBlocksX(list, x1, y1, y2, z1, z2);
            }
        } else if (z1 == z2) {
            switch (context.planeFilling()) {
                case PLANE_FULL -> addFullSquareBlocksZ(list, x1, x2, y1, y2, z1);
                case PLANE_HOLLOW -> addHollowSquareBlocksZ(list, x1, x2, y1, y2, z1);
            }
        }

        return list.stream();
    }

    public static BlockHitResult traceSquare(Player player, Context context) {
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
                .map(AxisCriteria::tracePlane)
                .orElse(null);
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, Context context) {
        return traceSquare(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectSquareBlocks(context);
    }

    public static class NearestLineCriteria extends Line.NearestLineCriteria {

        public NearestLineCriteria(Direction.Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            super(axis, entity, center, reach, skipRaytrace);
        }
    }

}
