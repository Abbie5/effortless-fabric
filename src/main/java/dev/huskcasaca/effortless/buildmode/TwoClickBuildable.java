package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.building.ReachHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public abstract class TwoClickBuildable extends MultipleClickBuildable {

    @Override
    public List<BlockPos> trace(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        var blockPos = hitResult.getBlockPos();

        List<BlockPos> list = new ArrayList<>();

        int useCount = getUseCount(player);

        useCount++;
        putUseCount(player, useCount);

        if (useCount == 1) {
            //If clicking in air, reset and try again
            if (blockPos == null) {
                putUseCount(player, 0);
                return list;
            }

            //First click, remember starting position
            putFirstResult(player, hitResult);
            //Keep list empty, dont place any blocks yet
        } else {
            //Second click, place blocks
            list = preview(player, hitResult, skipRaytrace);
            putUseCount(player, 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> preview(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        int useCount = getUseCount(player);
        var firstPos = getFirstHitResult(player).getBlockPos();

        if (useCount == 0) {
            if (hitResult != null)
                list.add(hitResult.getBlockPos());
        } else {
            var secondPos = findSecondPos(player, firstPos, skipRaytrace);
            if (secondPos == null) return list;

            //Limit amount of blocks we can place per row
            int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            list.addAll(getFinalBlocks(player, x1, y1, z1, x2, y2, z2));
        }

        return list;
    }

    //Finds the place of the second block pos based on criteria (floor must be on same height as first click, wall on same plane etc)
    protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

    //After first and second pos are known, we want all the blocks
    public abstract List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);

}
