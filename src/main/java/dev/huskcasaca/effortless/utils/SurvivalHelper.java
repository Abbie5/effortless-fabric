package dev.huskcasaca.effortless.utils;

import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SurvivalHelper {

    public static InteractionResult placeBlockByState(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return InteractionResult.FAIL;
        }
        if (!canPlace(level, player, blockPos, blockState)) {
            return InteractionResult.FAIL;
        }
        if ((blockState.isAir() || !level.getBlockState(blockPos).isAir()) && !canBreak(level, player, blockPos)) {
            return InteractionResult.FAIL;
        }

        if (player instanceof ServerPlayer) {
            return placeBlockByStateServer(level, (ServerPlayer) player, interactionHand, blockPos, blockState);
        } else {
            return placeBlockByStateClient(level, (LocalPlayer) player, interactionHand, blockPos, blockState);
        }
    }

    private static InteractionResult placeBlockByStateServer(Level level, ServerPlayer player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
        var itemStack = player.getItemInHand(interactionHand);
        var fakeResult = new BlockHitResult(Vec3.ZERO, Direction.UP, blockPos, false);

        var blockStateInWorld = level.getBlockState(blockPos);
        if (!blockStateInWorld.getBlock().isEnabled(level.enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        var itemStack2 = itemStack.copy();

        if (itemStack.isEmpty()) {
            return InteractionResult.PASS;
        }
        var blockStatePlaceContext = new BlockStatePlaceContext(level, player, interactionHand, itemStack, fakeResult, blockState);
        InteractionResult interactionResult;
        if (player.isCreative()) {
            int i = itemStack.getCount();
            interactionResult = ItemStackUtils.useBlockItemStackOn(blockStatePlaceContext);
            itemStack.setCount(i);
        } else {
            interactionResult = ItemStackUtils.useBlockItemStackOn(blockStatePlaceContext);
        }

        if (interactionResult.consumesAction()) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack2);
        }

        return interactionResult;

    }

    private static InteractionResult placeBlockByStateClient(Level level, LocalPlayer localPlayer, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
        var itemStack = localPlayer.getItemInHand(interactionHand);
        var blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, blockPos, false);

        if (localPlayer.isSpectator()) {
            return InteractionResult.SUCCESS;
        }

        if (itemStack.isEmpty()) {
            return InteractionResult.PASS;
        }
        var blockStatePlaceContext = new BlockStatePlaceContext(level, localPlayer, interactionHand, itemStack, blockHitResult, blockState);
//        UseOnContext useOnContext = new UseOnContext(localPlayer, interactionHand, blockHitResult);
        InteractionResult interactionResult2;
        if (localPlayer.isCreative()) {
            int i = itemStack.getCount();
//            interactionResult2 = itemStack.useOn(useOnContext);
            interactionResult2 = ItemStackUtils.useBlockItemStackOn(blockStatePlaceContext);
            itemStack.setCount(i);
        } else {
//            interactionResult2 = itemStack.useOn(useOnContext);
            interactionResult2 = ItemStackUtils.useBlockItemStackOn(blockStatePlaceContext);
        }

        return interactionResult2;
    }

    public static boolean destroyBlock(Level level, Player player, BlockPos blockPos, boolean skipChecks) {
        if (!skipChecks && !canBreak(level, player, blockPos)) return false;
        if (player instanceof ServerPlayer) {
            return destroyBlockServer(level, (ServerPlayer) player, blockPos);
        } else {
            return destroyBlockClient(level, (LocalPlayer) player, blockPos);
        }
    }

    private static boolean destroyBlockClient(Level level, LocalPlayer player, BlockPos blockPos) {
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

    private static boolean destroyBlockServer(Level level, ServerPlayer player, BlockPos blockPos) {
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
        if (player.blockActionRestricted(level, blockPos, player.gameMode.getGameModeForPlayer())) {
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

    public static boolean canPlace(Level level, Player player, BlockPos blockPos, BlockState newBlockState) {
        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        if (player.blockActionRestricted(level, blockPos, gameMode)) {
            return false;
        }
        if (BuildModifierHelper.isReplace(player)) {
            if (player.isCreative()) return true;
            return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
        }
        return level.getBlockState(blockPos).canBeReplaced(); // fluid
    }

    public static boolean canBreak(Level level, Player player, BlockPos blockPos) {
        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        if (player.blockActionRestricted(level, blockPos, gameMode)) {
            return false;
        }
        if (gameMode.isCreative()) {
            return true;
        }
        return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public static boolean doesBecomeDoubleSlab(Player player, BlockPos pos, Direction facing) {
        BlockState placedBlockState = player.level.getBlockState(pos);

        ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (CompatHelper.isItemBlockProxy(itemstack))
            itemstack = CompatHelper.getItemBlockFromStack(itemstack);

        if (itemstack.isEmpty() || !(itemstack.getItem() instanceof BlockItem) || !(((BlockItem) itemstack.getItem()).getBlock() instanceof SlabBlock heldSlab))
            return false;

        return false;
    }
}
