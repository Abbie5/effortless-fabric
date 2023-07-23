package dev.effortless.building.mode.builder;

import dev.effortless.building.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public abstract class DoubleClickBuilder extends AbstractClickBuilder {

    protected abstract BlockHitResult traceFirstHit(Player player, Context context);

    protected abstract BlockHitResult traceSecondHit(Player player, Context context);

    protected Stream<BlockPos> collectFirstBlocks(Context context) {
        return Stream.of(context.firstPos());
    }

    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return Stream.empty();
    }

    @Override
    public BlockHitResult trace(Player player, Context context) {
        return switch (context.clicks()) {
            case 0 -> traceFirstHit(player, context);
            case 1 -> traceSecondHit(player, context);
            default -> null;
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
