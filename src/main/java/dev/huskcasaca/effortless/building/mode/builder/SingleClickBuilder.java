package dev.huskcasaca.effortless.building.mode.builder;

import dev.huskcasaca.effortless.building.BuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public abstract class SingleClickBuilder implements Builder {

    protected abstract BlockHitResult traceFirstHit(Player player, BuildContext context);

    protected abstract Stream<BlockPos> collectFinalBlocks(BuildContext context);

    @Override
    public BlockHitResult trace(Player player, BuildContext context) {
        return switch (context.clicks()) {
            case 0 -> traceFirstHit(player, context);
            default -> BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO); // FIXME: 7/3/23
        };
    }

    @Override
    public Stream<BlockPos> collect(BuildContext context) {
        return collectFinalBlocks(context);
    }

    @Override
    public int totalClicks(BuildContext context) {
        return 1;
    }

}
