package dev.huskcasaca.effortless.buildmodifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

public interface Modifier {

    Set<BlockPos> findCoordinates(Player player, BlockPos startPos);

    Map<BlockPos, BlockState> findBlockStates(Player player, BlockPos startPos, BlockState blockState);

}