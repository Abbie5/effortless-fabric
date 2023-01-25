package dev.huskcasaca.effortless.buildmode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.UUID;

public abstract class MultipleClickBuildable implements Buildable {

    protected Dictionary<UUID, Integer> useCountTableClient = new Hashtable<>();
    protected Dictionary<UUID, Integer> useCountTableServer = new Hashtable<>();
    protected Dictionary<UUID, BlockHitResult> firstHitResultTable = new Hashtable<>();

    protected int getUseCount(Player player) {
        var useCountTable = player.getLevel().isClientSide() ? useCountTableClient : useCountTableServer;
        return useCountTable.get(player.getUUID());
    }

    protected int putUseCount(Player player, int count) {
        var useCountTable = player.getLevel().isClientSide() ? useCountTableClient : useCountTableServer;
        return useCountTable.put(player.getUUID(), count);
    }

    protected BlockHitResult getFirstHitResult(Player player) {
        return firstHitResultTable.get(player.getUUID());
    }

    protected BlockHitResult putFirstResult(Player player, BlockHitResult hitResult) {
        return firstHitResultTable.put(player.getUUID(), hitResult);
    }

    @Override
    public void initialize(Player player) {
        useCountTableClient.put(player.getUUID(), 0);
        useCountTableServer.put(player.getUUID(), 0);
        putFirstResult(player, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false));
    }

    @Override
    public Direction getHitSide(Player player) {
        return getFirstHitResult(player).getDirection();
    }

    @Override
    public Vec3 getHitVec(Player player) {
        return getFirstHitResult(player).getLocation();
    }

    public abstract static class AxisCriteria {
        protected final Vec3 center;
        protected final Vec3 eye;
        protected final Vec3 look;
        protected final Direction.Axis axis;

        public AxisCriteria(Vec3 center, Vec3 eye, Vec3 look, Direction.Axis axis) {
            this.center = center;
            this.eye = eye;
            this.look = look;
            this.axis = axis;
        }

        public Vec3 startVec() {
            return BuildModeHandler.getBound(center, eye, look);
        }

        public Vec3 planeVec() {
            return BuildModeHandler.findBound(center, eye, look, axis);
        }

        public Vec3 lineVec() {
            return planeVec();
        }

        public double distanceToEyeSqr() {
            return planeVec().subtract(eye).lengthSqr();
        }

        public double distanceToLineSqr() {
            return planeVec().subtract(lineVec()).lengthSqr();
        }

        public boolean isInRange(Player player, int reach, boolean skipRaytrace) {
            return BuildModeHandler.isCriteriaValid(eye, look, reach, player, skipRaytrace, lineVec(), planeVec(), distanceToEyeSqr());
        }

        public BlockPos tracePlane() {
            var offset = startVec().subtract(center);
            return new BlockPos(planeVec().subtract(axis == Direction.Axis.X ? offset.x : 0, axis == Direction.Axis.Y ? offset.y : 0, axis == Direction.Axis.Z ? offset.z : 0));
        }

        public BlockPos traceLine() {
            return new BlockPos(lineVec());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (AxisCriteria) obj;
            return Objects.equals(this.center, that.center) &&
                    Objects.equals(this.eye, that.eye) &&
                    Objects.equals(this.look, that.look) &&
                    Objects.equals(this.axis, that.axis);
        }

        @Override
        public int hashCode() {
            return Objects.hash(center, eye, look, axis);
        }

    }

}
