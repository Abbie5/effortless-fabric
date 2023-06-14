package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.builder.ThreeClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.stream.Stream;

public class DiagonalWall extends ThreeClickBuilder {

    //Add diagonal wall from first to second
    public static Stream<BlockPos> collectDiagonalWallBlocks(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();
        var x3 = context.thirdPos().getX();
        var y3 = context.thirdPos().getY();
        var z3 = context.thirdPos().getZ();

        //Get diagonal line blocks
        var diagonalLineBlocks = DiagonalLine.collectDiagonalLineBlocks(context, 1).toList();

        int lowest = Math.min(y1, y3);
        int highest = Math.max(y1, y3);

        //Copy diagonal line on y axis
        for (int y = lowest; y <= highest; y++) {
            for (BlockPos blockPos : diagonalLineBlocks) {
                list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
            }
        }

        return list.stream();
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
        return DiagonalLine.collectDiagonalLineBlocks(context, 1);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
        return collectDiagonalWallBlocks(context);
    }
}