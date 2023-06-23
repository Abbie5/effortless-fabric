package dev.huskcasaca.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public interface Operation<R extends Operation.Result<R>> {

    R perform();

    BlockPos getPosition();

    Type getType();

    Renderer<R> getRenderer();

    enum Type {
        WORLD_PLACE_OP,
        WORLD_BREAK_OP,
        WORLD_PERFORM_OP,
    }

    @FunctionalInterface
    interface Renderer<R extends Result<R>> {
        void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, R result);
    }

    interface Result<O extends Result<O>> {

        Operation<O> operation();

        default void render(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource) {
            operation().getRenderer().render(poseStack, multiBufferSource, (O) this);
        }


    }
}
