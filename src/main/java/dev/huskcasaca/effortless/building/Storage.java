package dev.huskcasaca.effortless.building;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Storage {

    static Storage createTemp(List<ItemStack> itemStacks) {
        return new Storage() {

            private final List<ItemStack> itemStacksTemp = itemStacks.stream().map(ItemStack::copy).toList();

            private final Map<Item, ItemStack> cache = new HashMap<>();

            @Override
            public ItemStack findByItem(Item item) {
                var last = cache.get(item);
                if (last != null && !last.isEmpty()) {
                    return last;
                }
                for (var itemStack : itemStacksTemp) {
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
                ;
    }

    ItemStack findByStack(ItemStack stack);

    ItemStack findByItem(Item item);

    boolean consume(ItemStack stack);

}
