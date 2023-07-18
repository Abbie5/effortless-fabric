package dev.effortless.building.operation;

import net.minecraft.core.BlockPos;

public interface Operation<R extends OperationResult<R>> {

    R perform();

    BlockPos getPosition();

    OperationType getType();

    boolean isPreview();

}
