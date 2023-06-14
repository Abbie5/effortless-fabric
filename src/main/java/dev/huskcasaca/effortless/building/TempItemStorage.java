package dev.huskcasaca.effortless.building;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempItemStorage implements ItemStorage {

    private final List<ItemStack> itemStacks;

    private final Map<Item, ItemStack> cache = new HashMap<>();

    public TempItemStorage(Inventory inventory) {
        this.itemStacks = inventory.items.stream().map(ItemStack::copy).toList();
    }

    public TempItemStorage(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks.stream().map(ItemStack::copy).toList();
    }

    @Override
    public ItemStack findByItem(Item item) {
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

    @Override
    public ItemStack findByStack(ItemStack itemStack) {
        var result = findByItem(itemStack.getItem());
        if (ItemStack.isSameItemSameTags(result, itemStack)) {
            return result;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean consume(ItemStack itemStack) {
        var found = findByStack(itemStack);
        if (found.isEmpty()) {
            return false;
        }
        if (itemStack.getCount() > found.getCount()) {
            return false;
        }
        found.shrink(itemStack.getCount());
        return true;
    }
}
