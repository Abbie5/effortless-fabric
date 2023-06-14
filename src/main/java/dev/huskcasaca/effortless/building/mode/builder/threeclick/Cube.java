package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.builder.ThreeClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Line;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Cube extends ThreeClickBuilder {

    public static Stream<BlockPos> collectFloorBlocksUsingCubeFill(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        switch (context.cubeFilling()) {
            case CUBE_SKELETON -> Floor.addHollowFloorBlocks(list, x1, x2, y1, z1, z2);
            default -> Floor.addFloorBlocks(list, x1, x2, y1, z1, z2);
        }

        return list.stream();
    }

    public static Stream<BlockPos> collectCubeBlocks(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        switch (context.cubeFilling()) {
            case CUBE_FULL -> addCubeBlocks(list, x1, x2, y1, y2, z1, z2);
            case CUBE_HOLLOW -> addHollowCubeBlocks(list, x1, x2, y1, y2, z1, z2);
            case CUBE_SKELETON -> addSkeletonCubeBlocks(list, x1, x2, y1, y2, z1, z2);
        }

        return list.stream();
    }

    public static void addCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                    list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Wall.addXWallBlocks(list, x1, y1, y2, z1, z2);
        Wall.addXWallBlocks(list, x2, y1, y2, z1, z2);

        Wall.addZWallBlocks(list, x1, x2, y1, y2, z1);
        Wall.addZWallBlocks(list, x1, x2, y1, y2, z2);

        Floor.addFloorBlocks(list, x1, x2, y1, z1, z2);
        Floor.addFloorBlocks(list, x1, x2, y2, z1, z2);
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

    @Override
    protected BlockHitResult traceFirstHit(Player player, BuildContext context) {
        return Single.traceSingle(player, context);
    }

    @Override
    protected BlockHitResult traceSecondHit(Player player, BuildContext context) {
        return Floor.traceFloor(player, context);
    }

    @Override
    protected BlockHitResult traceThirdHit(Player player, BuildContext context) {
        return traceLineY(player, context);
    }


    @Override
    protected Stream<BlockPos> collectStartBlocks(BuildContext context) {
        return Single.collectSingleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectInterBlocks(BuildContext context) {
        return collectFloorBlocksUsingCubeFill(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
        return collectCubeBlocks(context);
    }
}