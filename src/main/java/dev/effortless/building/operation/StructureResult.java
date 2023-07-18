package dev.effortless.building.operation;

import dev.effortless.building.TracingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;

public record StructureResult(
        StructureOperation operation,
        TracingResult result,
        List<BlockResult> children
) implements OperationResult<StructureResult> {

    public Vec3i size() {
        if (operation().context().blockHitResults().isEmpty()) {
            return Vec3i.ZERO;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (var hitResult : operation().context().blockHitResults()) {
            var blockPos = hitResult.getBlockPos();
            if (blockPos.getX() < minX) minX = blockPos.getX();
            if (blockPos.getX() > maxX) maxX = blockPos.getX();
            if (blockPos.getY() < minY) minY = blockPos.getY();
            if (blockPos.getY() > maxY) maxY = blockPos.getY();
            if (blockPos.getZ() < minZ) minZ = blockPos.getZ();
            if (blockPos.getZ() > maxZ) maxZ = blockPos.getZ();
        }

        return new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
    }

    public boolean isEmpty() {
        return size() == Vec3i.ZERO;
    }

    public Iterable<BlockPos> blockPoses() {
        return children().stream().map((p) -> p.operation().getPosition()).toList();
    }

}
