package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.building.operation.BlockStatePlaceOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.stream.Stream;

public class StructurePlaceOperation {
    private final Level level;
    private final Player player;
    private final BuildContext context;

    public StructurePlaceOperation(
            Level level,
            Player player,
            BuildContext context
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
    }

    public static Stream<BlockStatePlaceOperation> create(Level level, Player player, BuildContext context) {
        var state = context.state();
        var filter = context.getBlockFilter(level, player);

        return context.collect()
                .result()
                .map((hitResult) -> new BlockStatePlaceOperation(
                        level,
                        hitResult.getBlockPos(),
                        getBlockStateFromMainHand(player, hitResult)))
                .flatMap(Stream::of) // for modifiers
                .filter(filter)
                .map((op) -> op);
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

    public void perform() {
        create(level, player, context).forEach((op) -> op.perform(player));
    }

    public StructurePreview preview() {
        var storage = new TempItemStorage(player.getInventory().items);
        var op2Result = new ArrayList<OperationResult>();

        for (var operation : create(level, player, context).toList()) {
            op2Result.add(new OperationResult(operation, operation.preview(player, storage)));
        }

        return new StructurePreview(op2Result);
    }

    public Level level() {
        return level;
    }

    public Player player() {
        return player;
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
