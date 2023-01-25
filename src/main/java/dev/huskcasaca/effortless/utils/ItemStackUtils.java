package dev.huskcasaca.effortless.utils;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemStackUtils {

//    public static InteractionResult useBlockItemNoCooldown(Level level, ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, BlockState blockState) {
//
//        var blockPos = blockHitResult.getBlockPos();
//        var blockStateInWorld = level.getBlockState(blockPos);
//        if (!blockStateInWorld.getBlock().isEnabled(level.enabledFeatures())) {
//            return InteractionResult.FAIL;
//        }
//        if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
//            return InteractionResult.PASS;
//        }
////        boolean bl = !serverPlayer.getMainHandItem().isEmpty();
////        boolean bl2 = serverPlayer.isSecondaryUseActive() && bl;
////        var itemStack2 = itemStack.copy();
////        if (!bl2) {
////            InteractionResult interactionResult = blockStateInWorld.use(level, serverPlayer, interactionHand, blockHitResult);
////            if (interactionResult.consumesAction()) {
////                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
////                return interactionResult;
////            }
////        }
//        var itemStack2 = itemStack.copy();
//
//        if (itemStack.isEmpty()) {
//            return InteractionResult.PASS;
//        }
//        var blockStatePlaceContext = new BlockStatePlaceContext(serverPlayer.level, serverPlayer, interactionHand, itemStack, blockHitResult, blockState);
//        InteractionResult interactionResult2;
//        if (serverPlayer.isCreative()) {
//            int i = itemStack.getCount();
//            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext);
//            itemStack.setCount(i);
//        } else {
//            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext);
//        }
//
//        if (interactionResult2.consumesAction()) {
//            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
//        }
//
//        return interactionResult2;
//    }

    public static InteractionResult useBlockItemStackOn(BlockStatePlaceContext blockStatePlaceContext) {
        var itemStack = blockStatePlaceContext.getItemInHand();
        var player = blockStatePlaceContext.getPlayer();
        var blockPos = blockStatePlaceContext.getClickedPos();
        var blockInWorld = new BlockInWorld(blockStatePlaceContext.getLevel(), blockPos, false);
        if (player != null && !player.getAbilities().mayBuild && !itemStack.hasAdventureModePlaceTagForBlock(blockStatePlaceContext.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)) {
            return InteractionResult.PASS;
        }
        var item = itemStack.getItem();
        var interactionResult = ItemStackUtils.useBlockItemOn((BlockItem) item, blockStatePlaceContext);
        if (player != null && interactionResult.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }

        return interactionResult;
    }

    private static InteractionResult useBlockItemOn(BlockItem blockItem, BlockStatePlaceContext blockStatePlaceContext) {

        if (!blockItem.getBlock().isEnabled(blockStatePlaceContext.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        if (!blockStatePlaceContext.canPlace()) {
            return InteractionResult.FAIL;
        }
//        var blockStatePlaceContext = blockItem.updatePlacementContext(blockPlaceContext);

        var blockState = blockStatePlaceContext.getPlaceState();
        if (blockState == null) {
            return InteractionResult.FAIL;
        }
        if (!blockItem.placeBlock(blockStatePlaceContext, blockState)) {
            return InteractionResult.FAIL;
        }

        var blockPos = blockStatePlaceContext.getClickedPos();
        var level = blockStatePlaceContext.getLevel();
        var player = blockStatePlaceContext.getPlayer();
        var itemStack = blockStatePlaceContext.getItemInHand();
        var blockState2 = level.getBlockState(blockPos);
        if (blockState2.is(blockState.getBlock())) {
            blockState2 = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState2);
            blockItem.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState2);
            blockState2.getBlock().setPlacedBy(level, blockPos, blockState2, player, itemStack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }
        }
        var soundType = blockState2.getSoundType();
        level.playSound(player, blockPos, blockItem.getPlaceSound(blockState2), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player, blockState2));
        if (player == null || !player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
