package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.entity.player.EffortlessDataProvider;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ClientboundPlayerBuildModePacket;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerBreakBlockPacket;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerPlaceBlockPacket;
import dev.huskcasaca.effortless.render.preview.TracingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class BuildModeHandler {

    private static final double LOOK_VEC_TOLERANCE = 0.0001;
    private static final Dictionary<Player, Boolean> currentlyBreakingClient = new Hashtable<>();
    private static final Dictionary<Player, Boolean> currentlyBreakingServer = new Hashtable<>();

    public static void onBlockPlacedPacketReceived(Player player, ServerboundPlayerPlaceBlockPacket packet) {
        if (isCurrentlyBreaking(player)) {
            reset(player);
            return;
        }
        var tracingResult = TracingResult.trace(player, packet.blockHitResult(), false, true);

        switch (tracingResult.type()) {
            case SUCCESS -> {
                BuildModifierHandler.placeBlocks(player, tracingResult.result());
                resetBreakingPlacing(player);
            }
            case PASS -> {
                setCurrentlyPlacing(player);
            }
            case MISS_DIRECTION, MISS_VECTOR, FAIL -> {
            }
        }
    }

    public static void onBlockBrokenPacketReceived(Player player, ServerboundPlayerBreakBlockPacket packet) {
        if (isCurrentlyPlacing(player)) {
            reset(player);
            return;
        }
        var tracingResult = TracingResult.trace(player, packet.blockHitResult(), true, true);

        switch (tracingResult.type()) {
            case SUCCESS -> {
                BuildModifierHandler.destroyBlocks(player, tracingResult.result());
                resetBreakingPlacing(player);
            }
            case PASS -> {
                setCurrentlyBreaking(player);
            }
            case MISS_DIRECTION, MISS_VECTOR, FAIL -> {
            }
        }
    }

    public static List<BlockPos> findCoordinates(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
        List<BlockPos> coordinates = new ArrayList<>();

        var modeSettings = BuildModeHelper.getModeSettings(player);
        coordinates.addAll(modeSettings.buildMode().getInstance().preview(player, hitResult, skipRaytrace));

        return coordinates;
    }

    public static void reset(Player player) {
        if (player == null) {
            return;
        }
        resetBreakingPlacing(player);

        BuildModeHelper.getModeSettings(player).buildMode().getInstance().initialize(player);
    }

    private static Dictionary<Player, Boolean> getCurrentlyBreakingDict(Player player) {
        return player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
    }

    public static boolean isCurrentlyPlacing(Player player) {
        return Boolean.FALSE.equals(getCurrentlyBreakingDict(player).get(player));
    }

    public static boolean isCurrentlyBreaking(Player player) {
        return Boolean.TRUE.equals(getCurrentlyBreakingDict(player).get(player));
    }

    public static void setCurrentlyPlacing(Player player) {
        getCurrentlyBreakingDict(player).put(player, false);
    }

    public static void setCurrentlyBreaking(Player player) {
        getCurrentlyBreakingDict(player).put(player, true);
    }

    public static void resetBreakingPlacing(Player player) {
        getCurrentlyBreakingDict(player).remove(player);
    }

    //Either placing or breaking
    public static boolean isActive(Player player) {
        return getCurrentlyBreakingDict(player).get(player) != null;
    }

    public static Vec3 getBound(Vec3 start, Vec3 eye, Vec3 look) {
        return new Vec3(
                Math.round(getAxisBound(start.x, eye.x, look.x)),
                Math.round(getAxisBound(start.y, eye.y, look.y)),
                Math.round(getAxisBound(start.z, eye.z, look.z))
        );
    }

    public static double getAxisBound(double start, double eye, double look) {
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
    public static Vec3 findBound(Vec3 start, Vec3 eye, Vec3 look, Direction.Axis axis) {
        return switch (axis) {
            case X -> findXBound(start, eye, look);
            case Y -> findYBound(start, eye, look);
            case Z -> findZBound(start, eye, look);
        };
    }

    //Find coordinates on a line bound by a plane
    public static Vec3 findXBound(Vec3 start, Vec3 eye, Vec3 look) {
        var bound = getBound(start, eye, look);
        //then y and z are
        double y = (bound.x - eye.x) / look.x * look.y + eye.y;
        double z = (bound.x - eye.x) / look.x * look.z + eye.z;

        return new Vec3(bound.x, y, z);
    }

    public static Vec3 findYBound(Vec3 start, Vec3 eye, Vec3 look) {
        var bound = getBound(start, eye, look);
        //then x and z are
        double x = (bound.y - eye.y) / look.y * look.x + eye.x;
        double z = (bound.y - eye.y) / look.y * look.z + eye.z;

        return new Vec3(x, bound.y, z);
    }

    public static Vec3 findZBound(Vec3 start, Vec3 eye, Vec3 look) {
        var bound = getBound(start, eye, look);
        //then x and y are
        double x = (bound.z - eye.z) / look.z * look.x + eye.x;
        double y = (bound.z - eye.z) / look.z * look.y + eye.y;

        return new Vec3(x, y, bound.z);
    }

    // FIXME: 25/1/23 
    //Use this instead of player.getLookVec() in any buildmodes code
    public static Vec3 getPlayerLookVec(Player player) {
        Vec3 lookVec = player.getLookAngle();
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

    public static boolean isCriteriaValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace, Vec3 lineBound, Vec3 planeBound, double distToPlayerSq) {
        boolean intersects = false;
        if (!skipRaytrace) {
            //collision within a 1 block radius to selected is fine
            var rayTraceContext = new ClipContext(start, lineBound, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            var rayTraceResult = player.level.clip(rayTraceContext);
            intersects = rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK && planeBound.subtract(rayTraceResult.getLocation()).lengthSqr() > 4;
        }

        return planeBound.subtract(start).dot(look) > 0 &&
                distToPlayerSq > 2 && distToPlayerSq < reach * reach &&
                !intersects;
    }

    public static void handleNewPlayer(ServerPlayer player) {
        //Makes sure player has mode settings (if it doesnt it will create it)
        Packets.sendToClient(new ClientboundPlayerBuildModePacket(((EffortlessDataProvider) player).getModeSettings()), player);
    }
}

