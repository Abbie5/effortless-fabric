package dev.effortless.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class InventoryHelper {

    public static int findTotalItemsInInventory(Player player, Item item) {
        int total = 0;
        for (var itemStack : player.getInventory().items) {
            if (!itemStack.isEmpty() && itemStack.is(item)) {
                total += itemStack.getCount();
            }
        }
        return total;
    }

    //    @Deprecated
    public static ItemStack findItemStackInInventory(Player player, Block block) {
        for (ItemStack invStack : player.getInventory().items) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItem &&
                    ((BlockItem) invStack.getItem()).getBlock().equals(block)) {
                return invStack;
            }
        }
        return ItemStack.EMPTY;
    }

    //    @Deprecated
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
