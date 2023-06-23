package dev.huskcasaca.effortless.building.mode.builder.twoclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.builder.TwoClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Circle extends TwoClickBuilder {

    private static float distance(float a1, float b1, float a2, float b2) {
        return Mth.sqrt((a2 - a1) * (a2 - a1) + (b2 - b1) * (b2 - b1));
    }

    public static float calculateEllipseRadius(float centerA, float centerB, float radiusA, float radiusB, int a, int b) {
        //https://math.stackexchange.com/questions/432902/how-to-get-the-radius-of-an-ellipse-at-a-specific-angle-by-knowing-its-semi-majo
        float theta = (float) Mth.atan2(b - centerB, a - centerA);
        float part1 = radiusA * radiusA * Mth.sin(theta) * Mth.sin(theta);
        float part2 = radiusB * radiusB * Mth.cos(theta) * Mth.cos(theta);
        return radiusA * radiusB / Mth.sqrt(part1 + part2);
    }

    public static void addCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerX, float centerZ, float radiusX, float radiusZ) {

        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                float distance = distance(l, n, centerX, centerZ);
                float radius = calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, l, n);
                if (distance < radius + 0.4f)
                    list.add(new BlockPos(l, y1, n));
            }
        }
    }

    public static void addHollowCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerX, float centerZ, float radiusX, float radiusZ) {

        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                float distance = distance(l, n, centerX, centerZ);
                float radius = calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, l, n);
                if (distance < radius + 0.4f && distance > radius - 0.6f)
                    list.add(new BlockPos(l, y1, n));
            }
        }
    }

    public static void addXWallCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerY, float centerZ, float radiusY, float radiusZ) {

        for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
            for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {
                float distance = distance(y, z, centerY, centerZ);
                float radius = calculateEllipseRadius(centerY, centerZ, radiusY, radiusZ, y, z);
                if (distance < radius + 0.4f)
                    list.add(new BlockPos(x1, y, z));
            }
        }
    }

    public static void addZWallCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerY, float centerX, float radiusY, float radiusX) {

        for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
            for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {
                float distance = distance(y, x, centerY, centerX);
                float radius = calculateEllipseRadius(centerY, centerX, radiusY, radiusX, y, x);
                if (distance < radius + 0.4f)
                    list.add(new BlockPos(x, y, z1));
            }
        }
    }

    public static void addXHollowWallCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerY, float centerZ, float radiusY, float radiusZ) {

        for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
            for (int z = z1; z1 < z2 ? z <= z2 : z >= z2; z += z1 < z2 ? 1 : -1) {
                float distance = distance(y, z, centerY, centerZ);
                float radius = calculateEllipseRadius(centerY, centerZ, radiusY, radiusZ, y, z);
                if (distance < radius + 0.4f && distance > radius - 0.6f)
                    list.add(new BlockPos(x1, y, z));
            }
        }
    }

    public static void addZHollowWallCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, float centerY, float centerX, float radiusY, float radiusX) {

        for (int y = y1; y1 < y2 ? y <= y2 : y >= y2; y += y1 < y2 ? 1 : -1) {
            for (int x = x1; x1 < x2 ? x <= x2 : x >= x2; x += x1 < x2 ? 1 : -1) {
                float distance = distance(y, x, centerY, centerX);
                float radius = calculateEllipseRadius(centerY, centerX, radiusY, radiusX, y, x);
                if (distance < radius + 0.4f && distance > radius - 0.6f)
                    list.add(new BlockPos(x, y, z1));
            }
        }
    }

    public static Stream<BlockPos> collectFloorCircleBlocks(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        float centerX = x1;
        float centerZ = z1;

        if (context.circleStart() == BuildFeature.CircleStart.CIRCLE_START_CORNER) {
            centerX = x1 + (x2 - x1) / 2f;
            centerZ = z1 + (z2 - z1) / 2f;
        } else {
            x1 = (int) (centerX - (x2 - centerX));
            z1 = (int) (centerZ - (z2 - centerZ));
        }

        float radiusX = Mth.abs(x2 - centerX);
        float radiusZ = Mth.abs(z2 - centerZ);

        if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL) {
            addCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerX, centerZ, radiusX, radiusZ);
        } else {
            addHollowCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerX, centerZ, radiusX, radiusZ);
        }

        return list.stream();
    }

    public static Stream<BlockPos> collectWallCircleBlocks(BuildContext context) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        float centerX = x1;
        float centerY = y1;
        float centerZ = z1;

        //Adjust for CIRCLE_START
        if (context.circleStart() == BuildFeature.CircleStart.CIRCLE_START_CORNER) {
            centerX = x1 + (x2 - x1) / 2f;
            centerY = y1 + (y2 - y1) / 2f;
            centerZ = z1 + (z2 - z1) / 2f;
        } else {
            x1 = (int) (centerX - (x2 - centerX));
            y1 = (int) (centerY - (y2 - centerY));
            z1 = (int) (centerZ - (z2 - centerZ));
        }
        float radiusX = Mth.abs(x2 - centerX);
        float radiusY = Mth.abs(y2 - centerY);
        float radiusZ = Mth.abs(z2 - centerZ);

        if (x1 == x2) {
            if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL) {
                addXWallCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerY, centerZ, radiusY, radiusZ);
            } else {
                addXHollowWallCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerY, centerZ, radiusY, radiusZ);
            }
        } else {
            if (context.planeFilling() == BuildFeature.PlaneFilling.PLANE_FULL) {
                addZWallCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerY, centerX, radiusY, radiusX);
            } else {
                addZHollowWallCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerY, centerZ, radiusY, radiusZ);
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
        if (context.planeFacing() == BuildFeature.PlaneFacing.HORIZONTAL) {
            return Floor.traceFloor(player, context);
        } else {
            return Wall.traceWall(player, context);
        }
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
        if (context.planeFacing() == BuildFeature.PlaneFacing.HORIZONTAL) {
            return collectFloorCircleBlocks(context);
        } else {
            return collectWallCircleBlocks(context);
        }
    }

}
