package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.builder.ThreeClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Circle;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public class Cylinder extends ThreeClickBuilder {

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

        if (Objects.requireNonNull(context.planeFacing()) == BuildFeature.PlaneFacing.HORIZONTAL) {
            var circleBlocks = Circle.collectFloorCircleBlocks(context).toList();
            int lowest = Math.min(y1, y3);
            int highest = Math.max(y1, y3);

            for (int y = lowest; y <= highest; y++) {
                for (BlockPos blockPos : circleBlocks) {
                    list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
                }
            }
        } else {
            var circleBlocks = Circle.collectWallCircleBlocks(context).toList();
            if (x1 != x2) {
                int lowest = Math.min(z1, z3);
                int highest = Math.max(z1, z3);

                for (int z = lowest; z <= highest; z++) {
                    for (BlockPos blockPos : circleBlocks) {
                        list.add(new BlockPos(blockPos.getX(), blockPos.getY(), z));
                    }
                }
            } else {
                int lowest = Math.min(x1, x3);
                int highest = Math.max(x1, x3);

                for (int x = lowest; x <= highest; x++) {
                    for (BlockPos blockPos : circleBlocks) {
                        list.add(new BlockPos(x, blockPos.getY(), blockPos.getZ()));
                    }
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
        if (context.planeFacing() == BuildFeature.PlaneFacing.HORIZONTAL) {
            return Floor.traceFloor(player, context);
        } else {
            return Wall.traceWall(player, context);
        }
    }

    @Override
    protected BlockHitResult traceThirdHit(Player player, Context context) {
        if (context.planeFacing() == BuildFeature.PlaneFacing.HORIZONTAL) {
            return traceLineY(player, context);
        } else {
            if (context.firstPos().getX() == context.secondPos().getX()) {
                return tracePlaneZ(player, context);
            } else {
                return tracePlaneX(player, context);
            }
        }
    }

    @Override
    protected Stream<BlockPos> collectStartBlocks(Context context) {
        return Single.collectSingleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectInterBlocks(Context context) {
        if (context.planeFacing() == BuildFeature.PlaneFacing.HORIZONTAL) {
            return Circle.collectFloorCircleBlocks(context);
        } else {
            return Circle.collectWallCircleBlocks(context);
        }
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectCylinderBlocks(context);
    }
}