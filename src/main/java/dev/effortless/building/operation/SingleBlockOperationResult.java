package dev.effortless.building.operation;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SingleBlockOperationResult(
        SingleBlockOperation operation,
        InteractionResult result,
        List<ItemStack> inventoryConsumed, // player consumed
        List<ItemStack> inventoryPicked, // player picked
        List<ItemStack> levelConsumed, // level placed
        List<ItemStack> levelDropped // level dropped
) implements OperationResult<SingleBlockOperationResult> {
}
