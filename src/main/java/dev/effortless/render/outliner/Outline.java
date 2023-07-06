package dev.effortless.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.effortless.render.RenderTypes;
import dev.effortless.utils.AngleHelper;
import dev.effortless.utils.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

public abstract class Outline {

    protected OutlineParams params;
    protected Matrix3f transformNormals; // TODO: not used?
    protected Minecraft minecraft;

    public Outline() {
        params = new OutlineParams();
        minecraft = Minecraft.getInstance();
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource multiBufferSource, float pt);

    public void tick() {
    }

    public OutlineParams getParams() {
        return params;
    }

    public void renderCuboidLine(PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 start, Vec3 end) {
        var diff = end.subtract(start);
        var hAngle = AngleHelper.deg(Mth.atan2(diff.x, diff.z));
        var hDistance = (float) diff.multiply(1, 0, 1)
                .length();
        var vAngle = AngleHelper.deg(Mth.atan2(hDistance, diff.y)) - 90;
        poseStack.pushPose();
        // TODO: 27/1/23
        poseStack.translate(start.x(), start.y(), start.z());
//			.rotateY(hAngle).rotateX(vAngle);
        renderAACuboidLine(poseStack, multiBufferSource, Vec3.ZERO, new Vec3(0, 0, diff.length()));
        poseStack.popPose();
    }

    public void renderAACuboidLine(PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 start, Vec3 end) {
        var camera = minecraft.gameRenderer.getMainCamera().getPosition();
        start = start.subtract(camera);
        end = end.subtract(camera);
        var lineWidth = params.getLineWidth();
        if (lineWidth == 0)
            return;

        var builder = multiBufferSource.getBuffer(RenderTypes.getOutlineSolid());

        var diff = end.subtract(start);
        if (diff.x + diff.y + diff.z < 0) {
            var temp = start;
            start = end;
            end = temp;
            diff = diff.scale(-1);
        }

        var extension = diff.normalize()
                .scale(lineWidth / 2);
        var plane = VecHelper.axisAlingedPlaneOf(diff);
        var face = Direction.getNearest(diff.x, diff.y, diff.z);
        var axis = face.getAxis();

        start = start.subtract(extension);
        end = end.add(extension);
        plane = plane.scale(lineWidth / 2);

        var a1 = plane.add(start);
        var b1 = plane.add(end);
        plane = VecHelper.rotate(plane, -90, axis);
        var a2 = plane.add(start);
        var b2 = plane.add(end);
        plane = VecHelper.rotate(plane, -90, axis);
        var a3 = plane.add(start);
        var b3 = plane.add(end);
        plane = VecHelper.rotate(plane, -90, axis);
        var a4 = plane.add(start);
        var b4 = plane.add(end);

        if (params.disableNormals) {
            face = Direction.UP;
            putQuad(poseStack, builder, b4, b3, b2, b1, face);
            putQuad(poseStack, builder, a1, a2, a3, a4, face);
            putQuad(poseStack, builder, a1, b1, b2, a2, face);
            putQuad(poseStack, builder, a2, b2, b3, a3, face);
            putQuad(poseStack, builder, a3, b3, b4, a4, face);
            putQuad(poseStack, builder, a4, b4, b1, a1, face);
            return;
        }

        putQuad(poseStack, builder, b4, b3, b2, b1, face);
        putQuad(poseStack, builder, a1, a2, a3, a4, face.getOpposite());
        var vec = a1.subtract(a4);
        face = Direction.getNearest(vec.x, vec.y, vec.z);
        putQuad(poseStack, builder, a1, b1, b2, a2, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.x, vec.y, vec.z);
        putQuad(poseStack, builder, a2, b2, b3, a3, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.x, vec.y, vec.z);
        putQuad(poseStack, builder, a3, b3, b4, a4, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.x, vec.y, vec.z);
        putQuad(poseStack, builder, a4, b4, b1, a1, face);
    }

    public void putQuad(PoseStack poseStack, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4,
                        Direction normal) {
        putQuadUV(poseStack, builder, v1, v2, v3, v4, 0, 0, 1, 1, normal);
    }

    public void putQuadUV(PoseStack poseStack, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float minU,
                          float minV, float maxU, float maxV, Direction normal) {
        putVertex(poseStack, builder, v1, minU, minV, normal);
        putVertex(poseStack, builder, v2, maxU, minV, normal);
        putVertex(poseStack, builder, v3, maxU, maxV, normal);
        putVertex(poseStack, builder, v4, minU, maxV, normal);
    }

    protected void putVertex(PoseStack poseStack, VertexConsumer builder, Vec3 pos, float u, float v, Direction normal) {
        putVertex(poseStack.last(), builder, (float) pos.x, (float) pos.y, (float) pos.z, u, v, normal);
    }

    protected void putVertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, Direction normal) {
        var rgb = params.rgb;
        if (transformNormals == null) {
            transformNormals = pose.normal();
        }

        var xOffset = 0;
        var yOffset = 0;
        var zOffset = 0;

        if (normal != null) {
            xOffset = normal.getStepX();
            yOffset = normal.getStepY();
            zOffset = normal.getStepZ();
        }

        builder.vertex(pose.pose(), x, y, z)
                .color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (rgb.getAlpha() * params.alpha))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(params.lightMap)
                .normal(pose.normal(), xOffset, yOffset, zOffset)
                .endVertex();

        transformNormals = null;
    }

    public static class OutlineParams {
        protected Optional<ResourceLocation> faceTexture;
        protected Optional<ResourceLocation> hightlightedFaceTexture;
        protected Direction highlightedFace;
        protected boolean fadeLineWidth;
        protected boolean disableCull;
        protected boolean disableNormals;
        protected float alpha;
        protected int lightMap;
        protected Color rgb;
        private float lineWidth;

        public OutlineParams() {
            faceTexture = hightlightedFaceTexture = Optional.empty();
            alpha = 1;
            lineWidth = 1 / 32f;
            fadeLineWidth = true;
            rgb = Color.WHITE;
            lightMap = LightTexture.FULL_BRIGHT;
        }

        // builder

        public OutlineParams colored(float r, float g, float b, float a) {
            rgb = new Color(r, g, b, a);
            return this;
        }

        public OutlineParams colored(int red, int green, int blue, int alpha) {
            rgb = new Color(red, green, blue, alpha);
            return this;
        }

        public OutlineParams colored(int color) {
            rgb = new Color(color, false);
            return this;
        }

        public OutlineParams colored(Color c) {
            rgb = c;
            return this;
        }

        public OutlineParams lightMap(int light) {
            lightMap = light;
            return this;
        }

        public OutlineParams stroke(float width) {
            this.lineWidth = width;
            return this;
        }

        public OutlineParams texture(ResourceLocation resourceLocation) {
            this.faceTexture = Optional.ofNullable(resourceLocation);
            return this;
        }

        public OutlineParams clearTextures() {
            return this.textures(null, null);
        }

        public OutlineParams textures(ResourceLocation texture, ResourceLocation highlightTexture) {
            this.faceTexture = Optional.ofNullable(texture);
            this.hightlightedFaceTexture = Optional.ofNullable(highlightTexture);
            return this;
        }

        public OutlineParams highlightFace(@Nullable Direction face) {
            highlightedFace = face;
            return this;
        }

        public OutlineParams disableNormals() {
            disableNormals = true;
            return this;
        }

        public OutlineParams disableCull() {
            disableCull = true;
            return this;
        }

        // getter

        public float getLineWidth() {
            return fadeLineWidth ? alpha * lineWidth : lineWidth;
        }

        public Direction getHighlightedFace() {
            return highlightedFace;
        }

    }

}
