package dev.huskcasaca.effortless.randomizer;

import net.minecraft.world.item.Item;

interface ItemSource {

    // getDefault
    static ItemSource random(Randomizer randomizer) {
        return new ItemRandom(randomizer);
    }

    static ItemSource sequence(Randomizer randomizer) {
        return new ItemSource() {

            private final Item[] items = ItemRandom.mapRandomizer(randomizer);
            private int index = 0;

            @Override
            public Item nextItem() {
                if (items.length == 0) {
                    return null;
                }
                return items[index++ % items.length];
            }

            @Override
            public long getSeed() {
                return 0;
            }

        };
    }

    Item nextItem();

    long getSeed();

}
