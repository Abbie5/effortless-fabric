package dev.huskcasaca.effortless.utils;

public class SurvivalHelper {


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
