package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.building.operation.BlockStatePlaceOperation;
import dev.huskcasaca.effortless.building.replace.ReplaceMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public final class BlockFilter implements Predicate<BlockStatePlaceOperation> {

    private final Level level;
    private final Player player;
    private final BuildingState state;
    private final ReplaceMode replaceMode;

    public BlockFilter(
            Level level,
            Player player,
            BuildingState state,
            ReplaceMode replaceMode
    ) {
        this.level = level;
        this.player = player;
        this.state = state;
        this.replaceMode = replaceMode;
    }

    private static boolean canInteract(Level level, Player player, BlockPos blockPos) {
        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        return !player.blockActionRestricted(level, blockPos, gameMode);
    }

    private boolean testBreak(Level level, Player player, BlockPos blockPos) {
        if (!canInteract(level, player, blockPos)) {
            return false;
        }
        if (player.isCreative()) {
            return true;
        }
        return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    private boolean testPlace(Level level, Player player, BlockPos blockPos) {
        if (!canInteract(level, player, blockPos)) {
            return false;
        }
        if (replaceMode.isReplace()) {
            if (player.isCreative()) return true;
            return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
        }
        return level.getBlockState(blockPos).canBeReplaced(); // fluid
    }

    @Override
    public boolean test(BlockStatePlaceOperation operation) {
        return switch (state){
            case BREAKING -> testBreak(level, player, operation.blockPos());
            case PLACING, IDLE -> testPlace(level, player, operation.blockPos());
        };
    }
}
