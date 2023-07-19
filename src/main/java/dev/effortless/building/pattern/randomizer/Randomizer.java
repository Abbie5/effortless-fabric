package dev.effortless.building.pattern.randomizer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Randomizer {

    static Randomizer create(String name, Collection<ItemChance> chances) {
        return new Randomizer() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Collection<ItemChance> chances() {
                return Collections.unmodifiableCollection(chances);
            }
        };
    }

    static Randomizer create(String name, ItemChance... chanceMap) {
        return create(name, List.of(chanceMap));
    }

    String name();

    Collection<ItemChance> chances();

    default ItemSource asRandomSource() {
        return ItemSource.createRandom(this);
    }

    default ItemSource asSequenceSource() {
        return ItemSource.createSequence(this);
    }

}
