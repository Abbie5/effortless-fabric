package dev.effortless.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.effortless.render.RenderTypes;
import dev.effortless.render.SuperRenderTypeBuffer;
import dev.effortless.utils.Iterate;
import dev.effortless.utils.VecHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockClusterOutline extends Outline {

    static Vec3 xyz = new Vec3(-.5, -.5, -.5);
    static Vec3 Xyz = new Vec3(.5, -.5, -.5);
    static Vec3 xYz = new Vec3(-.5, .5, -.5);
    static Vec3 XYz = new Vec3(.5, .5, -.5);
    static Vec3 xyZ = new Vec3(-.5, -.5, .5);
    static Vec3 XyZ = new Vec3(.5, -.5, .5);
    static Vec3 xYZ = new Vec3(-.5, .5, .5);
    static Vec3 XYZ = new Vec3(.5, .5, .5);
    private final Cluster cluster;

    public BlockClusterOutline(Iterable<BlockPos> selection) {
        cluster = new Cluster();
        selection.forEach(cluster::include);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, float pt) {
        cluster.visibleEdges.forEach(edge -> {
            var start = Vec3.atLowerCornerOf(edge.pos);
            var direction = Direction.get(AxisDirection.POSITIVE, edge.axis);
            renderAACuboidLine(poseStack, multiBufferSource, start, Vec3.atLowerCornerOf(edge.pos.relative(direction)));
        });

        var faceTexture = params.faceTexture;
        if (!faceTexture.isPresent())
            return;

        var translucentType = RenderTypes.getOutlineTranslucent(faceTexture.get(), true);
        var builder = ((SuperRenderTypeBuffer) multiBufferSource).getLateBuffer(translucentType);

        cluster.visibleFaces.forEach((face, axisDirection) -> {
            var direction = Direction.get(axisDirection, face.axis);
            var pos = face.pos;
            if (axisDirection == AxisDirection.POSITIVE)
                pos = pos.relative(direction.getOpposite());
            renderBlockFace(poseStack, builder, pos, direction);
        });
    }

    protected void renderBlockFace(PoseStack poseStack, VertexConsumer builder, BlockPos pos, Direction face) {
        var camera = minecraft.gameRenderer.getMainCamera().getPosition();
        var center = VecHelper.getCenterOf(pos);
        var offset = Vec3.atLowerCornerOf(face.getNormal());
        offset = offset.scale(1 / 128d);
        center = center.subtract(camera).add(offset);

        poseStack.pushPose();
        poseStack.translate(center.x, center.y, center.z);

        switch (face) {
            case DOWN:
                putQuad(poseStack, builder, xyz, Xyz, XyZ, xyZ, face);
                break;
            case EAST:
                putQuad(poseStack, builder, XYz, XYZ, XyZ, Xyz, face);
                break;
            case NORTH:
                putQuad(poseStack, builder, xYz, XYz, Xyz, xyz, face);
                break;
            case SOUTH:
                putQuad(poseStack, builder, XYZ, xYZ, xyZ, XyZ, face);
                break;
            case UP:
                putQuad(poseStack, builder, xYZ, XYZ, XYz, xYz, face);
                break;
            case WEST:
                putQuad(poseStack, builder, xYZ, xYz, xyz, xyZ, face);
            default:
                break;
        }

        poseStack.popPose();
    }

    private static class Cluster {

        private final Map<MergeEntry, AxisDirection> visibleFaces;
        private final Set<MergeEntry> visibleEdges;

        public Cluster() {
            visibleEdges = new HashSet<>();
            visibleFaces = new HashMap<>();
        }

        public void include(BlockPos pos) {

            // 6 FACES
            for (var axis : Iterate.axes) {
                var direction = Direction.get(AxisDirection.POSITIVE, axis);
                for (var offset : Iterate.zeroAndOne) {
                    var entry = new MergeEntry(axis, pos.relative(direction, offset));
                    if (visibleFaces.remove(entry) == null)
                        visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
                }
            }

            // 12 EDGES
            for (var axis : Iterate.axes) {
                for (var axis2 : Iterate.axes) {
                    if (axis == axis2)
                        continue;
                    for (var axis3 : Iterate.axes) {
                        if (axis == axis3)
                            continue;
                        if (axis2 == axis3)
                            continue;

                        var direction = Direction.get(AxisDirection.POSITIVE, axis2);
                        var direction2 = Direction.get(AxisDirection.POSITIVE, axis3);

                        for (var offset : Iterate.zeroAndOne) {
                            var entryPos = pos.relative(direction, offset);
                            for (var offset2 : Iterate.zeroAndOne) {
                                entryPos = entryPos.relative(direction2, offset2);
                                var entry = new MergeEntry(axis, entryPos);
                                if (!visibleEdges.remove(entry))
                                    visibleEdges.add(entry);
                            }
                        }
                    }

                    break;
                }
            }

        }

    }

    private static class MergeEntry {

        private final Axis axis;
        private final BlockPos pos;

        public MergeEntry(Axis axis, BlockPos pos) {
            this.axis = axis;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MergeEntry other))
                return false;

            return this.axis == other.axis && this.pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return this.pos.hashCode() * 31 + axis.ordinal();
        }
    }

}