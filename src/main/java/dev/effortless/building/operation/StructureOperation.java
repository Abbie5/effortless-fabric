package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.Context;
import dev.effortless.render.RenderTypes;
import dev.effortless.render.outliner.OutlineRenderer;
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

        private static final Color PLACING_COLOR = new Color(0.92f, 0.92f, 0.92f, 1f);
        private static final Color BREAKING_COLOR = new Color(0.95f, 0f, 0f, 1f);

        private static final DefaultRenderer INSTANCE = new DefaultRenderer();

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }

        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, StructureOperationResult result) {
            if (!result.type().isSuccess()) return;
            var context = result.operation().context();

            result.result().forEach((result1) -> result1.render(poseStack, multiBufferSource));

            var cluster = OutlineRenderer.getInstance().showCluster(context.uuid(), result.blockPoses())
                    .texture(RenderTypes.CHECKERED_THIN_TEXTURE_LOCATION)
                    .stroke(1 / 64f)
                    .disableNormals();

            switch (context.state()) {
                case IDLE -> {
                }
                case PLACING -> cluster.colored(PLACING_COLOR);
                case BREAKING -> cluster.colored(BREAKING_COLOR);
            }

//            if (!preview.isEmpty() && soundTime < getGameTime() && !BlocksPreview.arePreviewSizeEqual(preview, currentPreview)) {
//                soundTime = getGameTime();
//                var soundType = preview.blockPosStates().get(0).blockState().getSoundType();
//                player.getLevel().playSound(player, player.blockPosition(), context.isBreaking() ? soundType.getBreakSound() : soundType.getPlaceSound(), SoundSource.BLOCKS, 0.3f, 0.8f);
//            }
        }

    }

}
