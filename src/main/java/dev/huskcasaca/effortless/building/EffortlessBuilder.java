package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.mode.BuildFeature;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.building.operation.Operation;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.huskcasaca.effortless.render.preview.OperationPreviewRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class EffortlessBuilder {

    private static final EffortlessBuilder INSTANCE = new EffortlessBuilder();
    private final ContextProvider provider = new ContextProvider();
    private Operation.Result<?> lastResult;

    public static EffortlessBuilder getInstance() {
        return INSTANCE;
    }

    public Context getContext(Player player) {
        return provider.get(player);
    }

    public Operation.Result<?> getLastResult() {
        return lastResult;
    }

    public void tick() {
        provider.tick();

        var player = Minecraft.getInstance().player;
        if (player == null) {
            lastResult = null;
            return;
        }
        if (getContext(player).isDisabled()) return;

        var context = getContext(player).withPlacingState().withNextHit(player, true);
        var storage = Storage.createTemp(player.getInventory().items);
        var operation = context.getStructure(player.getLevel(), player, storage);
        var result = operation.perform();
        lastResult = result;
        OperationPreviewRenderer.getInstance().putOpResult(result);
    }

    // from settings screen
    public void setBuildMode(Player player, BuildMode buildMode) {
        updateContext(player, context -> context.withUUID().withBuildMode(buildMode).withEmptyHits());
    }

    public void setBuildFeature(Player player, BuildFeature.Entry feature) {
        updateContext(player, context -> context.withUUID().withBuildFeature(feature).withEmptyHits());
    }

    private BuildingResult updateContext(Player player, Function<Context, Context> updater) {
        var context = provider.get(player);
        var updated = updater.apply(context);
        if (updated.isFulfilled()) {
            Packets.channel().sendToServer(new ServerboundPlayerBuildPacket(updated));
            provider.set(player, updated.reset());
            return BuildingResult.COMPLETED;
        } else {
            provider.set(player, updated);
            if (updated.isIdle()) {
                return BuildingResult.CANCELED;
            } else {
                return BuildingResult.PARTIAL;
            }
        }
    }

    // TODO: 12/6/23 temp disabled
    public void setIdle(Player player) {
//        perform(player, BuildingState.IDLE, null);
    }

    private BuildingResult perform(Player player, BuildingState state, @Nullable BlockHitResult hitResult) {
        return updateContext(player, context -> {
            if (hitResult == null) {
                Effortless.log("perform: hitResult is null");
                return context.reset();
            }
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                Effortless.log("perform: hitResult is " + hitResult.getType());
                return context.reset();
            }
            if (context.isBuilding() && context.state() != state) {
                return context.reset();
            }
            return context.withState(state).withNextHit(hitResult);
        });
    }

    public void setRightClickCooldown(int cooldown) {
        Minecraft.getInstance().rightClickDelay = cooldown; // for single build speed
    }

    public void handlePlayerBreak(Player player) {
        var hitResult = getContext(player).withBreakingState().trace(player, false);

        var perform = perform(player, BuildingState.BREAKING, hitResult);
        Effortless.log("handlePlayerBreak: " + perform);

        if (perform.isSuccess()) {
            //play sound if further than normal
            if ((hitResult.getLocation().subtract(player.getEyePosition(1f))).lengthSqr() > 25f) {
                var blockPos = hitResult.getBlockPos();
                var state = player.level.getBlockState(blockPos);
                var soundtype = state.getBlock().getSoundType(state);
                player.level.playSound(player, player.blockPosition(), soundtype.getBreakSound(), SoundSource.BLOCKS, 0.4f, soundtype.getPitch());
            }
        }
        player.swing(InteractionHand.MAIN_HAND);
    }

    public void handlePlayerPlace(Player player) {
        setRightClickCooldown(4); // for single build speed

        for (var interactionHand : InteractionHand.values()) {
            var itemStack = player.getItemInHand(interactionHand);

            // add placing state
            var hitResult = getContext(player).withPlacingState().trace(player, false);

//            if (!(itemStack.getItem() instanceof BlockItem)) return false; // pass

            var perform = perform(player, BuildingState.PLACING, hitResult);
            Effortless.log("handlePlayerPlace: " + perform);

            if (perform.isSuccess()) {
                //play sound if further than normal

                // TODO: 18/6/23  
//                if ((hitResult.getLocation().subtract(player.getEyePosition(1f))).lengthSqr() > 25f) {
//                    // FIXME: 18/6/23 java.lang.ClassCastException: class net.minecraft.world.item.AirItem cannot be cast to class net.minecraft.world.item.BlockItem (net.minecraft.world.item.AirItem and net.minecraft.world.item.BlockItem are in unnamed module of loader net.fabricmc.loader.impl.launch.knot.KnotClassLoader @68c4039c)
//                    var state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
//                    var blockPos = hitResult.getBlockPos();
//                    var soundType = state.getBlock().getSoundType(state);
//                    player.level.playSound(player, player.blockPosition(), soundType.getPlaceSound(), SoundSource.BLOCKS, 0.4f, soundType.getPitch());
//                }
            }

            player.swing(InteractionHand.MAIN_HAND);

            if (perform.isSuccess()) break;
//            return true; // consumed
        }
//        return false; // pass
    }

    public void cycleReplaceMode(Player player) {
        // TODO: 23/5/23  
//        setReplaceMode(player, ReplaceMode.values()[(getReplaceMode(player).ordinal() + 1) % ReplaceMode.values().length]);
    }

    public void cycleBuildMode(Player player, boolean reverse) {
        // TODO: 23/5/23  
//        setBuildMode(player, BuildMode.values()[(getBuildMode(player).ordinal() + 1) % BuildMode.values().length]);
//        Constructor.getInstance().reset(player);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////


//    public List<BlockPos> findCoordinates(Player player, BlockHitResult hitResult, boolean skipRaytrace) {
//        List<BlockPos> coordinates = new ArrayList<>();
//
//        var modeSettings = Constructor.getInstance().getModeSettings(player);
//        coordinates.addAll(modeSettings.buildMode().getInstance().collect(hitResult));
//
//        return coordinates;
//    }
//
//    public void reset(Player player) {
//        if (player == null) {
//            return;
//        }
//        resetBreakingPlacing(player);
//
//        Constructor.getInstance().getModeSettings(player).buildMode().getInstance().initialize(player);
//    }
//
//    private static Dictionary<Player, Boolean> getCurrentlyBreakingDict(Player player) {
//        return player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
//    }
//
//    public boolean isCurrentlyPlacing(Player player) {
//        return Boolean.FALSE.equals(getCurrentlyBreakingDict(player).get(player));
//    }
//
//    public boolean isCurrentlyBreaking(Player player) {
//        return Boolean.TRUE.equals(getCurrentlyBreakingDict(player).get(player));
//    }
//
//    public void setCurrentlyPlacing(Player player) {
//        getCurrentlyBreakingDict(player).put(player, false);
//    }
//
//    public void setCurrentlyBreaking(Player player) {
//        getCurrentlyBreakingDict(player).put(player, true);
//    }
//
//    public void resetBreakingPlacing(Player player) {
//        getCurrentlyBreakingDict(player).remove(player);
//    }
//
//    //Either placing or breaking
//    public boolean isActive(Player player) {
//        return getCurrentlyBreakingDict(player).get(player) != null;
//    }


    public void handleNewPlayer(ServerPlayer player) {
    }

//
//    public void placeBlocks(Player player, List<BlockHitResult> hitResults) {
//        if (player.getLevel().isClientSide()) {
//            BlockPreviewRenderer.getInstance().saveCurrentPreview();
//        }
//        var blockPosStates = Constructor.getInstance().getBlockPosStateForPlacing(player, hitResults);
//
//        for (var blockPosState : blockPosStates) {
//            if (!blockPosState.place()) continue;
//
//            var slot = InventoryHelper.findItemSlot(player.getInventory(), blockPosState.blockState().getBlock().asItem());
//            var swap = InventoryHelper.swapSlot(player.getInventory(), slot);
//            if (!swap) continue;
//
//            if (Constructor.getInstance().isReplace(player)) {
//                blockPosState.breakBy(player);
//            }
//
//            blockPosState.placeBy(player, InteractionHand.MAIN_HAND);
//            InventoryHelper.swapSlot(player.getInventory(), slot);
//        }
//    }
//
//    public void destroyBlocks(Player player, List<BlockHitResult> hitResults) {
//        if (player.getLevel().isClientSide()) {
//            BlockPreviewRenderer.getInstance().saveCurrentPreview();
//        }
//        var blockPosStates = Constructor.getInstance().getBlockPosStateForBreaking(player, hitResults);
//
//        for (var blockPosState : blockPosStates) {
////            if (!blockPosState.place()) continue;
//            blockPosState.breakBy(player);
//        }
//    }
//
//
//    public List<BlockPosState> getBlockPosStateForPlacing(Player player, List<BlockHitResult> blockHitResults) {
//
//        var level = player.getLevel();
//        var blockStates = findBlockStates(player, blockHitResults);
//        var result = new ArrayList<BlockPosState>(blockStates.size());
//
//        blockStates.forEach((blockPos, blockState) -> {
//            var blockPosState = new BlockPosState(level, blockPos, blockState, true);
//            if (blockPosState.canPlaceOn(player)) {
//                result.add(blockPosState);
//            } else {
////                result.add(new BlockPosState(level, blockPos, blockState, false));
//            }
//        });
//
//        return result;
//    }
//
//    public List<BlockPosState> getBlockPosStateForBreaking(Player player, List<BlockHitResult> blockHitResults) {
//
//        var level = player.getLevel();
//        var blockPoses = findCoordinatesByHitResult(player, blockHitResults);
//        var result = new ArrayList<BlockPosState>(blockPoses.size());
//
//        blockPoses.forEach(blockPos -> {
//            var blockPosState = new BlockPosState(level, blockPos, level.getBlockState(blockPos), true);
//            if (blockPosState.canBreakOn(player)) {
//                result.add(blockPosState);
//            } else {
////                result.add(new BlockPosState(level, blockPos, level.getBlockState(blockPos), false));
//            }
//        });
//
//        return result;
//    }
//
//
//    public Set<BlockPos> findCoordinatesByHitResult(Player player, BlockHitResult blockHitResult) {
//        return findCoordinatesByHitResult(player, Collections.singletonList(blockHitResult));
//    }
//
//    public Set<BlockPos> findCoordinatesByHitResult(Player player, List<BlockHitResult> blockHitResults) {
//        var coordinates = new LinkedHashSet<BlockPos>();
//        for (var hitResult : blockHitResults) {
//            coordinates.add(hitResult.getBlockPos());
//        }
//
//        for (var hitResult : blockHitResults) {
//            var arrayCoordinates = BuildModifier.getArray().findCoordinates(player, hitResult.getBlockPos());
//            coordinates.addAll(arrayCoordinates);
//            coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, hitResult.getBlockPos()));
//            coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, hitResult.getBlockPos()));
//            //get mirror for each array coordinate
//            for (var coordinate : arrayCoordinates) {
//                coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, coordinate));
//                coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, coordinate));
//            }
//        }
//
//        return coordinates;
//    }
//
//    public Set<BlockPos> findCoordinates(Player player, BlockPos blockPos) {
//        return findCoordinates(player, Collections.singletonList(blockPos));
//    }
//
//    public Set<BlockPos> findCoordinates(Player player, List<BlockPos> blockPosList) {
//        //Add current blocks being placed too
//        var coordinates = new LinkedHashSet<>(blockPosList);
//
//        //Find mirror/array/radial mirror coordinates for each blockpos
//        for (var blockPos : blockPosList) {
//            var arrayCoordinates = BuildModifier.getArray().findCoordinates(player, blockPos);
//            coordinates.addAll(arrayCoordinates);
//            coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, blockPos));
//            coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, blockPos));
//            //get mirror for each array coordinate
//            for (var coordinate : arrayCoordinates) {
//                coordinates.addAll(BuildModifier.getMirror().findCoordinates(player, coordinate));
//                coordinates.addAll(BuildModifier.getRadialMirror().findCoordinates(player, coordinate));
//            }
//        }
//
//        return coordinates;
//    }
//
//    public Map<BlockPos, BlockState> findBlockStates(Player player, List<BlockHitResult> hitResults) {
//        var blockStates = new LinkedHashMap<BlockPos, BlockState>();
//
//        for (var hitResult : hitResults) {
//            var itemStack = player.getMainHandItem();
//            var blockPos = hitResult.getBlockPos();
//            var blockState = getBlockStateFromItem(player, InteractionHand.MAIN_HAND, itemStack, hitResult);
////            if (blockState == null) continue;
//            blockStates.put(blockPos, blockState);
//        }
//
//        // TODO: 11/1/23 use states over coordinates
//        for (var hitResult : hitResults) {
//            var itemStack = player.getMainHandItem();
//            var blockPos = hitResult.getBlockPos();
//            var blockState = getBlockStateFromItem(player, InteractionHand.MAIN_HAND, itemStack, hitResult);
////            if (blockState == null) continue;
//
//            var arrayBlockStates = BuildModifier.getArray().findBlockStates(player, blockPos, blockState);
//            blockStates.putAll(arrayBlockStates);
//
//            blockStates.putAll(BuildModifier.getMirror().findBlockStates(player, blockPos, blockState));
//            blockStates.putAll(BuildModifier.getRadialMirror().findBlockStates(player, blockPos, blockState));
//            //add mirror for each array coordinate
//            for (BlockPos coordinate : BuildModifier.getArray().findCoordinates(player, blockPos)) {
//                var blockState1 = arrayBlockStates.get(coordinate);
////                if (blockState1 == null) continue;
//
//                blockStates.putAll(BuildModifier.getMirror().findBlockStates(player, coordinate, blockState1));
//                blockStates.putAll(BuildModifier.getRadialMirror().findBlockStates(player, coordinate, blockState1));
//            }
//        }
//
//        return blockStates;
//    }
//
//    public boolean isEnabled(ModifierSettings modifierSettings, BlockPos startPos) {
//        return Array.isEnabled(modifierSettings.arraySettings()) ||
//                Mirror.isEnabled(modifierSettings.mirrorSettings(), startPos) ||
//                RadialMirror.isEnabled(modifierSettings.radialMirrorSettings(), startPos) ||
//                modifierSettings.enableQuickReplace();
//    }
//
//    public BlockState getBlockStateFromItem(Player player, InteractionHand hand, ItemStack itemStack, BlockHitResult hitResult) {
//
//        var blockPlaceContext = new BlockPlaceContext(player, hand, itemStack, hitResult);
//        var item = itemStack.getItem();
//
//        if (item instanceof BlockItem blockItem) {
//            var state = blockItem.getPlacementState(blockPlaceContext);
//            return state != null ? state : Blocks.AIR.defaultBlockState();
//        } else {
//            return Block.byItem(item).getStateForPlacement(blockPlaceContext);
//        }
//    }
//
//    //Returns true if equal (or both null)
//    public boolean compareCoordinates(List<BlockPos> coordinates1, List<BlockPos> coordinates2) {
//        if (coordinates1 == null && coordinates2 == null) return true;
//        if (coordinates1 == null || coordinates2 == null) return false;
//
//        //Check count, not actual values
//        if (coordinates1.size() == coordinates2.size()) {
//            if (coordinates1.size() == 1) {
//                return coordinates1.get(0).equals(coordinates2.get(0));
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//
//    //Retrieves the build settings of a player through the modifierCapability capability
//    //Never returns null
//    public ModifierSettings getModifierSettings(Player player) {
//        return ((EffortlessDataProvider) player).getModifierSettings();
//    }
//
//    public void setModifierSettings(Player player, ModifierSettings modifierSettings) {
//        if (player == null) {
//            Effortless.log("Cannot set build modifier settings, player is null");
//            return;
//        }
//        ((EffortlessDataProvider) player).setModifierSettings(modifierSettings);
//
//    }
//
//    public boolean isReplace(Player player) {
//        return getModifierSettings(player).enableReplace();
//    }
//
//    public boolean isQuickReplace(Player player) {
//        return getModifierSettings(player).enableQuickReplace();
//    }
//
//    public ReplaceMode getReplaceMode(Player player) {
//        return getModifierSettings(player).replaceMode();
//    }
//
//    public void setReplaceMode(Player player, ReplaceMode mode) {
//        var modifierSettings = getModifierSettings(player);
//        modifierSettings = new ModifierSettings(modifierSettings.arraySettings(), modifierSettings.mirrorSettings(), modifierSettings.radialMirrorSettings(), mode);
//        Constructor.getInstance().setModifierSettings(player, modifierSettings);
//        setModifierSettings(player, modifierSettings);
//    }
//
//
//    public void toggleReplaceMode(Player player) {
//        setReplaceMode(player, getReplaceMode(player) == ReplaceMode.DISABLED ? ReplaceMode.NORMAL : ReplaceMode.DISABLED);
//    }
//
//    public void toggleQuickReplaceMode(Player player) {
//        setReplaceMode(player, getReplaceMode(player) == ReplaceMode.DISABLED ? ReplaceMode.QUICK : ReplaceMode.DISABLED);
//    }
//
//    public Component getReplaceModeName(Player player) {
//        var modifierSettings = getModifierSettings(player);
//        return Component.literal(ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (modifierSettings.enableReplace() ? (modifierSettings.enableQuickReplace() ? (ChatFormatting.GREEN + "QUICK") : (ChatFormatting.GREEN + "ON")) : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET);
//    }
//
//    public String getSanitizeMessage(ModifierSettings modifierSettings, Player player) {
//        int maxReach = ReachHelper.getMaxReachDistance(player);
//        String error = "";
//
//        //Array settings
//        var arraySettings = modifierSettings.arraySettings();
//        if (arraySettings.count() < 1) {
//            error += "Array count has to be at least 1. It has been reset to 1. \n";
//        }
//
//        if (arraySettings.reach() > maxReach) {
//            error += "Array exceeds your maximum reach of " + maxReach + ". Array count has been reset to 0. \n";
//        }
//
//        //Mirror settings
//        var mirrorSettings = modifierSettings.mirrorSettings();
//        if (mirrorSettings.radius() < 1) {
//            error += "Mirror size has to be at least 1. This has been corrected. \n";
//        }
//        if (mirrorSettings.reach() > maxReach) {
//            error += "Mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". \n";
//        }
//
//        //Radial mirror settings
//        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
//        if (radialMirrorSettings.slices() < 2) {
//            error += "Radial mirror needs to have at least 2 slices. Slices has been set to 2. \n";
//        }
//
//        if (radialMirrorSettings.radius() < 1) {
//            error += "Radial mirror radius has to be at least 1. This has been corrected. \n";
//        }
//        if (radialMirrorSettings.reach() > maxReach) {
//            error += "Radial mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". \n";
//        }
//
//        return error;
//    }
//
//    // TODO: 17/9/22
//    public ModifierSettings sanitize(ModifierSettings modifierSettings, Player player) {
//        int maxReach = ReachHelper.getMaxReachDistance(player);
//
//        //Array settings
//        var arraySettings = modifierSettings.arraySettings();
//        int count = arraySettings.count();
//        if (count < 1) {
//            count = 1;
//        }
//
//        if (arraySettings.reach() > maxReach) {
//            count = 1;
//        }
//        arraySettings = new Array.ArraySettings(
//                arraySettings.enabled(),
//                arraySettings.offset(),
//                count
//        );
//
//        //Mirror settings
//        var mirrorSettings = modifierSettings.mirrorSettings();
//        int radius = mirrorSettings.radius();
//        if (radius < 1) {
//            radius = 1;
//        }
//        if (mirrorSettings.reach() > maxReach) {
//            radius = maxReach / 2;
//        }
//        mirrorSettings = new Mirror.MirrorSettings(
//                mirrorSettings.enabled(),
//                mirrorSettings.position(),
//                mirrorSettings.mirrorX(),
//                mirrorSettings.mirrorY(),
//                mirrorSettings.mirrorZ(),
//                radius,
//                mirrorSettings.drawLines(),
//                mirrorSettings.drawPlanes()
//        );
//
//        //Radial mirror settings
//        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
//        int slices = radialMirrorSettings.slices();
//        if (slices < 2) {
//            slices = 2;
//        }
//        int radius1 = radialMirrorSettings.radius();
//        if (radius1 < 1) {
//            radius1 = 1;
//        }
//        if (radialMirrorSettings.reach() > maxReach) {
//            radius1 = maxReach / 2;
//        }
//        radialMirrorSettings = new RadialMirror.RadialMirrorSettings(
//                radialMirrorSettings.enabled(),
//                radialMirrorSettings.position(),
//                slices,
//                radialMirrorSettings.alternate(),
//                radius1,
//                radialMirrorSettings.drawLines(),
//                radialMirrorSettings.drawPlanes()
//        );
//
//        //Other
//        var replaceMode = modifierSettings.replaceMode();
//
//        return new ModifierSettings(
//                arraySettings,
//                mirrorSettings,
//                radialMirrorSettings,
//                replaceMode
//        );
//    }
//
//    // client
//
//
//
//    public static ModeSettings getModeSettings(Player player) {
//        return ((EffortlessDataProvider) player).getModeSettings();
//    }
//
//
//    public static void setModeSettings(Player player, ModeSettings modeSettings) {
//        if (player == null) {
//            Effortless.log("Cannot set buildmode settings, player is null");
//            return;
//        }
//        ((EffortlessDataProvider) player).setModeSettings(modeSettings);
//    }
//
//    public static BuildMode getBuildMode(Player player) {
//        return getModeSettings(player).buildMode();
//    }
//
//    public static void setBuildMode(Player player, BuildMode mode) {
//        ModeSettings modeSettings = getModeSettings(player);
//        modeSettings = new ModeSettings(mode, modeSettings.enableMagnet());
//        setModeSettings(player, modeSettings);
//    }
//
//
//    public static void reverseBuildMode(Player player) {
//        setBuildMode(player, BuildMode.values()[(getBuildMode(player).ordinal() + BuildMode.values().length - 1) % BuildMode.values().length]);
//        Constructor.getInstance().reset(player);
//    }
//
//    public static boolean isEnableMagnet(Player player) {
//        return getModeSettings(player).enableMagnet();
//    }
//
//    public static void setEnableMagnet(Player player, boolean enableMagnet) {
//        ModeSettings modeSettings = getModeSettings(player);
//        modeSettings = new ModeSettings(modeSettings.buildMode(), enableMagnet);
//        setModeSettings(player, modeSettings);
//    }
//
//
//    public static String getSanitizeMessage(ModeSettings modeSettings, Player player) {
//        int maxReach = ReachHelper.getMaxReachDistance(player);
//        String error = "";
//
//        //TODO sanitize
//
//        return error;
//    }
//
//    public static ModeSettings sanitize(ModeSettings modeSettings, Player player) {
//        return modeSettings;
//    }
//
//
//    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
//        float f = player.getXRot();
//        float g = player.getYRot();
//        var vec3 = player.getEyePosition();
//        float h = Mth.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
//        float i = Mth.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
//        float j = -Mth.cos(-f * ((float) Math.PI / 180));
//        float k = Mth.sin(-f * ((float) Math.PI / 180));
//        float l = i * j;
//        float m = k;
//        float n = h * j;
//        double d = 5.0;
//        var vec32 = vec3.add((double) l * 5.0, (double) m * 5.0, (double) n * 5.0);
//        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
//    }
//
//    public static HitResult getLookingAt(Player player) {
//        var level = player.level;
//
//        //base distance off of player ability (config)
//        float raytraceRange = ReachHelper.getPlacementReach(player) * 4;
//
//        var look = player.getLookAngle();
//        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
//        var end = new Vec3(player.getX() + look.x * raytraceRange, player.getY() + player.getEyeHeight() + look.y * raytraceRange, player.getZ() + look.z * raytraceRange);
////        return player.rayTrace(raytraceRange, 1f, RayTraceFluidMode.NEVER);
//        //TODO 1.14 check if correct
//        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
//    }


    public static class ContextProvider {

        private final Map<UUID, Context> contexts = new HashMap<>();

        public static Context defaultContext() {
            return Context.defaultSet();
        }

        public Context get(Player player) {
            return contexts.computeIfAbsent(player.getUUID(), (uuid) -> defaultContext());
        }

        public void set(Player player, Context context) {
            contexts.put(player.getUUID(), context);
            Effortless.log("setContext: " + player.getUUID() + " to " + context);
        }

        public void remove(Player player) {
            contexts.remove(player.getUUID());
        }

        public void tick() {

        }

    }
}
