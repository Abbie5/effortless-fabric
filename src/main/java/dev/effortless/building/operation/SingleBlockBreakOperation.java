package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;

public final class SingleBlockBreakOperation extends SingleBlockOperation {
    private final Level level;
    private final Player player;
    private final Context context;
    private final Storage storage;
    private final BlockPos blockPos;

    public SingleBlockBreakOperation(
            Level level,
            Player player,
            Context context,
            Storage storage, // for preview
            BlockPos blockPos
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
        this.blockPos = blockPos;
    }

    public static boolean breakBlock(Level level, Player player, BlockPos blockPos) {
        if (player.getLevel().isClientSide()) {
            return breakBlockClient(level, player, blockPos);
        } else {
            return breakBlockServer(level, player, blockPos);
        }
    }

    private static boolean breakBlockClient(Level level, Player player, BlockPos blockPos) {
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

    private static boolean breakBlockServer(Level level, Player player, BlockPos blockPos) {
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

    private static boolean testBreak(Context context, Level level, Player player, BlockPos blockPos) {
        if (!canInteract(level, player, blockPos)) {
            return false;
        }
        if (player.isCreative()) {
            return true;
        }
        return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    @Override
    public SingleBlockOperationResult perform() {
        var inputs = Collections.<ItemStack>emptyList();
        var outputs = Collections.singletonList(level.getBlockState(blockPos).getBlock().asItem().getDefaultInstance());
//        var outputs = level.getBlockState(blockPos).getDrops(level);

        if (storage != null) {
            return new SingleBlockOperationResult(this, InteractionResult.SUCCESS, inputs, outputs);
        } else {
            var result = breakBlock(level, player, blockPos);
            // TODO: 25/6/23 InteractionResult
            return new SingleBlockOperationResult(this, result ? InteractionResult.SUCCESS : InteractionResult.PASS, inputs, outputs);
        }
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
    public Type getType() {
        return Type.WORLD_PLACE_OP;
    }

    @Override
    public DefaultRenderer getRenderer() {
        return DefaultRenderer.getInstance();
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

}