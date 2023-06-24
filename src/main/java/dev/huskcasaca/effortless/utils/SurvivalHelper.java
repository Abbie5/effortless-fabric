package dev.huskcasaca.effortless.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;

public class SurvivalHelper {

    public static boolean destroyBlock(Level level, Player player, BlockPos blockPos) {
        if (player.getLevel().isClientSide()) {
            return destroyBlockClient(level, player, blockPos);
        } else {
            return destroyBlockServer(level, player, blockPos);
        }
    }

    private static boolean destroyBlockClient(Level level, Player player, BlockPos blockPos) {
        if (player.blockActionRestricted(level, blockPos, Minecraft.getInstance().gameMode.getPlayerMode())) {
            return false;
        }
        var blockState = level.getBlockState(blockPos);
        if (!player.getMainHandItem().getItem().canAttackBlock(blockState, level, blockPos, player)) {
            return false;
        }
        var block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            return false;
        }
        if (blockState.isAir()) {
            return false;
        }
        block.playerWillDestroy(level, blockPos, blockState, player);
        var fluidState = level.getFluidState(blockPos);
        boolean removed = level.setBlock(blockPos, fluidState.createLegacyBlock(), 11);
        if (removed) {
            block.destroy(level, blockPos, blockState);
        }
        return removed;
    }

    private static boolean destroyBlockServer(Level level, Player player, BlockPos blockPos) {
        var blockState = level.getBlockState(blockPos);
        if (!player.getMainHandItem().getItem().canAttackBlock(blockState, level, blockPos, player)) {
            return false;
        }
        var blockEntity = level.getBlockEntity(blockPos);
        var block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            return false;
        }
        if (player.blockActionRestricted(level, blockPos, ((ServerPlayer) player).gameMode.getGameModeForPlayer())) {
            return false;
        }
        block.playerWillDestroy(level, blockPos, blockState, player);
        var removed = level.removeBlock(blockPos, false);
        if (removed) {
            block.destroy(level, blockPos, blockState);
        }
        if (player.isCreative()) {
            return true;
        }
        var itemStack = player.getMainHandItem();
        var itemStack2 = itemStack.copy();
        var correctTool = player.hasCorrectToolForDrops(blockState);
        itemStack.mineBlock(level, blockState, blockPos, player);
        if (removed && correctTool) {
            block.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack2);
        }
        return true;
    }

//    public static boolean canPlace(Level level, Player player, BlockPos blockPos, boolean replace, BlockState newBlockState /* TODO: 7/3/23 remove */) {
//        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
//        if (player.blockActionRestricted(level, blockPos, gameMode)) {
//            return false;
//        }
//        if (replace) {
//            if (player.isCreative()) return true;
//            return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
//        }
//        return level.getBlockState(blockPos).canBeReplaced(); // fluid
//    }
//
//    public static boolean canBreak(Level level, Player player, BlockPos blockPos) {
//        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
//        if (player.blockActionRestricted(level, blockPos, gameMode)) {
//            return false;
//        }
//        if (gameMode.isCreative()) {
//            return true;
//        }
//        return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE);
//    }
}
