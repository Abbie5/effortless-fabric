package dev.effortless.renderer.preview.result;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.operation.BlockInteractionResult;
import dev.effortless.building.operation.BlockResult;
import dev.effortless.renderer.CustomRenderType;
import dev.effortless.renderer.SuperRenderTypeBuffer;
import dev.effortless.renderer.preview.SurfaceColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;

import java.awt.*;

public class BlockResultRenderer implements ResultRenderer<BlockResult> {

    private static final RandomSource RAND = RandomSource.create();
    private static final float SCALE = 129 / 128f;

    public Color getColor(BlockInteractionResult result) {
        return null;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockResult result) {

        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        var operation = result.operation();
        var level = operation.level();
        var blockPos = operation.blockPos();
        var blockState = operation.blockState();
        if (blockState == null) return;

//            if (item instanceof BlockItem blockItem && itemStack.is(item)) {
//                blockState = blockItem.updateBlockStateFromTag(blockPos, level, itemStack, blockState);
//            }
        var color = getColor(result.result());
        if (color == null) return;

        var renderType = CustomRenderType.solid(color);

        var buffer = ((SuperRenderTypeBuffer) multiBufferSource).getLateBuffer(renderType);

        var model = dispatcher.getBlockModel(blockState);
        var seed = blockState.getSeed(blockPos);

        poseStack.pushPose();

        poseStack.translate(blockPos.getX() - camera.x(), blockPos.getY() - camera.y(), blockPos.getZ() - camera.z());
        poseStack.translate((SCALE - 1) / -2, (SCALE - 1) / -2, (SCALE - 1) / -2);
        poseStack.scale(SCALE, SCALE, SCALE);

        dispatcher.getModelRenderer().tesselateBlock(level, model, blockState, blockPos, poseStack, buffer, false, RAND, seed, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    public static final class Break extends BlockResultRenderer {

        @Override
        public Color getColor(BlockInteractionResult result) {
            return switch (result) {
                case SUCCESS, SUCCESS_PREVIEW, CONSUME -> SurfaceColor.COLOR_RED;
                default -> null;
            };
        }

    }

    public static final class Place extends BlockResultRenderer {

        @Override
        public Color getColor(BlockInteractionResult result) {
            return switch (result) {
                case SUCCESS, SUCCESS_PREVIEW, CONSUME -> SurfaceColor.COLOR_WHITE;
                case FAIL_PLAYER_EMPTY_INV -> SurfaceColor.COLOR_RED;
                default -> null;
            };
        }

    }


}
