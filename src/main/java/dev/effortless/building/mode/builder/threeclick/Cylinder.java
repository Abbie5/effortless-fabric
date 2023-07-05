package dev.effortless.building.mode.builder.threeclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.builder.TripleClickBuilder;
import dev.effortless.building.mode.builder.oneclick.Single;
import dev.effortless.building.mode.builder.twoclick.Circle;
import dev.effortless.building.mode.builder.twoclick.Square;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Cylinder extends TripleClickBuilder {

    public static Stream<BlockPos> collectCylinderBlocks(Context context) {
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

        if (y1 == y2) {
            for (int y = y1; y1 < y3 ? y <= y3 : y >= y3; y += y1 < y3 ? 1 : -1) {
                int y0 = y;
                list.addAll(Circle.collectCircleBlocks(context).map(blockPos -> new BlockPos(blockPos.getX(), y0, blockPos.getZ())).toList());
            }
        } else if (x1 == x2) {
                        for (int x = x1; x1 < x3 ? x <= x3 : x >= x3; x += x1 < x3 ? 1 : -1) {
                int x0 = x;
                list.addAll(Circle.collectCircleBlocks(context).map(blockPos -> new BlockPos(x0, blockPos.getY(), blockPos.getZ())).toList());
            }
        } else if (z1 == z2) {
                        for (int z = z1; z1 < z3 ? z <= z3 : z >= z3; z += z1 < z3 ? 1 : -1) {
                int z0 = z;
                list.addAll(Circle.collectCircleBlocks(context).map(blockPos -> new BlockPos(blockPos.getX(), blockPos.getY(), z0)).toList());
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
            return traceLineY(player, context);
        } else if (x1 == x2) {
            return traceLineX(player, context);
        } else if (z1 == z2) {
            return traceLineZ(player, context);
        }
        return null;
    }

    @Override
    protected Stream<BlockPos> collectStartBlocks(Context context) {
        return Single.collectSingleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectInterBlocks(Context context) {
        return Circle.collectCircleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectCylinderBlocks(context);
    }
}