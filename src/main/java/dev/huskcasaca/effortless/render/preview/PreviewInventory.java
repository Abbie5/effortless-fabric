package dev.huskcasaca.effortless.render.preview;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PreviewInventory {

    private final List<ItemStack> itemStacks;

    private final Map<Item, ItemStack> cache = new HashMap<>();

    public PreviewInventory(Inventory inventory) {
        this.itemStacks = inventory.items.stream().map(ItemStack::copy).toList();
    }

    public PreviewInventory(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks.stream().map(ItemStack::copy).toList();
    }

    public ItemStack findItemStackByItem(Item item) {
        var last = cache.get(item);
        if (last != null && !last.isEmpty()) {
            return last;
        }
        for (var itemStack : itemStacks) {
            if (itemStack.is(item) && !itemStack.isEmpty()) {
                cache.put(item, itemStack);
                return itemStack;
            }
        }
        cache.put(item, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

}
