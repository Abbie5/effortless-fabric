package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;

public final class BlockBreakOperation extends BlockOperation {
    private final Level level;
    private final Player player;
    private final Context context;
    private final Storage storage;
    private final BlockPos blockPos;
    private final Direction direction;

    public BlockBreakOperation(
            Level level,
            Player player,
            Context context,
            Storage storage, // for preview
            BlockPos blockPos,
            Direction direction
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
        this.blockPos = blockPos;
        this.direction = direction;
    }

    private static BlockInteractionResult breakBlockClient(Level level, Player player, BlockPos blockPos, boolean preview) {
        if (player.blockActionRestricted(level, blockPos, Minecraft.getInstance().gameMode.getPlayerMode())) {
            return BlockInteractionResult.FAIL_LEVEL_INTERACT_RESTRICTED;
        }

        var blockState = level.getBlockState(blockPos);
        if (!player.getMainHandItem().getItem().canAttackBlock(blockState, level, blockPos, player)) {
            return BlockInteractionResult.FAIL_PLAYER_CANNOT_USE_ITEM_TO_ATTACK;
        }
        var block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            return BlockInteractionResult.FAIL_PLAYER_CANNOT_USE_GAME_MASTER_BLOCKS;
        }
        if (blockState.isAir()) {
            return BlockInteractionResult.FAIL_BLOCK_STATE_AIR;
        }
        if (preview) {
            return BlockInteractionResult.SUCCESS_PREVIEW;
        }
        block.playerWillDestroy(level, blockPos, blockState, player);
        var fluidState = level.getFluidState(blockPos);
        var removed = level.setBlock(blockPos, fluidState.createLegacyBlock(), 11);
        if (removed) {
            block.destroy(level, blockPos, blockState);
            return BlockInteractionResult.SUCCESS;
        } else {
            return BlockInteractionResult.FAIL_INTERNAL_SET_BLOCK;
        }
    }

    private static BlockInteractionResult breakBlockServer(Level level, Player player, BlockPos blockPos) {
        if (player.blockActionRestricted(level, blockPos, ((ServerPlayer) player).gameMode.getGameModeForPlayer())) {
            return BlockInteractionResult.FAIL_LEVEL_INTERACT_RESTRICTED;
        }
        var blockState = level.getBlockState(blockPos);
        if (!player.getMainHandItem().getItem().canAttackBlock(blockState, level, blockPos, player)) {
            return BlockInteractionResult.FAIL_PLAYER_ITEM_CANNOT_ATTACK_BLOCK;
        }
        var blockEntity = level.getBlockEntity(blockPos);
        var block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            return BlockInteractionResult.FAIL_PLAYER_CANNOT_USE_GAME_MASTER_BLOCKS;
        }
        block.playerWillDestroy(level, blockPos, blockState, player);
        var removed = level.removeBlock(blockPos, false);
        if (removed) {
            block.destroy(level, blockPos, blockState);
        }
        if (player.isCreative()) {
            return BlockInteractionResult.SUCCESS;
        }
        var itemStack = player.getMainHandItem();
        var itemStack2 = itemStack.copy();
        var correctTool = player.hasCorrectToolForDrops(blockState);
        itemStack.mineBlock(level, blockState, blockPos, player);
        if (removed && correctTool) {
            block.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack2);
        }
        return BlockInteractionResult.SUCCESS;
    }

    public static BlockInteractionResult breakBlock(Level level, Player player, BlockPos blockPos, Boolean preview) {

        if (!player.isCreative() && level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE)) {
            return BlockInteractionResult.FAIL_BLOCK_STATE_FLAG_CANNOT_REPLACE;
        }
        if (player.level().isClientSide()) {
            return breakBlockClient(level, player, blockPos, preview);
        } else {
            return breakBlockServer(level, player, blockPos);
        }
    }

    @Override
    public BlockResult perform() {
        var inputs = Collections.<ItemStack>emptyList();
        var outputs = Collections.singletonList(level.getBlockState(blockPos).getBlock().asItem().getDefaultInstance());
        var result = breakBlock(level, player, blockPos, isPreview());

        if (context.isPreviewOnce() && level.isClientSide() && result.consumesAction()) {
            Minecraft.getInstance().particleEngine.crack(blockPos, direction);
        }
        return new BlockResult(this, result, inputs, outputs);

    }

    @Override
    public ItemStack inputItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack outputItemStack() {
        return new ItemStack(level.getBlockState(blockPos).getBlock().asItem());
    }

    // block placement
    @Override
    public BlockPos getPosition() {
        return blockPos;
    }

    @Override
    public OperationType getType() {
        return OperationType.WORLD_BREAK_OP;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public Storage storage() {
        return storage;
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public BlockState blockState() {
        return level.getBlockState(blockPos);
    }

    @Override
    public Direction direction() {
        return direction;
    }

    @Override
    public boolean isPreview() {
        return storage() != null;
    }


}