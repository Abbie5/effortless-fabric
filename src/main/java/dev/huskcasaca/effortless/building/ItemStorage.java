package dev.huskcasaca.effortless.building;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemStorage {


    ItemStack findByStack(ItemStack stack);

    ItemStack findByItem(Item item);

    boolean consume(ItemStack stack);

}
