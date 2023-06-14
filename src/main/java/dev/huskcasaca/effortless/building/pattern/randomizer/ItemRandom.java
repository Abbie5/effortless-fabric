package dev.huskcasaca.effortless.building.pattern.randomizer;

import net.minecraft.world.item.Item;
import org.joml.Random;

import java.util.stream.IntStream;


public class ItemRandom extends Random implements ItemSource {

    private final long seed;
    private final Item[] items;

    public ItemRandom(Randomizer randomizer) {
        this(randomizer, newSeed() ^ System.nanoTime());
    }

    public ItemRandom(Randomizer randomizer, long seed) {
        super(seed);
        this.seed = seed;
        this.items = mapRandomizer(randomizer);
    }

    static Item[] mapRandomizer(Randomizer randomizer) {
        return (Item[]) randomizer.holders().stream().flatMap(holder -> IntStream.range(0, holder.count()).mapToObj((i) -> holder.item())).toArray();
    }

    @Override
    public Item nextItem() {
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
