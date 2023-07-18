package dev.effortless.renderer.preview.result;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.operation.StructureResult;
import dev.effortless.renderer.OutlineRenderType;
import dev.effortless.renderer.outliner.OutlineRenderer;
import dev.effortless.renderer.preview.OperationRenderer;
import dev.effortless.renderer.preview.SurfaceColor;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

public final class StructureResultRenderer implements ResultRenderer<StructureResult> {

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, StructureResult result) {
        var context = result.operation().context();
        result.children().forEach((result1) -> OperationRenderer.getInstance().renderResult(poseStack, multiBufferSource, result1));

        var cluster = OutlineRenderer.getInstance().showCluster(context.uuid(), result.blockPoses())
                .texture(OutlineRenderType.CHECKERED_THIN_TEXTURE_LOCATION)
                .lightMap(LightTexture.FULL_BLOCK)
                .disableNormals()
                .stroke(1 / 64f);

        switch (context.state()) {
            case IDLE -> {
            }
            case PLACE_BLOCK -> cluster.colored(SurfaceColor.COLOR_WHITE);
            case BREAK_BLOCK -> cluster.colored(SurfaceColor.COLOR_RED);
        }
    }

}
