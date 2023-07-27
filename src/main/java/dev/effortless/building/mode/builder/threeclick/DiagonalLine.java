package dev.effortless.building.mode.builder.threeclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.builder.TripleClickBuilder;
import dev.effortless.building.mode.builder.oneclick.Single;
import dev.effortless.building.mode.builder.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.stream.Stream;

public class DiagonalLine extends TripleClickBuilder {

    public static Stream<BlockPos> collectPlaneDiagonalLineBlocks(Context context, float sampleMultiplier) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.secondPos().getX();
        var y2 = context.secondPos().getY();
        var z2 = context.secondPos().getZ();

        var first = new Vec3(x1, y1, z1).add(0.5, 0.5, 0.5);
        var second = new Vec3(x2, y2, z2).add(0.5, 0.5, 0.5);

        int iterations = (int) Math.ceil(first.distanceTo(second) * sampleMultiplier);
        for (double t = 0; t <= 1.0; t += 1.0 / iterations) {
            Vec3 lerp = first.add(second.subtract(first).scale(t));
            BlockPos candidate = BlockPos.containing(lerp);
            // only add if not equal to the last in the list
            if (list.isEmpty() || !list.get(list.size() - 1).equals(candidate))
                list.add(candidate);
        }

        return list.stream();
    }

    public static Stream<BlockPos> collectDiagonalLineBlocks(Context context, float sampleMultiplier) {
        var list = new ArrayList<BlockPos>();

        var x1 = context.firstPos().getX();
        var y1 = context.firstPos().getY();
        var z1 = context.firstPos().getZ();
        var x2 = context.thirdPos().getX();
        var y2 = context.thirdPos().getY();
        var z2 = context.thirdPos().getZ();

        var first = new Vec3(x1, y1, z1).add(0.5, 0.5, 0.5);
        var second = new Vec3(x2, y2, z2).add(0.5, 0.5, 0.5);

        int iterations = (int) Math.ceil(first.distanceTo(second) * sampleMultiplier);
        for (double t = 0; t <= 1.0; t += 1.0 / iterations) {
            Vec3 lerp = first.add(second.subtract(first).scale(t));
            BlockPos candidate = BlockPos.containing(lerp);
            // only add if not equal to the last in the list
            if (list.isEmpty() || !list.get(list.size() - 1).equals(candidate))
                list.add(candidate);
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
        return collectPlaneDiagonalLineBlocks(context, 10);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectDiagonalLineBlocks(context, 10);
    }
}
