package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.builder.ThreeClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.stream.Stream;

public class SlopeFloor extends ThreeClickBuilder {

    //Add slope floor from first to second
    public static Stream<BlockPos> collectSlopeFloorBlocks(Context context) {
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

        int axisLimit = context.maxBlockPlacePerAxis();

        //Determine whether to use x or z axis to slope up
        boolean onXAxis = true;

        int xLength = Math.abs(x2 - x1);
        int zLength = Math.abs(z2 - z1);

        if (context.raisedEdge() == BuildFeature.RaisedEdge.RAISE_SHORT_EDGE) {
            //Slope along short edge
            if (zLength > xLength) onXAxis = false;
        } else {
            //Slope along long edge
            if (zLength <= xLength) onXAxis = false;
        }

        if (onXAxis) {
            //Along X goes up

            //Get diagonal line blocks
            var diagonalLineBlocks = DiagonalLine.collectDiagonalLineBlocks(context.withFirstPos(x1, y1, z1).withSecondPos(x2, y3, z1), 1f).toList();

            //Limit amount of blocks we can place
            int lowest = Math.min(z1, z2);
            int highest = Math.max(z1, z2);

            if (highest - lowest >= axisLimit) highest = lowest + axisLimit - 1;

            //Copy diagonal line on x axis
            for (int z = lowest; z <= highest; z++) {
                for (BlockPos blockPos : diagonalLineBlocks) {
                    list.add(new BlockPos(blockPos.getX(), blockPos.getY(), z));
                }
            }

        } else {
            //Along Z goes up

            //Get diagonal line blocks
            var diagonalLineBlocks = DiagonalLine.collectDiagonalLineBlocks(context.withFirstPos(x1, y1, z1).withSecondPos(x1, y3, z2), 1f).toList();

            //Limit amount of blocks we can place
            int lowest = Math.min(x1, x2);
            int highest = Math.max(x1, x2);

            if (highest - lowest >= axisLimit) highest = lowest + axisLimit - 1;

            //Copy diagonal line on x axis
            for (int x = lowest; x <= highest; x++) {
                for (BlockPos blockPos : diagonalLineBlocks) {
                    list.add(new BlockPos(x, blockPos.getY(), blockPos.getZ()));
                }
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
        return Floor.traceFloor(player, context);
    }

    @Override
    protected BlockHitResult traceThirdHit(Player player, Context context) {
        return traceLineY(player, context);
    }

    @Override
    protected Stream<BlockPos> collectStartBlocks(Context context) {
        return Single.collectSingleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectInterBlocks(Context context) {
        return Floor.collectFloorBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectSlopeFloorBlocks(context);
    }
}