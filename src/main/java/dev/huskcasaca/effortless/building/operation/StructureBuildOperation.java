package dev.huskcasaca.effortless.building.operation;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public final class StructureBuildOperation extends StructureOperation {
    private final Level level;
    private final Player player;
    private final Context context;
    private final Storage storage;

    public StructureBuildOperation(
            Level level,
            Player player,
            Context context,
            Storage storage
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
    }

    private static BlockState getBlockStateFromMainHand(Player player, BlockHitResult hitResult) {
        return getBlockStateFromItem(player, InteractionHand.MAIN_HAND, hitResult);
    }

    private static BlockState getBlockStateFromItem(Player player, InteractionHand hand, BlockHitResult hitResult) {
        var itemStack = player.getItemInHand(hand);
        var blockPlaceContext = new BlockPlaceContext(player, hand, itemStack, hitResult);
        var item = itemStack.getItem();

        if (item instanceof BlockItem blockItem) {
            var state = blockItem.getPlacementState(blockPlaceContext);
            return state != null ? state : Blocks.AIR.defaultBlockState();
        } else {
            return Block.byItem(item).getStateForPlacement(blockPlaceContext);
        }
    }

    public Stream<SingleBlockOperation> stream() {
        var state = context.state();

        switch (context.state()) {
            case IDLE -> {
                return Stream.empty();
            }
            case PLACING -> {
                return context.collect()
                        .result()
                        .stream()
                        .map((hitResult) -> new SingleBlockPlaceOperation(
                                level, player, context, storage,
                                hitResult.getBlockPos(),
                                getBlockStateFromMainHand(player, hitResult)))
                        .flatMap(Stream::of) // for modifiers
                        .map((op) -> op);
            }
            case BREAKING -> {
                return context.collect()
                        .result()
                        .stream()
                        .map((hitResult) -> new SingleBlockBreakOperation(
                                level, player, context, storage,
                                hitResult.getBlockPos()))
                        .flatMap(Stream::of) // for modifiers
                        .map((op) -> op);
            }
        }
        return Stream.empty();
    }

    public Result perform() {
        return new Result(this, context.collect().type(), stream().map(Operation::perform).toList());
    }

    @Override
    public BlockPos getPosition() {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Player player() {
        return player;
    }

    public Storage storage() {
        return storage;
    }

    @Override
    public Context context() {
        return context;
    }

}
