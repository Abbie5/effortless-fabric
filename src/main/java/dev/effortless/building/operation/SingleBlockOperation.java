package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import dev.effortless.render.CustomRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;

public abstract class SingleBlockOperation implements Operation<SingleBlockOperationResult> {

    protected static boolean canInteract(Level level, Player player, BlockPos blockPos) {
        var gameMode = level.isClientSide() ? Minecraft.getInstance().gameMode.getPlayerMode() : ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        return !player.blockActionRestricted(level, blockPos, gameMode);
    }

    public abstract Level level();

    public abstract Player player();

    public abstract Storage storage();

    public abstract Context context();

    // for preview
    public abstract BlockPos blockPos();

    public abstract BlockState blockState();

    public abstract ItemStack inputItemStack();

    public abstract ItemStack outputItemStack();

    public static class DefaultRenderer implements Renderer<SingleBlockOperationResult> {

        private static final DefaultRenderer INSTANCE = new DefaultRenderer();
        private static final RandomSource RAND = RandomSource.create();
        private static final float SCALE = 1 / 128f;
        private static final Color COLOR_RED = new Color(16733525);
        private static final Color COLOR_WHITE = new Color(255, 255, 255);

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }

        protected Color getColor(InteractionResult result) {
            return result.consumesAction() ? COLOR_WHITE : COLOR_RED;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, SingleBlockOperationResult result) {

            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            var operation = result.operation();
            var level = operation.level();
            var blockPos = operation.blockPos();
            var blockState = operation.blockState();

//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }
            var renderType = CustomRenderType.solid(getColor(result.result()));

            var buffer = multiBufferSource.getBuffer(renderType);

            var model = dispatcher.getBlockModel(blockState);
            var seed = blockState.getSeed(blockPos);

            poseStack.pushPose();

            poseStack.translate(blockPos.getX() - camera.x(), blockPos.getY() - camera.y(), blockPos.getZ() - camera.z());
            poseStack.translate(SCALE / -2, SCALE / -2, SCALE / -2);
            poseStack.scale( 1 + SCALE, 1 + SCALE, 1 + SCALE);

            dispatcher.getModelRenderer().tesselateBlock(level, model, blockState, blockPos, poseStack, buffer, false, RAND, seed, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }

}
