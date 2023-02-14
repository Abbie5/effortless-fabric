package dev.huskcasaca.effortless.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.huskcasaca.effortless.render.RenderTypes;
import dev.huskcasaca.effortless.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
public class AABBOutline extends Outline {

	protected AABB bb;

	public AABBOutline(AABB bb) {
		this.setBounds(bb);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, float pt) {
		renderBB(poseStack, buffer, bb);
	}

	public void renderBB(PoseStack poseStack, MultiBufferSource buffer, AABB bb) {
		Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		boolean noCull = bb.contains(projectedView);
		bb = bb.inflate(noCull ? -1 / 128d : 1 / 128d);
		noCull |= params.disableCull;

		Vec3 xyz = new Vec3(bb.minX, bb.minY, bb.minZ);
		Vec3 Xyz = new Vec3(bb.maxX, bb.minY, bb.minZ);
		Vec3 xYz = new Vec3(bb.minX, bb.maxY, bb.minZ);
		Vec3 XYz = new Vec3(bb.maxX, bb.maxY, bb.minZ);
		Vec3 xyZ = new Vec3(bb.minX, bb.minY, bb.maxZ);
		Vec3 XyZ = new Vec3(bb.maxX, bb.minY, bb.maxZ);
		Vec3 xYZ = new Vec3(bb.minX, bb.maxY, bb.maxZ);
		Vec3 XYZ = new Vec3(bb.maxX, bb.maxY, bb.maxZ);

		Vec3 start = xyz;
		renderAACuboidLine(poseStack, buffer, start, Xyz);
		renderAACuboidLine(poseStack, buffer, start, xYz);
		renderAACuboidLine(poseStack, buffer, start, xyZ);

		start = XyZ;
		renderAACuboidLine(poseStack, buffer, start, xyZ);
		renderAACuboidLine(poseStack, buffer, start, XYZ);
		renderAACuboidLine(poseStack, buffer, start, Xyz);

		start = XYz;
		renderAACuboidLine(poseStack, buffer, start, xYz);
		renderAACuboidLine(poseStack, buffer, start, Xyz);
		renderAACuboidLine(poseStack, buffer, start, XYZ);

		start = xYZ;
		renderAACuboidLine(poseStack, buffer, start, XYZ);
		renderAACuboidLine(poseStack, buffer, start, xyZ);
		renderAACuboidLine(poseStack, buffer, start, xYz);

		renderFace(poseStack, buffer, Direction.NORTH, xYz, XYz, Xyz, xyz, noCull);
		renderFace(poseStack, buffer, Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, noCull);
		renderFace(poseStack, buffer, Direction.EAST, XYz, XYZ, XyZ, Xyz, noCull);
		renderFace(poseStack, buffer, Direction.WEST, xYZ, xYz, xyz, xyZ, noCull);
		renderFace(poseStack, buffer, Direction.UP, xYZ, XYZ, XYz, xYz, noCull);
		renderFace(poseStack, buffer, Direction.DOWN, xyz, Xyz, XyZ, xyZ, noCull);

	}

	protected void renderFace(PoseStack poseStack, MultiBufferSource buffer, Direction direction, Vec3 p1, Vec3 p2,
		Vec3 p3, Vec3 p4, boolean noCull) {
		if (!params.faceTexture.isPresent())
			return;

		ResourceLocation faceTexture = params.faceTexture.get();
		float alphaBefore = params.alpha;
		params.alpha =
			(direction == params.getHighlightedFace() && params.hightlightedFaceTexture.isPresent()) ? 1 : 0.5f;

		RenderType translucentType = RenderTypes.getOutlineTranslucent(faceTexture, !noCull);
		VertexConsumer builder = ((SuperRenderTypeBuffer) buffer).getLateBuffer(translucentType);

		Axis axis = direction.getAxis();
		Vec3 uDiff = p2.subtract(p1);
		Vec3 vDiff = p4.subtract(p1);
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);
		putQuadUV(poseStack, builder, p1, p2, p3, p4, 0, 0, maxU, maxV, Direction.UP);
		params.alpha = alphaBefore;
	}

	public void setBounds(AABB bb) {
		this.bb = bb;
	}

}