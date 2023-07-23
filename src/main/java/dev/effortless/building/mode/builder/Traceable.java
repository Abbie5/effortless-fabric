package dev.effortless.building.mode.builder;

import dev.effortless.building.Context;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public interface Traceable {

    BlockHitResult trace(Player player, Context context);

}
