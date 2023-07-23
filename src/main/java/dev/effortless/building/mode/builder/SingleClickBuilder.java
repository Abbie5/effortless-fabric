package dev.effortless.building.mode.builder;

import dev.effortless.building.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public abstract class SingleClickBuilder extends AbstractClickBuilder {

    protected abstract BlockHitResult traceFirstHit(Player player, Context context);

    protected abstract Stream<BlockPos> collectFinalBlocks(Context context);

    @Override
    public BlockHitResult trace(Player player, Context context) {
        return switch (context.clicks()) {
            case 0 -> traceFirstHit(player, context);
            default -> null;
        };
    }

    @Override
    public Stream<BlockPos> collect(Context context) {
        return collectFinalBlocks(context);
    }

    @Override
    public int totalClicks(Context context) {
        return 1;
    }

}
