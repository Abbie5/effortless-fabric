package dev.effortless.building;

import dev.effortless.Effortless;
import dev.effortless.building.base.MultiSelectFeature;
import dev.effortless.building.base.SingleSelectFeature;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.operation.Operations;
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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class EffortlessBuilder {

    private static final EffortlessBuilder INSTANCE = new EffortlessBuilder();
    private final ContextProvider provider = new ContextProvider();

    public static EffortlessBuilder getInstance() {
        return INSTANCE;
    }

    private static void showLocalPreview(Player player, Context context) {
        var result = Operations.createStructurePreview(player, context).perform();
        OverlayHelper.showOperationResult(context.uuid(), result);
        OverlayHelper.showContext(context.uuid(), context, 0);
        OverlayHelper.showStructureResult(context.uuid(), result, 1);
    }

    private static void showLocalPreviewOnce(Player player, Context context) {
        var result = Operations.createStructurePreviewOnce(player, context).perform();
        OverlayHelper.showOperationResult(context.uuid(), result);
        OverlayHelper.showStructureResult(context.uuid(), result, 1000);
    }

    private static void showPreview(Player player, Context context) {
        var result = Operations.createStructurePreview(player, context).perform();
        OverlayHelper.showStructureResult(context.uuid(), result, 1);
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

    private BuildingResult updateContext(Player player, UnaryOperator<Context> updater) {
        var context = provider.get(player);
        var updated = updater.apply(context);
        if (updated.isFulfilled()) {

            showLocalPreviewOnce(player, updated);

            Packets.channel().sendToServer(new ServerboundPlayerBuildPacket(updated));
            Effortless.log("Sent to server: " + updated);
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

    private Context getContextWithNextTrace(Player player) {
        var context = getContext(player).withPreviewSource();
        if (context.noClicks()) {
            if (player.getMainHandItem().isEmpty()) {
                context = context.withBreakingState();
            } else {
                context = context.withPlacingState();
            }
        }
        return context.withNextHitTraced(player).withUUID(player.getUUID());
    }

    public Context getContext(Player player) {
        return provider.get(player);
    }

    public void tick() {
        provider.tick();
        var player = Minecraft.getInstance().player;
        if (player != null && !getContext(player).isDisabled()) {
            var context = getContextWithNextTrace(player);
            showLocalPreview(player, context);
            Packets.channel().sendToServer(new ServerboundPlayerBuildPacket(context));
        }
    }

    // from settings screen
    public void setBuildMode(Player player, BuildMode buildMode) {
        updateContext(player, context -> context.withEmptyHits().withBuildMode(buildMode));
    }

    public void setBuildFeature(Player player, SingleSelectFeature feature) {
        updateContext(player, context -> context.withBuildFeature(feature));
    }

    public void setBuildFeature(Player player, MultiSelectFeature feature) {
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
        var hitResult = context.withBreakingState().trace(player);
        var perform = perform(player, BuildingState.BREAK_BLOCK, hitResult);

        if (perform.isSuccess()) {
            //play sound if further than normal
            // TODO: 22/7/23
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

            var hitResult = context.withPlacingState().trace(player);
            var perform = perform(player, BuildingState.PLACE_BLOCK, hitResult);

            if (perform.isSuccess()) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            break;
        }
    }

    public void onContextReceived(Player player, Context context) {
        showPreview(player, context);
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
        }

        public void remove(Player player) {
            contexts.remove(player.getUUID());
        }

        public void tick() {

        }

    }
}
