package dev.huskcasaca.effortless.building.mode.builder;

import dev.huskcasaca.effortless.building.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public interface Builder {

    BlockHitResult trace(Player player, Context context);

    Stream<BlockPos> collect(Context context);

    int totalClicks(Context context);


}
