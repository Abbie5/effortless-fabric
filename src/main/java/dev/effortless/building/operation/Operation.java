package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public interface Operation<R extends OperationResult<R>> {

    R perform();

    BlockPos getPosition();

    Type getType();

    Renderer<R> getRenderer();

    enum Type {
        WORLD_PLACE_OP,
        WORLD_BREAK_OP,
        WORLD_PERFORM_OP,
    }

    interface Renderer<R extends OperationResult<R>> {
        void render(PoseStack poseStack, MultiBufferSource multiBufferSource, R result);
    }

}
