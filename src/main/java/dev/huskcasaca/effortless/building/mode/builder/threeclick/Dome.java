package dev.huskcasaca.effortless.building.mode.builder.threeclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.builder.ThreeClickBuilder;
import dev.huskcasaca.effortless.building.mode.builder.oneclick.Single;
import dev.huskcasaca.effortless.building.mode.builder.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public class Dome extends ThreeClickBuilder {

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
        return Floor.collectFloorBlocks(context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
        //TODO
        return SlopeFloor.collectSlopeFloorBlocks(context);
    }
}