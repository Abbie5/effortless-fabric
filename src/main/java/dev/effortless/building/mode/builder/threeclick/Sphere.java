package dev.effortless.building.mode.builder.threeclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.BuildFeature;
import dev.effortless.building.mode.builder.TripleClickBuilder;
import dev.effortless.building.mode.builder.oneclick.Single;
import dev.effortless.building.mode.builder.twoclick.Circle;
import dev.effortless.building.mode.builder.twoclick.Floor;
import dev.effortless.building.mode.builder.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Sphere extends TripleClickBuilder {

    public static Stream<BlockPos> collectSphereBlocks(Context context) {
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

        float centerX = x1;
        float centerY = y1;
        float centerZ = z1;

        //Adjust for CIRCLE_START

        float radiusX;
        float radiusY;
        float radiusZ;

        if (context.buildFeatures().contains(BuildFeature.PlaneFacing.HORIZONTAL)) {
            if (context.circleStart() == BuildFeature.CircleStart.CIRCLE_START_CORNER) {
                centerX = x1 + (x2 - x1) / 2f;
                centerY = y1 + (y3 - y1) / 2f;
                centerZ = z1 + (z2 - z1) / 2f;
            } else {
                x1 = (int) (centerX - (x2 - centerX));
                y1 = (int) (centerY - (y3 - centerY));
                z1 = (int) (centerZ - (z2 - centerZ));
            }
            radiusX = Mth.abs(x2 - centerX);
            radiusY = Mth.abs(y3 - centerY);
            radiusZ = Mth.abs(z2 - centerZ);
        } else {
            if (x1 == x2) {
                if (context.circleStart() == BuildFeature.CircleStart.CIRCLE_START_CORNER) {
                    centerX = x1 + (x3 - x1) / 2f;
                    centerY = y1 + (y2 - y1) / 2f;
                    centerZ = z1 + (z2 - z1) / 2f;
                } else {
                    x1 = (int) (centerX - (x3 - centerX));
                    y1 = (int) (centerY - (y2 - centerY));
                    z1 = (int) (centerZ - (z2 - centerZ));
                }
                radiusX = Mth.abs(x3 - centerX);
                radiusY = Mth.abs(y2 - centerY);
                radiusZ = Mth.abs(z2 - centerZ);
            } else {
                if (context.circleStart() == BuildFeature.CircleStart.CIRCLE_START_CORNER) {
                    centerX = x1 + (x2 - x1) / 2f;
                    centerY = y1 + (y2 - y1) / 2f;
                    centerZ = z1 + (z3 - z1) / 2f;
                } else {
                    x1 = (int) (centerX - (x2 - centerX));
                    y1 = (int) (centerY - (y2 - centerY));
                    z1 = (int) (centerZ - (z3 - centerZ));
                }
                radiusX = Mth.abs(x2 - centerX);
                radiusY = Mth.abs(y2 - centerY);
                radiusZ = Mth.abs(z3 - centerZ);
            }
        }
        if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL) {
            addSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);
        } else {
            addHollowSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);
        }

        return list.stream();
    }

    public static void addSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                       float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                             float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f && distance > radius - 0.6f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    private static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return Mth.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
    }

    public static float calculateSpheroidRadius(float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ, int x, int y, int z) {
        //Twice ellipse radius
        float radiusXZ = Circle.calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, x, z);

        //TODO project x to plane

        return Circle.calculateEllipseRadius(centerX, centerY, radiusXZ, radiusY, x, y);
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
         return Circle.collectCircleBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectSphereBlocks(context);
    }
}