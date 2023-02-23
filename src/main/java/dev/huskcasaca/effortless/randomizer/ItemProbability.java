package dev.huskcasaca.effortless.randomizer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemProbability(
        Item item,
        int count
) {

    public ItemProbability withCount(int count) {
        return new ItemProbability(item, count);
    }

    public ItemStack singleItemStack() {
        return new ItemStack(item, 1);
    }

}
