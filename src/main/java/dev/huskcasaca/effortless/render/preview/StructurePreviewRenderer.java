package dev.huskcasaca.effortless.render.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.BuildingState;
import dev.huskcasaca.effortless.building.EffortlessBuilder;
import dev.huskcasaca.effortless.building.TempItemStorage;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.building.operation.BlockStatePlaceOperation;
import dev.huskcasaca.effortless.building.operation.StructureOperation;
import dev.huskcasaca.effortless.config.ConfigManager;
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

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StructurePreviewRenderer {

    private static final StructurePreviewRenderer INSTANCE = new StructurePreviewRenderer(Minecraft.getInstance());
    private final Minecraft minecraft;
    private final List<StructureOperation.Result> history = new ArrayList<>();
    private final EffortlessBuilder builder = EffortlessBuilder.getInstance();
    private int soundTime = 0;
    private boolean hasLastMessage = false;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private StructureOperation.Result currentPreview = StructureOperation.Result.EMPTY;

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

    public void saveCurrentPreview(StructureOperation.Result preview) {
        if (shouldRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
//            history.add(preview);
        }
    }

    public void saveCurrentBreakPreview() {
        saveCurrentBreakPreview(currentPreview);
    }

    public void saveCurrentBreakPreview(StructureOperation.Result preview) {
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

    public StructureOperation.Result getCurrentPreview() {
        return currentPreview == null ? StructureOperation.Result.EMPTY : currentPreview;
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
            currentPreview = StructureOperation.Result.EMPTY;
            return;
        }

        var context = builder.getContext(player).withState(BuildingState.PLACING).withNextHit(player, true);
        var storage = new TempItemStorage(player.getInventory().items);
        var operation = context.getStructure(player.getLevel(), player, storage);
        var result = operation.perform();
        operation.getRenderer().render(poseStack, multiBufferSource, result);

        // TODO: 21/6/23 move to op renderer
        if (result.type().isSuccess()) {
            currentPreview = result;


        } else {
            currentPreview = StructureOperation.Result.EMPTY;
        }

        if (context.isBuilding()) {
            showMessage(player, context, result);
        } else {
            clearMessage(player);
        }

    }

    private void showMessage(Player player, BuildContext context, StructureOperation.Result result) {
        if (result.type().isSuccess()) {
            showBlockPlaceMessage(player, context, result);
        } else {
            showTracingFailedMessage(player, context);
        }
    }

    private void showBlockPlaceMessage(Player player, BuildContext context, StructureOperation.Result result) {
        var volume = result.size();

        var dimensions = "(";
        if (volume.getX() > 1) dimensions += volume.getX() + "x";
        if (volume.getZ() > 1) dimensions += volume.getZ() + "x";
        if (volume.getY() > 1) dimensions += volume.getY() + "x";
        dimensions = dimensions.substring(0, dimensions.length() - 1);
        if (dimensions.length() > 1) dimensions += ")";

        var blockCounter = "" + ChatFormatting.WHITE + result.usages().sufficientCount() + ChatFormatting.RESET + (result.usages().isFilled() ? " " : " + " + ChatFormatting.RED + result.usages().insufficientCount() + ChatFormatting.RESET + " ") + (result.usages().totalCount() == 1 ? "block" : "blocks");

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
