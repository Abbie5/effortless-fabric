package dev.effortless.renderer.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.renderer.OutlineRenderType;
import dev.effortless.renderer.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBOutline extends Outline {

    protected AABB bb;

    public AABBOutline(AABB bb) {
        this.setBounds(bb);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, float pt) {
        renderBB(poseStack, multiBufferSource, bb);
    }

    public void renderBB(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB bb) {
        var projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var noCull = bb.contains(projectedView);
        bb = bb.inflate(noCull ? -1 / 128d : 1 / 128d);
        noCull |= params.disableCull;

        var xyz = new Vec3(bb.minX, bb.minY, bb.minZ);
        var Xyz = new Vec3(bb.maxX, bb.minY, bb.minZ);
        var xYz = new Vec3(bb.minX, bb.maxY, bb.minZ);
        var XYz = new Vec3(bb.maxX, bb.maxY, bb.minZ);
        var xyZ = new Vec3(bb.minX, bb.minY, bb.maxZ);
        var XyZ = new Vec3(bb.maxX, bb.minY, bb.maxZ);
        var xYZ = new Vec3(bb.minX, bb.maxY, bb.maxZ);
        var XYZ = new Vec3(bb.maxX, bb.maxY, bb.maxZ);

        var start = xyz;
        renderAACuboidLine(poseStack, multiBufferSource, start, Xyz);
        renderAACuboidLine(poseStack, multiBufferSource, start, xYz);
        renderAACuboidLine(poseStack, multiBufferSource, start, xyZ);

        start = XyZ;
        renderAACuboidLine(poseStack, multiBufferSource, start, xyZ);
        renderAACuboidLine(poseStack, multiBufferSource, start, XYZ);
        renderAACuboidLine(poseStack, multiBufferSource, start, Xyz);

        start = XYz;
        renderAACuboidLine(poseStack, multiBufferSource, start, xYz);
        renderAACuboidLine(poseStack, multiBufferSource, start, Xyz);
        renderAACuboidLine(poseStack, multiBufferSource, start, XYZ);

        start = xYZ;
        renderAACuboidLine(poseStack, multiBufferSource, start, XYZ);
        renderAACuboidLine(poseStack, multiBufferSource, start, xyZ);
        renderAACuboidLine(poseStack, multiBufferSource, start, xYz);

        renderFace(poseStack, multiBufferSource, Direction.NORTH, xYz, XYz, Xyz, xyz, noCull);
        renderFace(poseStack, multiBufferSource, Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, noCull);
        renderFace(poseStack, multiBufferSource, Direction.EAST, XYz, XYZ, XyZ, Xyz, noCull);
        renderFace(poseStack, multiBufferSource, Direction.WEST, xYZ, xYz, xyz, xyZ, noCull);
        renderFace(poseStack, multiBufferSource, Direction.UP, xYZ, XYZ, XYz, xYz, noCull);
        renderFace(poseStack, multiBufferSource, Direction.DOWN, xyz, Xyz, XyZ, xyZ, noCull);

    }

    protected void renderFace(PoseStack poseStack, MultiBufferSource multiBufferSource, Direction direction, Vec3 p1, Vec3 p2,
                              Vec3 p3, Vec3 p4, boolean noCull) {
        if (!params.faceTexture.isPresent())
            return;

        var faceTexture = params.faceTexture.get();
        var alphaBefore = params.alpha;
        params.alpha =
                (direction == params.getHighlightedFace() && params.hightlightedFaceTexture.isPresent()) ? 1 : 0.5f;

        var translucentType = OutlineRenderType.outlineTranslucent(faceTexture, !noCull);
        var builder = ((SuperRenderTypeBuffer) multiBufferSource).getLateBuffer(translucentType);

        var axis = direction.getAxis();
        var uDiff = p2.subtract(p1);
        var vDiff = p4.subtract(p1);
        var maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
        var maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);
        putQuadUV(poseStack, builder, p1, p2, p3, p4, 0, 0, maxU, maxV, Direction.UP);
        params.alpha = alphaBefore;
    }

    public void setBounds(AABB bb) {
        this.bb = bb;
    }

}
