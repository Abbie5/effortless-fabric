package dev.huskcasaca.effortless.buildmode.twoclick;

import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.TwoClickBuildable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.Direction.Axis;

public class Floor extends TwoClickBuildable {

    public static BlockPos findFloor(Player player, BlockPos firstPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var eye = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        var criteriaList = new ArrayList<Criteria>(3);

        criteriaList.add(new Criteria(firstPos.getCenter(), eye, look, Axis.Y));

        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isInRange(player, reach, skipRaytrace));

        if (criteriaList.isEmpty()) return null;

        AxisCriteria selected = criteriaList.get(0);

        return selected.tracePlane();
    }

    public static List<BlockPos> getFloorBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        List<BlockPos> list = new ArrayList<>();

        if (BuildActionHandler.getPlaneFilling() == BuildAction.PLANE_FULL)
            addFloorBlocks(list, x1, x2, y1, z1, z2);
        else
            addHollowFloorBlocks(list, x1, x2, y1, z1, z2);

        return list;
    }

    public static void addFloorBlocks(List<BlockPos> list, int x1, int x2, int y, int z1, int z2) {

        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                list.add(new BlockPos(l, y, n));
            }
        }
    }

    public static void addHollowFloorBlocks(List<BlockPos> list, int x1, int x2, int y, int z1, int z2) {
        Line.addXLineBlocks(list, x1, x2, y, z1);
        Line.addXLineBlocks(list, x1, x2, y, z2);
        Line.addZLineBlocks(list, z1, z2, x1, y);
        Line.addZLineBlocks(list, z1, z2, x2, y);
    }

    @Override
    protected BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace) {
        return findFloor(player, firstPos, skipRaytrace);
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return getFloorBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    public static class Criteria extends AxisCriteria {

        public Criteria(Vec3 center, Vec3 eye, Vec3 look, Axis axis) {
            super(center, eye, look, axis);
        }

    }

}
