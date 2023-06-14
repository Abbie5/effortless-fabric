package dev.huskcasaca.effortless.building.mode.builder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class MultipleClickBuilder implements Builder {

    public abstract static class AxisCriteria {
        protected static final double LOOK_VEC_TOLERANCE = 0.001;
        protected final Entity entity;
        protected final Vec3 center;
        protected final Vec3 eye;
        protected final Vec3 look;
        protected final int reach;
        protected final boolean skipRaytrace;
        protected final Axis axis;

        public AxisCriteria(Axis axis, Entity entity, Vec3 center, int reach, boolean skipRaytrace) {
            this.axis = axis;
            this.entity = entity;
            this.look = getPlayerLookVec(entity);
            this.eye = entity.getEyePosition();
            this.center = center;
            this.reach = reach;
            this.skipRaytrace = skipRaytrace;
        }

        // FIXME: 25/1/23
        //Use this instead of player.getLookVec() in any buildmodes code
        protected static Vec3 getPlayerLookVec(Entity entity) {
            Vec3 lookVec = entity.getLookAngle();
            double x = lookVec.x;
            double y = lookVec.y;
            double z = lookVec.z;

            if (Math.abs(x) < LOOK_VEC_TOLERANCE) x = LOOK_VEC_TOLERANCE;
            if (Math.abs(x - 1.0) < LOOK_VEC_TOLERANCE) x = 1 - LOOK_VEC_TOLERANCE;
            if (Math.abs(x + 1.0) < LOOK_VEC_TOLERANCE) x = LOOK_VEC_TOLERANCE - 1;

            if (Math.abs(y) < LOOK_VEC_TOLERANCE) y = LOOK_VEC_TOLERANCE;
            if (Math.abs(y - 1.0) < LOOK_VEC_TOLERANCE) y = 1 - LOOK_VEC_TOLERANCE;
            if (Math.abs(y + 1.0) < LOOK_VEC_TOLERANCE) y = LOOK_VEC_TOLERANCE - 1;

            if (Math.abs(z) < LOOK_VEC_TOLERANCE) z = LOOK_VEC_TOLERANCE;
            if (Math.abs(z - 1.0) < LOOK_VEC_TOLERANCE) z = 1 - LOOK_VEC_TOLERANCE;
            if (Math.abs(z + 1.0) < LOOK_VEC_TOLERANCE) z = LOOK_VEC_TOLERANCE - 1;

            return new Vec3(x, y, z).normalize();
        }

        protected static Vec3 getBound(Vec3 start, Vec3 eye, Vec3 look) {
            return new Vec3(
                    Math.round(getAxisBound(start.x, eye.x, look.x)),
                    Math.round(getAxisBound(start.y, eye.y, look.y)),
                    Math.round(getAxisBound(start.z, eye.z, look.z))
            );
        }

        protected static double getAxisBound(double start, double eye, double look) {
            if (eye >= start + 0.5) {
                return start + 0.5;
            }
            if (eye <= start - 0.5) {
                return start - 0.5;
            }
            if (look > 0) {
                return start + 0.5;
            }
            if (look < 0) {
                return start - 0.5;
            }
            return start;
        }

        //Find coordinates on a line bound by a plane
        protected static Vec3 findBound(Vec3 start, Vec3 eye, Vec3 look, Direction.Axis axis) {
            return switch (axis) {
                case X -> findXBound(start, eye, look);
                case Y -> findYBound(start, eye, look);
                case Z -> findZBound(start, eye, look);
            };
        }

        //Find coordinates on a line bound by a plane
        protected static Vec3 findXBound(Vec3 start, Vec3 eye, Vec3 look) {
            var bound = getBound(start, eye, look);
            //then y and z are
            double y = (bound.x - eye.x) / look.x * look.y + eye.y;
            double z = (bound.x - eye.x) / look.x * look.z + eye.z;

            return new Vec3(bound.x, y, z);
        }

        protected static Vec3 findYBound(Vec3 start, Vec3 eye, Vec3 look) {
            var bound = getBound(start, eye, look);
            //then x and z are
            double x = (bound.y - eye.y) / look.y * look.x + eye.x;
            double z = (bound.y - eye.y) / look.y * look.z + eye.z;

            return new Vec3(x, bound.y, z);
        }

        protected static Vec3 findZBound(Vec3 start, Vec3 eye, Vec3 look) {
            var bound = getBound(start, eye, look);
            //then x and y are
            double x = (bound.z - eye.z) / look.z * look.x + eye.x;
            double y = (bound.z - eye.z) / look.z * look.y + eye.y;

            return new Vec3(x, y, bound.z);
        }

        protected static boolean isCriteriaValid(Vec3 start, Vec3 look, int reach, Entity entity, boolean skipRaytrace, Vec3 lineBound, Vec3 planeBound, double distToPlayerSq) {
            boolean intersects = false;
            if (!skipRaytrace) {
                //collision within a 1 block radius to selected is fine
                var rayTraceContext = new ClipContext(start, lineBound, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity);
                var rayTraceResult = entity.level.clip(rayTraceContext);
                intersects = rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK && planeBound.subtract(rayTraceResult.getLocation()).lengthSqr() > 4;
            }

            return planeBound.subtract(start).dot(look) > 0 &&
                    distToPlayerSq > 2 && distToPlayerSq < reach * reach &&
                    !intersects;
        }

        public Vec3 startVec() {
            return getBound(center, eye, look);
        }

        public Vec3 planeVec() {
            return findBound(center, eye, look, axis);
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

        public boolean isInRange() {
            return isCriteriaValid(eye, look, reach, entity, skipRaytrace, lineVec(), planeVec(), distanceToEyeSqr());
        }

        public BlockHitResult tracePlane() {
            var offset = startVec().subtract(center);
            var found = new BlockPos(planeVec().subtract(axis == Axis.X ? offset.x : 0, axis == Axis.Y ? offset.y : 0, axis == Axis.Z ? offset.z : 0));
            return convert(found);
        }

        public BlockHitResult traceLine() {
            var found = new BlockPos(lineVec());
            return convert(found);
        }

        protected BlockHitResult convert(BlockPos blockPos) {
            var look = entity.getLookAngle();
            var vec3 = entity.getEyePosition().add(look.scale(0.001));
            return new BlockHitResult(vec3, Direction.getNearest(look.x, look.y, look.z).getOpposite(), blockPos, true);
        }

    }

}
