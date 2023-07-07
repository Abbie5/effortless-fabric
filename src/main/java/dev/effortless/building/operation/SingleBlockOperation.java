package dev.effortless.building.operation;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.Context;
import dev.effortless.building.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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

    public static final class DefaultRenderer implements Renderer<SingleBlockOperationResult> {

        public static final float SCALE = 1 / 128f;
        private static final DefaultRenderer INSTANCE = new DefaultRenderer();
        private static final RandomSource RAND = RandomSource.create();

        public static DefaultRenderer getInstance() {
            return INSTANCE;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, SingleBlockOperationResult result) {

            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            var operation = result.operation();
            var level = operation.level();
            var blockPos = operation.blockPos();
            var blockState = operation.blockState();
            var buffer = multiBufferSource.getBuffer(RenderType.solid());

//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }

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
