package dev.huskcasaca.effortless.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockStatePlaceContext extends BlockPlaceContext {

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
