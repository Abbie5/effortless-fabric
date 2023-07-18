package dev.effortless.building.operation;

import dev.effortless.building.Context;
import dev.effortless.building.InventorySwapper;
import dev.effortless.building.Storage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class BlockPlaceOperation extends BlockOperation {
    private final Level level;
    private final Player player;
    private final Context context;
    private final Storage storage;
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final Direction direction;

    public BlockPlaceOperation(
            Level level,
            Player player,
            Context context,
            Storage storage,
            BlockPos blockPos, // for preview
            Direction direction,
            BlockState blockState
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
        this.blockPos = blockPos;
        this.direction = direction;
        this.blockState = blockState;
    }

    private static BlockInteractionResult useBlockItemOn(BlockItem blockItem, BlockStatePlaceContext blockStatePlaceContext, boolean preview) {

        if (!blockItem.getBlock().isEnabled(blockStatePlaceContext.getLevel().enabledFeatures())) {
            return BlockInteractionResult.FAIL_LEVEL_FEATURE_LIMIT;
        }
        if (!blockStatePlaceContext.canPlace()) {
            return BlockInteractionResult.FAIL_BLOCK_STATE_FLAG_CANNOT_REPLACE;
        }
//        var blockStatePlaceContext = blockItem.updatePlacementContext(blockPlaceContext);

        var blockState = blockStatePlaceContext.getPlaceState();
        if (blockState == null) {
            return BlockInteractionResult.FAIL_BLOCK_STATE_NULL;
        }
        if (!preview && !blockItem.placeBlock(blockStatePlaceContext, blockState)) {
            return BlockInteractionResult.FAIL_INTERNAL_SET_BLOCK;
        }

        var blockPos = blockStatePlaceContext.getClickedPos();
        var level = blockStatePlaceContext.getLevel();
        var player = blockStatePlaceContext.getPlayer();
        var itemStack = blockStatePlaceContext.getItemInHand();
        if (preview) {
            return BlockInteractionResult.SUCCESS_PREVIEW;
        }
        var blockState2 = level.getBlockState(blockPos);
        if (blockState2.is(blockState.getBlock())) {
            blockState2 = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState2);
            blockItem.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState2);
            blockState2.getBlock().setPlacedBy(level, blockPos, blockState2, player, itemStack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
            }
        }
        var soundType = blockState2.getSoundType();
        level.playSound(player, blockPos, blockItem.getPlaceSound(blockState2), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player, blockState2));
        if (player == null || !player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return BlockInteractionResult.sidedSuccess(level.isClientSide());

    }


    private static BlockInteractionResult useBlockItemStackOn(BlockStatePlaceContext blockStatePlaceContext, boolean preview) {
        var itemStack = blockStatePlaceContext.getItemInHand();
        var player = blockStatePlaceContext.getPlayer();
        var blockPos = blockStatePlaceContext.getClickedPos();
        var blockInWorld = new BlockInWorld(blockStatePlaceContext.getLevel(), blockPos, false);
        if (player == null) {
            return BlockInteractionResult.FAIL_PLAYER_NULL;
        }
        if (!player.getAbilities().mayBuild && !itemStack.hasAdventureModePlaceTagForBlock(blockStatePlaceContext.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)) {
            return BlockInteractionResult.FAIL_PLAYER_ABILITY_CANNOT_BUILD;
        }
//        if (!itemStack.hasAdventureModePlaceTagForBlock(blockStatePlaceContext.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)) {
//            return BlockInteractionResult.PASS_LEVEL_ADVENTURE_LIMIT;
//        }
        var item = itemStack.getItem();
        var interactionResult = useBlockItemOn((BlockItem) item, blockStatePlaceContext, preview);
        if (!preview && interactionResult.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return interactionResult;
    }

    private static boolean testReplace(Context context, Level level, Player player, BlockPos blockPos) {

        if (context.replaceMode().isReplace()) {
            if (player.isCreative()) return true;
            return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
        }
        return level.getBlockState(blockPos).canBeReplaced(); // fluid
    }

    private static BlockInteractionResult placeBlockServer(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState, boolean preview) {
        var itemStack = player.getItemInHand(interactionHand);
        var fakeResult = new BlockHitResult(Vec3.ZERO, Direction.UP, blockPos, false);

        var blockStateInWorld = level.getBlockState(blockPos);
        if (!blockStateInWorld.getBlock().isEnabled(level.enabledFeatures())) {
            return BlockInteractionResult.FAIL_LEVEL_FEATURE_LIMIT;
        }
        var itemStack2 = itemStack.copy();

        var blockStatePlaceContext = new BlockStatePlaceContext(level, player, interactionHand, itemStack, fakeResult, blockState);
        var interactionResult = BlockInteractionResult.SUCCESS;
        if (player.isCreative()) {
            int i = itemStack.getCount();
            interactionResult = useBlockItemStackOn(blockStatePlaceContext, preview);
            itemStack.setCount(i);
        } else {
            interactionResult = useBlockItemStackOn(blockStatePlaceContext, preview);
        }

        if (interactionResult.consumesAction()) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack2);
        }

        return interactionResult;
    }

    // TODO: 15/7/23 use vanilla
    private static BlockInteractionResult placeBlockClient(Level level, Player localPlayer, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState, boolean preview) {
        var itemStack = localPlayer.getItemInHand(interactionHand);
        var blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, blockPos, false);

        var blockStatePlaceContext = new BlockStatePlaceContext(level, localPlayer, interactionHand, itemStack, blockHitResult, blockState); // new UseOnContext(localPlayer, interactionHand, blockHitResult);
        var interactionResult2 = BlockInteractionResult.SUCCESS;
        if (localPlayer.isCreative()) {
            int i = itemStack.getCount();
            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext, preview); // itemStack.useOn(useOnContext);
            itemStack.setCount(i);
        } else {
            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext, preview); // itemStack.useOn(useOnContext);
        }

        return interactionResult2;
    }

    // no context
    public static BlockInteractionResult placeBlock(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState, boolean preview) {
        if (player.isSpectator()) { // move
            return BlockInteractionResult.FAIL_PLAYER_SPECTATOR;
        }
        var itemStack = player.getItemInHand(interactionHand);

        if (itemStack.isEmpty()) {
            return BlockInteractionResult.FAIL_PLAYER_EMPTY_INV;
        }

        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return BlockInteractionResult.FAIL_PLAYER_ITEM_NOT_BLOCK;
        }

        if (player.getLevel().isClientSide()) {
            return placeBlockClient(level, player, interactionHand, blockPos, blockState, preview);
        } else {
            return placeBlockServer(level, player, interactionHand, blockPos, blockState, false);
        }
    }

    @Override
    public BlockResult perform() {
        var inputs = Collections.singletonList(blockState.getBlock().asItem().getDefaultInstance());
        var outputs = Collections.<ItemStack>emptyList();

        var result = BlockInteractionResult.SUCCESS;
        var interactionHand = InteractionHand.MAIN_HAND;
        var preview = storage != null;

        if (testReplace(context, level, player, blockPos)) {
            if (preview) {
                var old = player.getItemInHand(interactionHand);
                player.setItemInHand(interactionHand, storage.findByItem(blockState.getBlock().asItem()).orElse(ItemStack.EMPTY));
                result = placeBlock(level, player, interactionHand, blockPos, blockState, true);
                player.setItemInHand(interactionHand, old);
            } else {
                var swapper = new InventorySwapper(player.getInventory(), blockState.getBlock().asItem());
                swapper.swapSelected();
                result = placeBlock(level, player, interactionHand, blockPos, blockState, false);
                swapper.restoreSelected();
            }
        } else {
            result = BlockInteractionResult.FAIL_BLOCK_STATE_FLAG_CANNOT_REPLACE;
        }

        return new BlockResult(this, result, inputs, outputs);
    }

    @Override
    public ItemStack inputItemStack() {
        return new ItemStack(blockState.getBlock().asItem());
    }

    @Override
    public ItemStack outputItemStack() {
        return ItemStack.EMPTY;
    }

    // block placement
    @Override
    public BlockPos getPosition() {
        return blockPos;
    }

    @Override
    public OperationType getType() {
        return OperationType.WORLD_PLACE_OP;
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
        return blockState;
    }

    @Override
    public Direction direction() {
        return direction;
    }

    @Override
    public boolean isPreview() {
        return storage() != null;
    }

    private static class BlockStatePlaceContext extends BlockPlaceContext {

        private final BlockState placeState;
        private final BlockPos blockPos;

        public BlockStatePlaceContext(Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, BlockState blockState) {
            this(player.level, player, interactionHand, player.getItemInHand(interactionHand), blockHitResult, blockState);
        }

        public BlockStatePlaceContext(Level level, @Nullable Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult, BlockState blockState) {
            super(level, player, interactionHand, itemStack, blockHitResult);
            this.placeState = blockState;
            this.blockPos = blockHitResult.getBlockPos();
        }

        public BlockState getPlaceState() {
            return placeState;
        }

    }
}