package dev.huskcasaca.effortless.building.operation;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.ItemStorage;
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

public final class StructurePlaceOperation extends StructureOperation {
    private final Level level;
    private final Player player;
    private final ItemStorage storage;
    private final BuildContext context;

    public StructurePlaceOperation(
            Level level,
            Player player,
            ItemStorage storage,
            BuildContext context
    ) {
        this.level = level;
        this.player = player;
        this.storage = storage;
        this.context = context;
    }

    public Stream<SingleBlockOperation> stream() {
        var state = context.state();

        if (context.isBuilding()) {
            return context.collect()
                    .result()
                    .stream()
                    .map((hitResult) -> context.isPlacing() ? new SingleBlockPlaceOperation(
                            level, player, storage, context,
                            hitResult.getBlockPos(),
                            getBlockStateFromMainHand(player, hitResult)) : new SingleBlockBreakOperation(
                            level, player, storage, context,
                            hitResult.getBlockPos()))
                    .flatMap(Stream::of) // for modifiers
                    .map((op) -> op);
        } else {
            return Stream.empty();
        }

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

    public ItemStorage storage() {
        return storage;
    }

    @Override
    public BuildContext context() {
        return context;
    }

    //    public static void placeBlocks(Player player, List<BlockHitResult> hitResults) {
//        if (player.getLevel().isClientSide()) {
//            BlockPreviewRenderer.getInstance().saveCurrentPreview();
//        }
//        var blockPosStates = getBlockPosStateForPlacing(player, hitResults);
//
//        for (var blockPosState : blockPosStates) {
//            if (!blockPosState.place()) continue;
//
//            var slot = InventoryHelper.findItemSlot(player.getInventory(), blockPosState.blockState().getBlock().asItem());
//            var swap = InventoryHelper.swapSlot(player.getInventory(), slot);
//            if (!swap) continue;
//
//            if (Constructor.getInstance().isReplace(player)) {
//                blockPosState.breakBy(player);
//            }
//
//            blockPosState.placeBy(player, InteractionHand.MAIN_HAND);
//            InventoryHelper.swapSlot(player.getInventory(), slot);
//        }
//    }
//
//    public static void destroyBlocks(Player player, List<BlockHitResult> hitResults) {
//        if (player.getLevel().isClientSide()) {
//            BlockPreviewRenderer.getInstance().saveCurrentPreview();
//        }
//        var blockPosStates = Constructor.getInstance().getBlockPosStateForBreaking(player, hitResults);
//
//        for (var blockPosState : blockPosStates) {
////            if (!blockPosState.place()) continue;
//            blockPosState.breakBy(player);
//        }
//    }
}
