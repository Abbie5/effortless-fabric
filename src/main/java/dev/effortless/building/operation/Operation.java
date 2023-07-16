package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

import java.awt.*;

public interface Operation<R extends OperationResult<R>> {

    R perform();

    BlockPos getPosition();

    Type getType();

    Renderer<R> getRenderer();

    enum Type {
        WORLD_PLACE_OP,
        WORLD_BREAK_OP,
    }

    interface Renderer<R extends OperationResult<R>> {

        Color COLOR_WHITE = new Color(235, 235, 235);
        Color COLOR_RED = new Color(255, 85, 85);
        Color COLOR_DARK_RED = new Color(170, 0, 0);
        Color COLOR_ORANGE = new Color(255, 200, 0);

        void render(PoseStack poseStack, MultiBufferSource multiBufferSource, R result);
    }

}
