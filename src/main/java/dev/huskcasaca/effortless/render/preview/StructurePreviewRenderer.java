package dev.huskcasaca.effortless.render.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.EffortlessBuilder;
import dev.huskcasaca.effortless.building.StructurePreview;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.building.operation.BlockStatePlaceOperation;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.render.RenderTypes;
import dev.huskcasaca.effortless.render.outliner.OutlineRenderer;
import dev.huskcasaca.effortless.utils.AnimationTicker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StructurePreviewRenderer {

    private static final StructurePreviewRenderer INSTANCE = new StructurePreviewRenderer(Minecraft.getInstance());
    private final Minecraft minecraft;
    private final List<StructurePreview> history = new ArrayList<>();
    private final EffortlessBuilder builder = EffortlessBuilder.getInstance();
    private int soundTime = 0;
    private boolean hasLastMessage = false;

    private static final Color PLACING_COLOR = new Color(0.92f, 0.92f, 0.92f, 1f);
    private static final Color BREAKING_COLOR = new Color(0.95f, 0f, 0f, 1f);
    private final ConfigManager configManager = ConfigManager.getInstance();
    private StructurePreview currentPreview = StructurePreview.EMPTY;

    public StructurePreviewRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private static void sortOnDistanceToPlayer(List<BlockStatePlaceOperation> blockPosStates, Player player) {
        blockPosStates.sort((lpl, rpl) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public static StructurePreviewRenderer getInstance() {
        return INSTANCE;
    }

    private boolean shouldRenderBlockPreviews(Player player) {
        return configManager.getConfig().getPreviewConfig().isAlwaysShowBlockPreview() || builder.getContext(player).buildMode() != BuildMode.DISABLED;
    }

    public void saveCurrentPreview() {
        saveCurrentPreview(currentPreview);
    }

    public void saveCurrentPreview(StructurePreview preview) {
        if (shouldRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
//            history.add(preview);
        }
    }

    public void saveCurrentBreakPreview() {
        saveCurrentBreakPreview(currentPreview);
    }

    public void saveCurrentBreakPreview(StructurePreview preview) {
        if (shouldRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
//            history.add(preview);
        }
    }

    @Deprecated
    public void saveCurrentPreview(List<BlockPos> coordinates, List<BlockState> blockStates, BlockPos firstPos, BlockPos secondPos) {
        // no-op
    }

    @Deprecated
    public void saveCurrentBreakPreview(List<BlockPos> coordinates, List<BlockState> blockStates, BlockPos firstPos, BlockPos secondPos) {
        // no-op
    }

    public StructurePreview getCurrentPreview() {
        return currentPreview == null ? StructurePreview.EMPTY : currentPreview;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource) {
        var player = minecraft.player;

        renderStructurePreview(poseStack, multiBufferSource, player);
        renderStructurePreviewHistory(poseStack, multiBufferSource, player);
    }

    private void renderStructurePreviewHistory(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {
        if (!shouldRenderBlockPreviews(player)) {
            history.clear();
            return;
        }

        // TODO: 26/5/23
//        history.forEach(preview -> renderStructureShader(poseStack, multiBufferSource, preview));
        // expire
        // TODO: 26/5/23
//        history.removeIf(preview -> preview.time() + preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier() < getGameTime());
    }

    private void renderStructurePreview(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {
        if (!shouldRenderBlockPreviews(player)) {
            currentPreview = StructurePreview.EMPTY;
            return;
        }

        var context = builder.getContext(player).withNextHit(player, true);
        var state = context.state();
        var tracingResult = context.collect();
        var preview = context.getStructure(player).preview();

        if (tracingResult.type().isSuccess()) {
//            if (!preview.isEmpty() && soundTime < getGameTime() && !BlocksPreview.arePreviewSizeEqual(preview, currentPreview)) {
//                soundTime = getGameTime();
//                var soundType = preview.blockPosStates().get(0).blockState().getSoundType();
//                player.getLevel().playSound(player, player.blockPosition(), context.isBreaking() ? soundType.getBreakSound() : soundType.getPlaceSound(), SoundSource.BLOCKS, 0.3f, 0.8f);
//            }
//                switch (ConfigManager.getGlobalPreviewConfig().getBlockPreviewMode()) {
//                    case OUTLINES -> renderBlockOutlines(poseStack, multiBufferSource, preview, 0);
//                    case DISSOLVE_SHADER -> renderStructureShader(poseStack, multiBufferSource, preview, 0);
//                }
            currentPreview = preview;
            renderStructureShader(poseStack, multiBufferSource, preview);

            OutlineRenderer.getInstance().showCluster(context.uuid(), preview.blockPoses())
                    .texture(RenderTypes.CHECKERED_THIN_TEXTURE_LOCATION)
                    .stroke(1 / 64f)
                    .colored(context.isBreaking() ? BREAKING_COLOR : PLACING_COLOR)
                    .disableNormals();

            if (context.isBuilding()) {
                showBlockPlaceMessage(player, preview, context);
            } else {
                clearMessage(player);
            }
        } else {
            currentPreview = StructurePreview.EMPTY;
            if (context.isBuilding()) {
                showTracingFailedMessage(player, context);
            } else {
                clearMessage(player);
            }
        }

    }

    private void renderStructureShader(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, StructurePreview preview) {
        if (preview.isEmpty()) return;
        var dispatcher = minecraft.getBlockRenderer();

        double totalTime = preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier();
//        float dissolve = (getGameTime() - preview.time()) / (float) totalTime;

        float dissolve = 1;

        var firstPos = preview.firstPos();
        var secondPos = preview.secondPos();

//        var states =  preview.operations().stream().map((op) -> {
//            return op.getFirst().ge
//        }).collect(Collectors.toList());
//
//        for (var blockPosState :) {
//            var level = blockPosState.level();
//            var blockPos = blockPosState.blockPos();
//            var blockState = blockPosState.blockState();
//            var item = blockState.getBlock().asItem();
//            var itemStack = inventory.findItemStackByItem(item);
//
//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }
//            var red = breaking || (!skip && itemStack.isEmpty());
//
//            renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);
//            if (skip || breaking) {
//                continue;
//            }
//            itemStack.shrink(1);
//        }
    }




    private void showBlockPlaceMessage(Player player, StructurePreview preview, BuildContext context) {
        var volume = preview.size();

        var dimensions = "(";
        if (volume.getX() > 1) dimensions += volume.getX() + "x";
        if (volume.getZ() > 1) dimensions += volume.getZ() + "x";
        if (volume.getY() > 1) dimensions += volume.getY() + "x";
        dimensions = dimensions.substring(0, dimensions.length() - 1);
        if (dimensions.length() > 1) dimensions += ")";

        var blockCounter = "" + ChatFormatting.WHITE + preview.usages().sufficientCount() + ChatFormatting.RESET + (preview.usages().isFilled() ? " " : " + " + ChatFormatting.RED + preview.usages().insufficientCount() + ChatFormatting.RESET + " ") + (preview.usages().totalCount() == 1 ? "block" : "blocks");

        var buildingText = switch (context.state()) {
            case IDLE -> "idle";
            case PLACING -> "placing";
            case BREAKING -> "breaking";
        };

        displayActionBarMessage(player, "%s%s%s of %s %s %s".formatted(ChatFormatting.GOLD, context.getTranslatedModeOptionName(), ChatFormatting.RESET, buildingText, blockCounter, dimensions));
    }

    private void showTracingFailedMessage(Player player, BuildContext context) {
        displayActionBarMessage(player, "%s%s%s %s".formatted(ChatFormatting.GOLD, context.getTranslatedModeOptionName(), ChatFormatting.RESET, "cannot be traced"));
    }

    private void displayActionBarMessage(Player player, String message) {
        if (builder.getContext(player).isBuilding()) {
            Effortless.log(player, message, true);
            hasLastMessage = true;
        }
    }

    private void clearMessage(Player player) {
        if (builder.getContext(player).isBuilding() && hasLastMessage) {
            Effortless.log(player, "", true);
            hasLastMessage = false;
        }
    }

    private int getGameTime() {
        return AnimationTicker.getTicks();
    }

}
