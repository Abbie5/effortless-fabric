package dev.effortless.building.operation;

import dev.effortless.building.Context;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record ItemStackSummary(
        Context context,
        Map<ItemStackType, List<ItemStack>> inventoryConsumed, // player consumed
        Map<ItemStackType, List<ItemStack>> inventoryPicked, // player picked
        Map<ItemStackType, List<ItemStack>> levelConsumed, // level placed
        Map<ItemStackType, List<ItemStack>> levelDropped // level dropped
) {
}
