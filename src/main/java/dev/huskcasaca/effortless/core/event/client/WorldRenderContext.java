package dev.huskcasaca.effortless.core.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public record WorldRenderContext(
        PoseStack poseStack,
        Camera camera
) {
}
