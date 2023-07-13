package dev.effortless.building.mode.builder.oneclick;

import dev.effortless.building.Context;
import dev.effortless.building.mode.builder.SingleClickBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public class Single extends SingleClickBuilder {

    public static BlockHitResult traceSingle(Player player, Context context) {
        return transformCurrentHit(player, context);
    }

    //    float raytraceRange = ReachHelper.getPlacementReach(player) * 4;
    public static BlockHitResult clipInRange(Player player, int range) {
        var look = getEntityLookAngleGap(player);
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        var end = new Vec3(player.getX() + look.x * range, player.getY() + player.getEyeHeight() + look.y * range, player.getZ() + look.z * range);
        return player.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    public static BlockHitResult clipOverride(Player player, BlockPos blockPos) {
        var look = getEntityLookAngleGap(player);
        var vec3 = player.getEyePosition().add(look.scale(0.001));
        return new BlockHitResult(vec3, Direction.getNearest(look.x, look.y, look.z).getOpposite(), blockPos, true);
    }

    public static boolean checkDoubleSlab(Player player, BlockPos pos, Direction facing) {
//        var placedBlockState = player.level.getBlockState(pos);
//
//        var itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
//        if (CompatHelper.isItemBlockProxy(itemstack))
//            itemstack = CompatHelper.getItemBlockFromStack(itemstack);
//
//        if (itemstack.isEmpty() || !(itemstack.getItem() instanceof BlockItem) || !(((BlockItem) itemstack.getItem()).getBlock() instanceof SlabBlock heldSlab))
//            return false;

        return false;
    }


    public static BlockHitResult transformCurrentHit(Player player, Context context) {
        var hitResult = clipInRange(player, context.maxReachDistance());
        var startPos = hitResult.getBlockPos();

        var skipTracing = context.isSkipTracing();

        var replaceable = player.getLevel().getBlockState(startPos).canBeReplaced();

        var becomesDoubleSlab = checkDoubleSlab(player, startPos, hitResult.getDirection());

        var tracingRelative = !skipTracing && !replaceable && !becomesDoubleSlab;

        if (tracingRelative) {
            startPos = startPos.relative(hitResult.getDirection());
        }
        return hitResult.withPosition(startPos);
    }

    public static Stream<BlockPos> collectSingleBlocks(Context context) {
        return Stream.of(context.firstPos());
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, Context context) {
        return traceSingle(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(Context context) {
        return collectSingleBlocks(context);
    }

}
