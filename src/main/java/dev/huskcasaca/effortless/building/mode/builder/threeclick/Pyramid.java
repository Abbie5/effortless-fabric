package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.mode.builder.TripleClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public class Pyramid extends TripleClickBuilder {

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
        //TODO
        return SlopeFloor.collectSlopeFloorBlocks(context);
    }
}