package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.Context;
import dev.effortless.renderer.OutlineRenderType;
import dev.effortless.renderer.outliner.OutlineRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;

public abstract class StructureOperation implements Operation<StructureOperationResult> {

    private static void sortOnDistanceToPlayer(List<SingleBlockOperation> blockPosStates, Player player) {
        blockPosStates.sort((lpl, rpl) -> {
            // -1 for less than, 1 for greater than, 0 for equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rpl.blockPos()).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public abstract Level level();

    public abstract Player player();

    public abstract Context context();

    // for preview

    public DefaultRenderer getRenderer() {
        return DefaultRenderer.getInstance();
    }

    public static final class DefaultRenderer implements Renderer<StructureOperationResult> {

        private static final Color COLOR_WHITE = new Color(0.82f, 0.82f, 0.82f, 1f);
        private static final Color COLOR_RED = new Color(0.95f, 0f, 0f, 1f);

        private static final DefaultRenderer INSTANCE = new DefaultRenderer();

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }

        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, StructureOperationResult result) {
            if (!result.result().isSuccess()) return;
            var context = result.operation().context();

            result.children().forEach((result1) -> result1.render(poseStack, multiBufferSource));

            var cluster = OutlineRenderer.getInstance().showCluster(context.uuid(), result.blockPoses())
                    .texture(OutlineRenderType.CHECKERED_THIN_TEXTURE_LOCATION)
                    .lightMap(LightTexture.FULL_BLOCK)
                    .disableNormals()
                    .stroke(1 / 64f);

            switch (context.state()) {
                case IDLE -> {
                }
                case PLACE_BLOCK -> cluster.colored(COLOR_WHITE);
                case BREAK_BLOCK -> cluster.colored(COLOR_RED);
            }
        }

    }

}
