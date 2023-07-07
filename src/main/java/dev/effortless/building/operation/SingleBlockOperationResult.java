package dev.effortless.building.operation;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SingleBlockOperationResult(
        SingleBlockOperation operation,
        InteractionResult result,
        List<ItemStack> inputs, // player consumed
        List<ItemStack> outputs // level dropped
) implements OperationResult<SingleBlockOperationResult> {
}
