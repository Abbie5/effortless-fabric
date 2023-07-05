package dev.effortless.building.mode.builder.threeclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.builder.TripleClickBuilder;
import dev.effortless.building.mode.builder.twoclick.Line;
import dev.effortless.building.mode.builder.twoclick.Square;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.effortless.building.mode.builder.oneclick.Single.collectSingleBlocks;
import static dev.effortless.building.mode.builder.oneclick.Single.traceSingle;

public class Cube extends TripleClickBuilder {

    public static void addFullCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {
            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {
                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                    list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Square.addFullSquareBlocksX(list, x1, y1, y2, z1, z2);
        Square.addFullSquareBlocksX(list, x2, y1, y2, z1, z2);

        Square.addFullSquareBlocksZ(list, x1, x2, y1, y2, z1);
        Square.addFullSquareBlocksZ(list, x1, x2, y1, y2, z2);

        Square.addFullSquareBlocksY(list, x1, x2, y1, z1, z2);
        Square.addFullSquareBlocksY(list, x1, x2, y2, z1, z2);
    }

    public static void addSkeletonCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Line.addXLineBlocks(list, x1, x2, y1, z1);
        Line.addXLineBlocks(list, x1, x2, y1, z2);
        Line.addXLineBlocks(list, x1, x2, y2, z1);
        Line.addXLineBlocks(list, x1, x2, y2, z2);

        Line.addYLineBlocks(list, y1, y2, x1, z1);
        Line.addYLineBlocks(list, y1, y2, x1, z2);
        Line.addYLineBlocks(list, y1, y2, x2, z1);
        Line.addYLineBlocks(list, y1, y2, x2, z2);

        Line.addZLineBlocks(list, z1, z2, x1, y1);
        Line.addZLineBlocks(list, z1, z2, x1, y2);
        Line.addZLineBlocks(list, z1, z2, x2, y1);
        Line.addZLineBlocks(list, z1, z2, x2, y2);
    }


    public static Stream<BlockPos> collectCubePlaneBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        switch (context.cubeFilling()) {
            case CUBE_SKELETON -> Square.addHollowSquareBlocks(list, x1, x2, y1, y2, z1, z2);
            case CUBE_FULL -> Square.addFullSquareBlocks(list, x1, x2, y1, y2, z1, z2);
            case CUBE_HOLLOW -> Square.addFullSquareBlocks(list, x1, x2, y1, y2, z1, z2);
        }

        return list.stream();
    }

    public static Stream<BlockPos> collectCubeBlocks(Context context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x3 = context.thirdPos().getX();
        var y3 = context.thirdPos().getY();
        var z3 = context.thirdPos().getZ();

        switch (context.cubeFilling()) {
            case CUBE_FULL -> addFullCubeBlocks(list, x1, x3, y1, y3, z1, z3);
            case CUBE_HOLLOW -> addHollowCubeBlocks(list, x1, x3, y1, y3, z1, z3);
            case CUBE_SKELETON -> addSkeletonCubeBlocks(list, x1, x3, y1, y3, z1, z3);
        }

        return list.stream();
    }
    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, Context context) {
        return Square.traceSquare(player, context);
    }

    @Override
    protected BlockHitResult traceThirdHit(Player player, Context context) {
        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        if (y1 == y2) {
            if (x1 == x2 && z1 == z2) {
                return Line.traceLine(player, context);
            }
            if (x1 == x2) {
                return tracePlaneZ(player, context);
            }
            if (z1 == z2) {
                return tracePlaneX(player, context);
            }
            return traceLineY(player, context);
        } else {
            if (x1 == x2 && z1 == z2) {
                return tracePlaneY(player, context);
            }
            if (x1 == x2) {
                return traceLineX(player, context);
            }
            if (z1 == z2) {
                return traceLineZ(player, context);
            }
        }
        return null;
    }


    @Override
    protected Stream<BlockPos> collectStartBlocks(Context context) {
        return collectSingleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectInterBlocks(Context context) {
        return collectCubePlaneBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectCubeBlocks(context);
    }

}