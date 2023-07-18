package dev.effortless.renderer.preview.result;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.operation.OperationResult;
import net.minecraft.client.renderer.MultiBufferSource;

public interface ResultRenderer<R extends OperationResult<R>> {
    void render(PoseStack poseStack, MultiBufferSource multiBufferSource, R result);
}
