package dev.huskcasaca.effortless.render.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.building.operation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class OperationPreviewRenderer {

    private static final OperationPreviewRenderer INSTANCE = new OperationPreviewRenderer(Minecraft.getInstance());
    private final Minecraft minecraft;

    private List<Operation.Result<?>> currentOpResults = new ArrayList<>();
    private final List<Operation.Result<?>> historyOpResults = new ArrayList<>();

    public OperationPreviewRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public static OperationPreviewRenderer getInstance() {
        return INSTANCE;
    }

    public void putOpResult(Operation.Result<?> preview) {
        currentOpResults.add(preview);
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource) {
        var player = minecraft.player;
//        if (!shouldRenderBlockPreviews(player)) {
//            currentPreview = null;
//            return;
//        }
        renderStructurePreview(poseStack, multiBufferSource, player);
        renderStructurePreviewHistory(poseStack, multiBufferSource, player);
    }

    private void renderStructurePreview(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {

        currentOpResults.forEach(preview -> preview.render(poseStack, multiBufferSource));
        currentOpResults = new ArrayList<>();

//        if (context.isBuilding()) {
//            showMessage(player, context, result);
//        } else {
//            clearMessage(player);
//        }

    }

    private void renderStructurePreviewHistory(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Player player) {
//        if (!shouldRenderBlockPreviews(player)) {
//            history.clear();
//            return;
//        }

        // TODO: 26/5/23
//        history.forEach(preview -> renderStructureShader(poseStack, multiBufferSource, preview));
        // expire
        // TODO: 26/5/23
//        history.removeIf(preview -> preview.time() + preview.dissolveSize() * PreviewConfig.shaderDissolveTimeMultiplier() < getGameTime());
    }


//    private final List<StructureOperation.Result> history = new ArrayList<>();
//    private final EffortlessBuilder builder = EffortlessBuilder.getInstance();
//    private int soundTime = 0;

//    private final ConfigManager configManager = ConfigManager.getInstance();
//    private StructureOperation.Result currentPreview;

//    private boolean shouldRenderBlockPreviews(Player player) {
//        return configManager.getConfig().getPreviewConfig().isAlwaysShowBlockPreview();
//    }
//
//    public void saveCurrentPreview() {
//        saveCurrentPreview(currentPreview);
//    }
//
//    public void saveCurrentPreview(StructureOperation.Result preview) {
//        if (shouldRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
////            history.add(preview);
//        }
//    }
//
//    public void saveCurrentBreakPreview() {
//        saveCurrentBreakPreview(currentPreview);
//    }
//
//    public void saveCurrentBreakPreview(StructureOperation.Result preview) {
//        if (shouldRenderBlockPreviews(minecraft.player) && !preview.isEmpty()) {
////            history.add(preview);
//        }
//    }
//
//    @Deprecated
//    public void saveCurrentPreview(List<BlockPos> coordinates, List<BlockState> blockStates, BlockPos firstPos, BlockPos secondPos) {
//        // no-op
//    }
//
//    @Deprecated
//    public void saveCurrentBreakPreview(List<BlockPos> coordinates, List<BlockState> blockStates, BlockPos firstPos, BlockPos secondPos) {
//        // no-op
//    }


}
