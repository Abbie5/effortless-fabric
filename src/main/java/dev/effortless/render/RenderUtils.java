package dev.effortless.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

public class RenderUtils {

    public static VertexConsumer beginLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(CustomRenderType.lines());
    }

    public static void endLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    public static VertexConsumer beginPlanes(MultiBufferSource multiBufferSource) {
        return multiBufferSource.getBuffer(CustomRenderType.planes());
    }

    public static void endPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

}
