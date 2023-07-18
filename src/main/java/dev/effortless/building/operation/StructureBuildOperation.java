package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
    private final Boolean once;

    public StructureBuildOperation(
            Level level,
            Player player,
            Context context,
            Storage storage,
            Boolean once
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
        this.once = once;
    }

    public StructureBuildOperation(
            Level level,
            Player player,
            Context context
    ) {
        this(level, player, context, null, false);
    }

    private static BlockState getBlockStateFromItem(Player player, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult) {
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
        switch (context.state()) {
            case IDLE -> {
                return Stream.empty();
            }
            case PLACE_BLOCK -> {
                var itemStack = player.getMainHandItem().copy();
                return context.collect()
                        .map((hitResult) -> new SingleBlockPlaceOperation(
                                level, player, context, storage,
                                hitResult.getBlockPos(),
                                hitResult.getDirection(),
                                getBlockStateFromItem(player, itemStack, InteractionHand.MAIN_HAND, hitResult)))
                        .flatMap(Stream::of) // for modifiers
                        .map((op) -> op);
            }
            case BREAK_BLOCK -> {
                return context.collect()
                        .map((hitResult) -> new SingleBlockBreakOperation(
                                level, player, context, storage,
                                hitResult.getBlockPos(),
                                hitResult.getDirection(),
                                once))
                        .flatMap(Stream::of) // for modifiers
                        .map((op) -> op);
            }
        }
        return Stream.empty();
    }

    public StructureOperationResult perform() {
        return new StructureOperationResult(this, context.tracingResult(), stream().filter(Operation.distinctByPosition()).map(Operation::perform).toList());
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

    @Override
    public boolean isPreview() {
        return storage != null;
    }
}
