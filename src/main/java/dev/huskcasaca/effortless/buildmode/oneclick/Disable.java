package dev.huskcasaca.effortless.buildmode.oneclick;

import dev.huskcasaca.effortless.buildmode.SingleClickBuildable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Disable extends SingleClickBuildable {

    @Override
    public void initialize(Player player) {

    }

    @Override
    public List<BlockPos> trace(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        var blockPos = hitResult.getBlockPos();
        if (blockPos == null) return Collections.emptyList();
        return getFinalBlocks(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public List<BlockPos> preview(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        if (hitResult == null) return Collections.emptyList();
        return getFinalBlocks(player, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ());
    }

    @Override
    public Direction getHitSide(Player player) {
        return null;
    }

    @Override
    public Vec3 getHitVec(Player player) {
        return null;
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1) {
        List<BlockPos> list = new ArrayList<>();
        list.add(new BlockPos(x1, y1, z1));
        return list;
    }
}
