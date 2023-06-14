package dev.huskcasaca.effortless.building.mode.builder;

import dev.huskcasaca.effortless.building.BuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public interface Builder {

    BlockHitResult trace(Player player, BuildContext context);

    Stream<BlockPos> collect(BuildContext context);

    int totalClicks(BuildContext context);


}
