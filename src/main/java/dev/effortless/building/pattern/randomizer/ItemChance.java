package dev.effortless.building.pattern.randomizer;

import dev.effortless.building.base.Chance;
import net.minecraft.world.item.Item;

public interface ItemChance extends Chance<Item> {

    static ItemChance of(Item content, int chance) {
        return new Impl(content, chance);
    }

    record Impl(
            Item content,
            int chance
    ) implements ItemChance {

    }
}
