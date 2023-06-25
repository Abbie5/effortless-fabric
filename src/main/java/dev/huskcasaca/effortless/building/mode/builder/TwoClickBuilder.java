package dev.huskcasaca.effortless.building.mode.builder;

import dev.huskcasaca.effortless.building.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public abstract class TwoClickBuilder extends MultipleClickBuilder {

    protected abstract BlockHitResult traceFirstHit(Player player, Context context);

    protected abstract BlockHitResult traceSecondHit(Player player, Context context);

    protected Stream<BlockPos> collectFirstBlocks(Context context) {
        return Stream.of(context.firstPos());
    };

    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return Stream.empty();
    }

    @Override
    public BlockHitResult trace(Player player, Context context) {
        return switch (context.clicks()) {
            case 0 -> traceFirstHit(player, context);
            case 1 -> traceSecondHit(player, context);
            default -> BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO); // FIXME: 7/3/23 ;
        };
    }

    @Override
    public Stream<BlockPos> collect(Context context) {
        return switch (context.clicks()) {
            case 1 -> collectFirstBlocks(context);
            case 2 -> collectFinalBlocks(context);
            default -> Stream.empty();
        };
    }

    @Override
    public int totalClicks(Context context) {
        return 2;
    }
}
