package dev.huskcasaca.effortless.building.mode.builder.oneclick;

import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.mode.builder.SingleClickBuilder;
import dev.huskcasaca.effortless.utils.ClipUtils;
import dev.huskcasaca.effortless.utils.SurvivalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public class Single extends SingleClickBuilder {

    public static BlockHitResult traceSingle(Player player, BuildContext context) {
        return transformCurrentHit(player, context);
    }

    public static BlockHitResult transformCurrentHit(Player player, BuildContext context) {
        var hitResult = ClipUtils.clipInRange(player, context.maxReachDistance());
        var startPos = hitResult.getBlockPos();

        boolean quick = context.isSkipTracing();
//        boolean replaceable = player.level.getBlockState(startPos).canBeReplaced();

        // get item from build context
        var blockStatePlaceContext = new BlockPlaceContext(player.level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hitResult);
        boolean canPlace = blockStatePlaceContext.canPlace();

        boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, hitResult.getDirection());
        if (quick || canPlace || becomesDoubleSlab) {
            startPos = startPos;
        } else {
            startPos = startPos.relative(hitResult.getDirection());
        }
        return hitResult.withPosition(startPos);
    }

    public static Stream<BlockPos> collectSingleBlocks(BuildContext context) {
        return Stream.of(context.firstPos());
    }

    @Override
    protected BlockHitResult traceFirstHit(Player player, BuildContext context) {
        return traceSingle(player, context);
    }

    @Override
    protected Stream<BlockPos> collectFinalBlocks(BuildContext context) {
        return collectSingleBlocks(context);
    }

}
