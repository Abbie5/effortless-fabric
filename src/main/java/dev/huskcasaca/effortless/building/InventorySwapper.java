package dev.huskcasaca.effortless.building;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

public final class InventorySwapper {
    private final Inventory inventory;
    private final Item item;
    private int lastSlot = -1;

    public InventorySwapper(
            Inventory inventory,
            // TODO: 25/6/23 use ItemPredicate
            Item item
    ) {
        this.inventory = Objects.requireNonNull(inventory);
        this.item = item;
    }

    public static boolean swapSlot(Inventory inventory, int slot) {
        if (slot == -1) return false;
        if (slot == inventory.selected) return true;
        var selected = inventory.items.get(inventory.selected);
        inventory.items.set(inventory.selected, inventory.items.get(slot));
        inventory.items.set(slot, selected);
        return true;
    }

    public static int findItemSlot(Inventory inventory, Item item) {
        if (inventory.getSelected().is(item) && !inventory.getSelected().isEmpty()) {
            return inventory.selected;
        }
        for (int itemSlot = 0; itemSlot < inventory.items.size(); ++itemSlot) {
            var itemStack = inventory.items.get(itemSlot);
            if (itemStack.isEmpty() || !itemStack.is(item)) {
                continue;
            }
            return itemSlot;
        }
        return -1;
    }

    public Item item() {
        return item;
    }

    public boolean swapSelected() {
        lastSlot = findItemSlot(inventory, item);
        return swapSlot(inventory, lastSlot);
    }

    public void restoreSelected() {
        swapSlot(inventory, lastSlot);
        lastSlot = -1;
    }

    public void consume(Consumer<ItemStack> consumer) {
        if (swapSelected()) {
            consumer.accept(inventory.getSelected());
            restoreSelected();
        }
    }



}
