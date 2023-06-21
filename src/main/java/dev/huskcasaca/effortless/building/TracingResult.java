package dev.huskcasaca.effortless.building;

import net.minecraft.world.phys.BlockHitResult;

import java.util.Collections;
import java.util.List;

public record TracingResult(
        List<BlockHitResult> result,
        Type type
) {

    public static TracingResult success(List<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS);
    }

    public static TracingResult partial(List<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS_PARTIAL);
    }

    public static TracingResult pass() { // placing
        return new TracingResult(Collections.emptyList(), Type.PASS);
    }

    public static TracingResult fail() {
        return new TracingResult(Collections.emptyList(), Type.FAIL);
    }

//    public static TracingResult trace(Player player, HitResult hitResult, boolean skip, boolean use) {
//        var startPos = (BlockPos) null;
//        var hitSide = (Direction) null;
//        var hitVec = (Vec3) null;
//
//        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
//            startPos = blockHitResult.getBlockPos();
//            hitSide = blockHitResult.getDirection();
//            hitVec = hitResult.getLocation();
//
//            boolean quick = skip || EffortlessBuilder.getInstance().isQuickReplace(player);
//
//            boolean replaceable = player.level.getBlockState(startPos).canBeReplaced();
//
//            var blockStatePlaceContext = new BlockPlaceContext(player.level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), blockHitResult);
//
//            boolean replaceable1 = player.level.getBlockState(startPos).canBeReplaced(blockStatePlaceContext);
//
//            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, blockHitResult.getDirection());
//            if (!quick && !replaceable1 && !becomesDoubleSlab) {
//                startPos = startPos.relative(blockHitResult.getDirection());
//            }
//        }
//
//        if (EffortlessBuilder.getInstance().isActive(player)) {
//            var hitVec1 = EffortlessBuilder.getInstance().getBuildMode(player).getInstance().getHitVec(player);
//            var hitSide1 = EffortlessBuilder.getInstance().getBuildMode(player).getInstance().getHitSide(player);
//            if (hitSide1 != null) {
//                hitSide = hitSide1;
//            }
//            if (hitVec1 != null) {
//                hitVec = hitVec1;
//            }
//        }
//
//        if (hitSide == null) {
//            return TracingResult.missSide();
//        }
//
//        if (hitVec == null) {
//            return TracingResult.missVec();
//        }
//
//        new BlockHitResult().isInside()
//
//        var skipRaytrace = EffortlessBuilder.getInstance().isQuickReplace(player);
//        var offset = ((BlockHitResult) hitResult).withPosition(startPos);
//        var coordinates = use ? EffortlessBuilder.getBuildMode(player).getInstance().collect(player, offset, skipRaytrace) : EffortlessBuilder.getInstance().findCoordinates(player, offset, skipRaytrace);
//
//        if (coordinates.isEmpty()) {
//            return TracingResult.pass();
//        }
//
//        int limit = ReachHelper.getMaxBlockPlaceAtOnce(player);
//
//        if (coordinates.size() > limit) {
//            coordinates = coordinates.subList(0, limit);
//            // TODO: 21/1/23
//        }
//
////        hitVec = hitVec.subtract((int) hitVec.x, (int) hitVec.y, (int) hitVec.z).normalize();
//        var hitResult1 = new BlockHitResult(hitVec, hitSide, BlockPos.ZERO, false);
//        var hitResults = coordinates.stream().map(hitResult1::withPosition).toList();
//
//        return TracingResult.success(hitResults);
//    }

    public enum Type {
        SUCCESS,
        SUCCESS_PARTIAL,
        PASS,
        FAIL;

        public boolean isSuccess() {
            return this == SUCCESS || this == SUCCESS_PARTIAL;
        }
    }

}
