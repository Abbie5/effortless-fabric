package dev.huskcasaca.effortless.utils;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class InventoryHelper {

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
        for(int itemSlot = 0; itemSlot < inventory.items.size(); ++itemSlot) {
            var itemStack = inventory.items.get(itemSlot);
            if (itemStack.isEmpty() || !itemStack.is(item)) {
                continue;
            }
            return itemSlot;
        }
        return -1;
    }

    public static int findTotalItemsInInventory(Player player, Item item) {
        int total = 0;
        for (var itemStack : player.getInventory().items) {
            if (!itemStack.isEmpty() && itemStack.is(item)) {
                total += itemStack.getCount();
            }
        }
        return total;
    }

    @Deprecated
    public static ItemStack findItemStackInInventory(Player player, Block block) {
        for (ItemStack invStack : player.getInventory().items) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItem &&
                    ((BlockItem) invStack.getItem()).getBlock().equals(block)) {
                return invStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Deprecated
    public static int findTotalBlocksInInventory(Player player, Block block) {
        int total = 0;
        for (ItemStack invStack : player.getInventory().items) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItem &&
                    ((BlockItem) invStack.getItem()).getBlock().equals(block)) {
                total += invStack.getCount();
            }
        }
        return total;
    }
}
