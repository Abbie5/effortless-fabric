package dev.effortless.building.operation;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record BlockResult(
        BlockOperation operation,
        BlockInteractionResult result,
        List<ItemStack> inputs,
        List<ItemStack> outputs) implements OperationResult<BlockResult> {
}