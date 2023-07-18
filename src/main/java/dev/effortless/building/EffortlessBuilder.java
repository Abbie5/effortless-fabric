package dev.effortless.building;

import dev.effortless.Effortless;
import dev.effortless.building.mode.BuildFeature;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.operation.StructureBuildOperation;
import dev.effortless.network.Packets;
import dev.effortless.network.protocol.building.ServerboundPlayerBuildPacket;
import dev.effortless.utils.OverlayHelper;
import net.minecraft.client.Minecraft;
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
import java.util.stream.Collectors;

public class EffortlessBuilder {

    private static final EffortlessBuilder INSTANCE = new EffortlessBuilder();
    private static final UUID BUILDING_UUID = UUID.randomUUID();
    private final ContextProvider provider = new ContextProvider();

    private static StructureBuildOperation generateStructurePreviewFromContext(Player player, Context context, Boolean once) {
        var storage = Storage.createTemp(player.getInventory().items);
        return new StructureBuildOperation(player.getLevel(), player, context, storage, once);
    }

    private static StructureBuildOperation generateStructureFromContext(Player player, Context context) {
        return new StructureBuildOperation(player.getLevel(), player, context);
    }

    public static EffortlessBuilder getInstance() {
        return INSTANCE;
    }

    private BuildingResult perform(Player player, BuildingState state, @Nullable BlockHitResult hitResult) {
        return updateContext(player, context -> {
            if (hitResult == null) {
                Effortless.log("updateContext: hitResult is null");
                return context.reset();
            }
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                Effortless.log("updateContext: hitResult is " + hitResult.getType());
                return context.reset();
            }
            if (context.isBuilding() && context.state() != state) {
                return context.reset();
            }
            return context.withState(state).withNextHit(hitResult);
        });
    }

    private BuildingResult updateContext(Player player, Function<Context, Context> updater) {
        var context = provider.get(player);
        var updated = updater.apply(context);
        if (updated.isFulfilled()) {

            var result = generateStructurePreviewFromContext(player, updated, true).perform();
            OverlayHelper.showOperationResult(updated.uuid(), result);
            OverlayHelper.showItemStackSummary(updated.uuid(), result, 1000);

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

    private void generateClientPreview() {
        var player = Minecraft.getInstance().player;
        if (player == null) return; // necessary

        var context = getContext(player);
        if (context.noClicks()) {
            if (player.getMainHandItem().isEmpty()) {
                context = context.withBreakingState();
            } else {
                context = context.withPlacingState();
            }
        }
        context = context.withNextHit(player, true);
        var result = generateStructurePreviewFromContext(player, context, false).perform();

        OverlayHelper.showOperationResult(BUILDING_UUID, result);
        OverlayHelper.showContainerContext(BUILDING_UUID, context, 0);
        OverlayHelper.showItemStackSummary(BUILDING_UUID, result, 1);
    }

    public Context getContext(Player player) {
        return provider.get(player);
    }

    public void tick() {
        provider.tick();
        generateClientPreview();
    }

    // from settings screen
    public void setBuildMode(Player player, BuildMode buildMode) {
        updateContext(player, context -> context.withEmptyHits().withBuildMode(buildMode));
    }

    public void setBuildFeature(Player player, BuildFeature.SingleSelectEntry feature) {
        updateContext(player, context -> {
            return context.withBuildFeature(feature);
        });
    }

    public void setBuildFeature(Player player, BuildFeature.MultiSelectEntry feature) {
        updateContext(player, context -> {
            var features = context.buildFeatures().stream().filter((f) -> f.getClass().equals(feature.getClass())).collect(Collectors.toSet());
            if (features.contains(feature)) {
                if (features.size() > 1) {
                    features.remove(feature);
                }
            } else {
                features.add(feature);
            }
            return context.withBuildFeature(features);
        });
    }

    public void setRightClickDelay(int delay) {
        Minecraft.getInstance().rightClickDelay = delay; // for single build speed
    }

    public void handlePlayerBreak(Player player) {
        var context = getContext(player);
        var hitResult = context.withBreakingState().trace(player, false);
        var perform = perform(player, BuildingState.BREAK_BLOCK, hitResult);

        if (perform.isSuccess()) {
            //play sound if further than normal
            if ((hitResult.getLocation().subtract(player.getEyePosition(1f))).lengthSqr() > 25f) {
                var blockPos = hitResult.getBlockPos();
                var state = player.level.getBlockState(blockPos);
                var soundtype = state.getBlock().getSoundType(state);
                player.level.playSound(player, player.blockPosition(), soundtype.getBreakSound(), SoundSource.BLOCKS, 0.4f, soundtype.getPitch());
            }
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public void handlePlayerPlace(Player player) {
        var context = getContext(player);
        setRightClickDelay(4); // for single build speed

        for (var interactionHand : InteractionHand.values()) {

            if (player.getItemInHand(interactionHand).isEmpty()) {
                continue;
            }

            var hitResult = context.withPlacingState().trace(player, false);
            var perform = perform(player, BuildingState.PLACE_BLOCK, hitResult);

            if (perform.isSuccess()) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            break;
        }
    }

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
//            Effortless.log("setContext: " + player.getUUID() + " to " + context);
        }

        public void remove(Player player) {
            contexts.remove(player.getUUID());
        }

        public void tick() {

        }

    }
}
