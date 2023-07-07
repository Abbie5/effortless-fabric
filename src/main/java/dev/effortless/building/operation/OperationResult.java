package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface OperationResult<O extends OperationResult<O>> {

    Operation<O> operation();

    default void render(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        operation().getRenderer().render(poseStack, multiBufferSource, (O) this);
    }

}
