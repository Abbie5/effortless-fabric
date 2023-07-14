package dev.effortless.building;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Storage {

    static Storage createTemp(List<ItemStack> itemStacks) {
        return new Storage() {

            private final List<ItemStack> itemStacksTemp = itemStacks.stream().map(ItemStack::copy).toList();

            private final Map<Item, ItemStack> cache = new HashMap<>();

            @Override
            public Optional<ItemStack> findByItem(Item item) {
                var last = cache.get(item);
                if (last != null && !last.isEmpty()) {
                    return Optional.of(last);
                }
                for (var itemStack : itemStacksTemp) {
                    if (itemStack.is(item) && !itemStack.isEmpty()) {
                        cache.put(item, itemStack);
                        return Optional.of(itemStack);
                    }
                }
                cache.put(item, ItemStack.EMPTY);
                return Optional.empty();
            }

            @Override
            public Optional<ItemStack> findByStack(ItemStack itemStack) {
                var result = findByItem(itemStack.getItem());
                if (result.isPresent() && ItemStack.isSameItemSameTags(result.get(), itemStack)) {
                    return result;
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public boolean consume(ItemStack itemStack) {
                var found = findByStack(itemStack);
                if (found.isEmpty()) {
                    return false;
                }
                if (itemStack.getCount() > found.get().getCount()) {
                    return false;
                }
                found.get().shrink(itemStack.getCount());
                return true;
            }
        }
                ;
    }

    Optional<ItemStack> findByStack(ItemStack stack);

    Optional<ItemStack> findByItem(Item item);

    boolean consume(ItemStack stack);

}
