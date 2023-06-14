package dev.huskcasaca.effortless.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ClipUtils {

    //    float raytraceRange = ReachHelper.getPlacementReach(player) * 4;
    public static BlockHitResult clipInRange(Player player, int range) {
        var look = player.getLookAngle();
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        var end = new Vec3(player.getX() + look.x * range, player.getY() + player.getEyeHeight() + look.y * range, player.getZ() + look.z * range);
        return player.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    public static BlockHitResult clipOverride(Player player, BlockPos blockPos) {
        var look = player.getLookAngle();
        var vec3 = player.getEyePosition().add(look.scale(0.001));
        return new BlockHitResult(vec3, Direction.getNearest(look.x, look.y, look.z).getOpposite(), blockPos, true);
    }

}
