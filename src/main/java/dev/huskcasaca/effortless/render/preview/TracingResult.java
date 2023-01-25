package dev.huskcasaca.effortless.render.preview;

import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.utils.SurvivalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public record TracingResult(
        List<BlockHitResult> result,
        Type type
) {

    public static TracingResult success(List<BlockHitResult> result) {
        return new TracingResult(result, Type.SUCCESS);
    }

    public static TracingResult fail() {
        return new TracingResult(Collections.emptyList(), Type.FAIL);
    }

    public static TracingResult missSide() {
        return new TracingResult(Collections.emptyList(), Type.MISS_DIRECTION);
    }

    public static TracingResult missVec() {
        return new TracingResult(Collections.emptyList(), Type.MISS_VECTOR);
    }

    public static TracingResult pass() { // placing
        return new TracingResult(Collections.emptyList(), Type.PASS);
    }

    public static TracingResult trace(Player player, HitResult hitResult, boolean skip, boolean use) {
        var startPos = (BlockPos) null;
        var hitSide = (Direction) null;
        var hitVec = (Vec3) null;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
            startPos = blockHitResult.getBlockPos();
            hitSide = blockHitResult.getDirection();
            hitVec = hitResult.getLocation();

            boolean quick = skip || BuildModifierHelper.isQuickReplace(player);

            boolean replaceable = player.level.getBlockState(startPos).canBeReplaced();

            var blockStatePlaceContext = new BlockPlaceContext(player.level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), blockHitResult);

            boolean replaceable1 = player.level.getBlockState(startPos).canBeReplaced(blockStatePlaceContext);

            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, blockHitResult.getDirection());
            if (!quick && !replaceable1 && !becomesDoubleSlab) {
                startPos = startPos.relative(blockHitResult.getDirection());
            }
        }

        if (BuildModeHandler.isActive(player)) {
            var hitVec1 = BuildModeHelper.getBuildMode(player).getInstance().getHitVec(player);
            var hitSide1 = BuildModeHelper.getBuildMode(player).getInstance().getHitSide(player);
            if (hitSide1 != null) {
                hitSide = hitSide1;
            }
            if (hitVec1 != null) {
                hitVec = hitVec1;
            }
        }

        if (hitSide == null) {
            return TracingResult.missSide();
        }

        if (hitVec == null) {
            return TracingResult.missVec();
        }

        var breaking = BuildModeHandler.isCurrentlyBreaking(player);

        var skipRaytrace = true || BuildModifierHelper.isQuickReplace(player);
        var offset = ((BlockHitResult) hitResult).withPosition(startPos);
        var coordinates = use ? BuildModeHelper.getBuildMode(player).getInstance().trace(player, offset, skipRaytrace) : BuildModeHandler.findCoordinates(player, offset, skipRaytrace);

        if (coordinates.isEmpty()) {
            return TracingResult.pass();
        }

        int limit = ReachHelper.getMaxBlockPlaceAtOnce(player);

        if (coordinates.size() > limit) {
            coordinates = coordinates.subList(0, limit);
            // TODO: 21/1/23
        }

//        hitVec = hitVec.subtract((int) hitVec.x, (int) hitVec.y, (int) hitVec.z).normalize();
        var hitResult1 = new BlockHitResult(hitVec, hitSide, BlockPos.ZERO, false);
        var hitResults = coordinates.stream().map(hitResult1::withPosition).toList();

        return TracingResult.success(hitResults);
    }

    public enum Type {
        SUCCESS,
        MISS_DIRECTION,
        MISS_VECTOR,
        PASS,
        FAIL
    }

}
