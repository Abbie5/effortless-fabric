package dev.effortless.building.pattern.randomizer;

import dev.effortless.building.base.Source;
import net.minecraft.world.item.Item;

import java.util.stream.IntStream;

interface ItemSource extends Source<Item> {

    // getDefault
    static ItemSource createRandom(Randomizer randomizer) {
        return new Random(randomizer);
    }

    static ItemSource createSequence(Randomizer randomizer) {
        return new Sequence(randomizer);
    }

    long getSeed();

    class Random extends org.joml.Random implements ItemSource {

        private final long seed;
        private final Item[] items;

        public Random(Randomizer randomizer) {
            this(randomizer, newSeed() ^ System.nanoTime());
        }

        public Random(Randomizer randomizer, long seed) {
            super(seed);
            this.seed = seed;
            this.items = mapRandomizer(randomizer);
        }

        static Item[] mapRandomizer(Randomizer randomizer) {
            return (Item[]) randomizer.chances().stream().flatMap(holder -> IntStream.range(0, holder.chance()).mapToObj((i) -> holder.content())).toArray();
        }

        @Override
        public Item next() {
            if (items.length == 0) {
                return null;
            }
            return items[nextInt(items.length)];
        }

        @Override
        public long getSeed() {
            return seed;
        }
    }

    class Sequence implements ItemSource {

        private final Item[] items;
        private int index = 0;

        public Sequence(Randomizer randomizer) {
            this.items = Random.mapRandomizer(randomizer);
        }

        @Override
        public Item next() {
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
