package dev.huskcasaca.effortless.render.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BlockPosState;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.render.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BlockPreviewRenderer {

    private static final BlockPreviewRenderer INSTANCE = new BlockPreviewRenderer(Minecraft.getInstance());
    private final Minecraft minecraft;
    private final List<BlocksPreview> history = new ArrayList<>();
    private BlocksPreview currentPreview = BlocksPreview.EMPTY;
    private int soundTime = 0;
    private boolean hasLastMessage = false;

    public BlockPreviewRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private static boolean doRenderBlockPreviews(Player player) {
        return ConfigManager.getGlobalPreviewConfig().isAlwaysShowBlockPreview() || (BuildModeHelper.getBuildMode(player) != BuildMode.DISABLE);
    }

    private static void sortOnDistanceToPlayer(List<BlockPosState> blockPosStates, Player player) {
        blockPosStates.sort((lpl, rpl) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public static BlockPreviewRenderer getInstance() {
        return INSTANCE;
    }

    public void saveCurrentPreview() {
        saveCurrentPreview(currentPreview);
    }

    public void saveCurrentPreview(BlocksPreview preview) {
        if (doRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
            history.add(preview);
        }
    }

    public void saveCurrentBreakPreview() {
        saveCurrentBreakPreview(currentPreview);
    }

    public void saveCurrentBreakPreview(BlocksPreview preview) {
        if (doRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
            history.add(preview);
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

    public BlocksPreview getCurrentPreview() {
        return currentPreview == null ? BlocksPreview.EMPTY : currentPreview;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource) {
        var player = minecraft.player;

        renderBlockPreviewHistory(poseStack, multiBufferSource, player);
        renderBlockPreview(poseStack, multiBufferSource, player);
    }

    private void renderBlockPreviewHistory(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {
        if (!doRenderBlockPreviews(player)) {
            clearPreviewHistory();
            return;
        }

        if (PreviewConfig.useShader()) {
            for (var preview : history) {
                if (preview.blockPosStates().isEmpty()) {
                    continue;
                }
                double totalTime = preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier();
                float dissolve = (EffortlessClient.getTicksInGame() - preview.time()) / (float) totalTime;

                renderBlockDissolveShader(poseStack, multiBufferSource, preview, dissolve);
            }
        }
        //Expire
        history.removeIf(preview -> preview.time() + preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier() < EffortlessClient.getTicksInGame());
    }

    private void renderBlockPreview(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {

        if (!doRenderBlockPreviews(player)) {
            clearPreview();
            return;
        }

        var hitResult = EffortlessClient.getLookingAt(player);
        if (BuildModeHelper.getBuildMode(player) == BuildMode.DISABLE) {
            hitResult = minecraft.hitResult;
        }

        var breaking = BuildModeHandler.isCurrentlyBreaking(player);
        var tracingResult = TracingResult.trace(player, hitResult, breaking, false);

        switch (tracingResult.type()) {

            case SUCCESS -> {
                var hitResults = tracingResult.result();

                var blockPosStates = breaking ? BuildModifierHandler.getBlockPosStateForBreaking(player, hitResults) : BuildModifierHandler.getBlockPosStateForPlacing(player, hitResults);
                var preview = BlocksPreview.snapshot(player, blockPosStates, breaking);

                if (!preview.isEmpty() && soundTime < EffortlessClient.getTicksInGame() && !BlocksPreview.arePreviewSizeEqual(preview, currentPreview)) {
                    soundTime = EffortlessClient.getTicksInGame();
                    var soundType = preview.blockPosStates().get(0).blockState().getSoundType();
                    player.getLevel().playSound(player, player.blockPosition(), breaking ? soundType.getBreakSound() : soundType.getPlaceSound(), SoundSource.BLOCKS, 0.3f, 0.8f);
                }

                currentPreview = preview;

                switch (ConfigManager.getGlobalPreviewConfig().getBlockPreviewMode()) {
                    case OUTLINES -> renderBlockOutlines(poseStack, multiBufferSource, preview, 0);
                    case DISSOLVE_SHADER -> renderBlockDissolveShader(poseStack, multiBufferSource, preview, 0);
                }
                if (BuildModeHandler.isActive(player)) {
                    displayBlockPlaceMessage(player, preview, breaking);
                } else {
                    clearActionBarMessage(player);
                }
            }
            case MISS_DIRECTION, MISS_VECTOR, PASS, FAIL -> {
                clearPreview();
                if (BuildModeHandler.isActive(player)) {
                    displayTracingFailedMessage(player);
                } else {
                    clearActionBarMessage(player);
                }
            }
        }

    }

    private void renderBlockDissolveShader(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, BlocksPreview preview, float dissolve) {
        renderBlockDissolveShader(poseStack, multiBufferSource, preview.blockPosStates(), preview.createPreviewInventory(), preview.breaking(), preview.skip(), dissolve);
    }

    private void renderBlockDissolveShader(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, List<BlockPosState> blockPosStates, PreviewInventory inventory, boolean breaking, boolean skip, float dissolve) {
        if (blockPosStates.isEmpty()) return;

        var firstPos = blockPosStates.get(0).blockPos();
        var secondPos = blockPosStates.get(blockPosStates.size() - 1).blockPos();

        var dispatcher = minecraft.getBlockRenderer();

        for (var blockPosState : blockPosStates) {
            var level = blockPosState.level();
            var blockPos = blockPosState.blockPos();
            var blockState = blockPosState.blockState();
            var item = blockState.getBlock().asItem();
            var itemStack = inventory.findItemStackByItem(item);

            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
            }
            var red = breaking || (!skip && itemStack.isEmpty());

            RenderUtils.renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);
            if (skip || breaking) {
                continue;
            }
            itemStack.shrink(1);
        }
    }

    private void renderBlockOutlines(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, BlocksPreview preview, float dissolve) {
        renderBlockOutlines(poseStack, multiBufferSource, preview.blockPosStates(), preview.createPreviewInventory(), preview.breaking(), preview.skip(), dissolve);
    }

    private void renderBlockOutlines(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, List<BlockPosState> blockPosStates, PreviewInventory inventory, boolean breaking, boolean skip, float dissolve) {
        if (blockPosStates.isEmpty()) return;

        var firstPos = blockPosStates.get(0).blockPos();
        var secondPos = blockPosStates.get(blockPosStates.size() - 1).blockPos();

        var dispatcher = minecraft.getBlockRenderer();

        for (var blockPosState : blockPosStates) {
            var level = blockPosState.level();
            var blockPos = blockPosState.blockPos();
            var blockState = blockPosState.blockState();
            var item = blockState.getBlock().asItem();
            var itemStack = inventory.findItemStackByItem(item);

            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
            }
            var red = breaking || (!skip && itemStack.isEmpty());

            RenderUtils.renderBlockOutlines(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);
            if (skip || breaking) {
                continue;
            }
            itemStack.shrink(1);
        }
    }

    private void displayBlockPlaceMessage(Player player, BlocksPreview preview, boolean breaking) {
        var volume = preview.size();

        var dimensions = "(";
        if (volume.getX() > 1) dimensions += volume.getX() + "x";
        if (volume.getZ() > 1) dimensions += volume.getZ() + "x";
        if (volume.getY() > 1) dimensions += volume.getY() + "x";
        dimensions = dimensions.substring(0, dimensions.length() - 1);
        if (dimensions.length() > 1) dimensions += ")";

        var blockCounter = "" + ChatFormatting.WHITE + preview.usages().sufficientCount() + ChatFormatting.RESET + (preview.usages().isFilled() ? " " : " + " + ChatFormatting.RED + preview.usages().insufficientCount() + ChatFormatting.RESET + " ") + (preview.usages().totalCount() == 1 ? "block" : "blocks");

        displayActionBarMessage(player, "%s%s%s of %s %s %s".formatted(ChatFormatting.GOLD, BuildModeHelper.getTranslatedModeOptionName(player), ChatFormatting.RESET, breaking ? "breaking" : "placing", blockCounter, dimensions));
    }

    private void displayTracingFailedMessage(Player player) {
        displayActionBarMessage(player, "%s%s%s %s".formatted(ChatFormatting.GOLD, BuildModeHelper.getTranslatedModeOptionName(player), ChatFormatting.RESET, "cannot be traced"));
    }

    private void displayActionBarMessage(Player player, String message) {
        if (BuildModeHelper.getBuildMode(player) == BuildMode.DISABLE) {
            return;
        }
        Effortless.log(player, message, true);
        hasLastMessage = true;
    }

    private void clearActionBarMessage(Player player) {
        if (BuildModeHelper.getBuildMode(player) == BuildMode.DISABLE) {
            return;
        }
        if (hasLastMessage) {
            Effortless.log(player, "", true);
            hasLastMessage = false;
        }
    }

    private void clearPreview() {
        currentPreview = BlocksPreview.EMPTY;
    }

    private void clearPreviewHistory() {
        history.clear();
    }

}
