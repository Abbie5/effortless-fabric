package dev.effortless.building.mode.builder.oneclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.builder.SingleClickBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public class Disable extends SingleClickBuilder {

    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return (Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult) ? hitResult : null;
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return Stream.of(context.firstPos());
    }

}
