package dev.huskcasaca.effortless.building.operation;

import dev.huskcasaca.effortless.building.Context;
import dev.huskcasaca.effortless.building.InventorySwapper;
import dev.huskcasaca.effortless.building.Storage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public final class SingleBlockPlaceOperation extends SingleBlockOperation {
    private final Level level;
    private final Player player;
    private final Context context;
    private final Storage storage;
    private final BlockPos blockPos;
    private final BlockState blockState;

    public SingleBlockPlaceOperation(
            Level level,
            Player player,
            Context context,
            Storage storage,
            BlockPos blockPos, // for preview
            BlockState blockState
    ) {
        this.level = level;
        this.player = player;
        this.context = context;
        this.storage = storage;
        this.blockPos = blockPos;
        this.blockState = blockState;
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
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
            }
        }
        var soundType = blockState2.getSoundType();
        level.playSound(player, blockPos, blockItem.getPlaceSound(blockState2), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player, blockState2));
        if (player == null || !player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean testPlace(Context context, Level level, Player player, BlockPos blockPos) {
        if (!canInteract(level, player, blockPos)) {
            return false;
        }
        if (context.replaceMode().isReplace()) {
            if (player.isCreative()) return true;
            return !level.getBlockState(blockPos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
        }
        return level.getBlockState(blockPos).canBeReplaced(); // fluid
    }

    public static InteractionResult placeBlock(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return InteractionResult.FAIL;
        }

        // TODO: 8/3/23
//        if (!canPlace(level, player, blockPos, blockState)) {
//            return InteractionResult.FAIL;
//        }
//        if ((blockState.isAir() || !level.getBlockState(blockPos).isAir()) && !canBreak(level, player, blockPos)) {
//            return InteractionResult.FAIL;
//        }

        if (player.getLevel().isClientSide()) {
            return placeBlockClient(level, player, interactionHand, blockPos, blockState);
        } else {
            return placeBlockServer(level, player, interactionHand, blockPos, blockState);
        }
    }

    private static InteractionResult placeBlockServer(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
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
            interactionResult = useBlockItemStackOn(blockStatePlaceContext);
            itemStack.setCount(i);
        } else {
            interactionResult = useBlockItemStackOn(blockStatePlaceContext);
        }

        if (interactionResult.consumesAction()) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack2);
        }

        return interactionResult;

    }

    private static InteractionResult placeBlockClient(Level level, Player localPlayer, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState) {
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
            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext);
            itemStack.setCount(i);
        } else {
//            interactionResult2 = itemStack.useOn(useOnContext);
            interactionResult2 = useBlockItemStackOn(blockStatePlaceContext);
        }

        return interactionResult2;
    }

    private static InteractionResult useBlockItemStackOn(BlockStatePlaceContext blockStatePlaceContext) {
        var itemStack = blockStatePlaceContext.getItemInHand();
        var player = blockStatePlaceContext.getPlayer();
        var blockPos = blockStatePlaceContext.getClickedPos();
        var blockInWorld = new BlockInWorld(blockStatePlaceContext.getLevel(), blockPos, false);
        if (player != null && !player.getAbilities().mayBuild && !itemStack.hasAdventureModePlaceTagForBlock(blockStatePlaceContext.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)) {
            return InteractionResult.PASS;
        }
        var item = itemStack.getItem();
        var interactionResult = useBlockItemOn((BlockItem) item, blockStatePlaceContext);
        if (player != null && interactionResult.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }

        return interactionResult;
    }

    @Override
    public Result perform() {
        if (storage != null) {
            return new Result(this, InteractionResult.SUCCESS, ItemStack.EMPTY, ItemStack.EMPTY);
        } else {
            var swapper = new InventorySwapper(player.getInventory(), blockState.getBlock().asItem());

            swapper.swapSelected();
            var result = placeBlock(level, player, InteractionHand.MAIN_HAND, blockPos, blockState);
            swapper.restoreSelected();

            return new Result(this, result, ItemStack.EMPTY, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack requiredItemStack() {
        return new ItemStack(blockState.getBlock().asItem());
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
        return blockState;
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

        //    public boolean canPlace() {
        //        var player = getPlayer();
        //        if (player == null) return false;
        //        return SurvivalHelper.canPlace(player.getLevel(), player, blockPos, placeState);
        //    }

    }
}