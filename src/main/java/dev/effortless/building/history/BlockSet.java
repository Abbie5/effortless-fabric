package dev.effortless.building.history;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public record BlockSet(
        Set<BlockPos> coordinates,
        List<BlockState> previousBlockStates,
        List<BlockState> newBlockStates,
        Vec3 hitVec,
        BlockPos firstPos,
        BlockPos secondPos
) {
}
