package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.utils.SurvivalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPosState(
        Level level,
        BlockPos blockPos,
        BlockState blockState,
        boolean place
) {

    public boolean canPlaceBy(Player player) {
        return SurvivalHelper.canPlace(level, player, blockPos, blockState);
    }

    public boolean canBreakBy(Player player) {
        return SurvivalHelper.canBreak(level, player, blockPos);
    }

    public boolean destroyBy(Player player) {
        return SurvivalHelper.destroyBlock(level, player, blockPos, false);
    }

    public InteractionResult placeBy(Player player, InteractionHand interactionHand) {
        return SurvivalHelper.placeBlockByState(level, player, interactionHand, blockPos, blockState);
    }

}
