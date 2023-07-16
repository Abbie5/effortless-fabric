package dev.effortless.building.operation;

import dev.effortless.building.Context;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record ItemStackSummary(
        Context context,
        Map<ConsumerGroup, List<ItemStack>> group
) { }

