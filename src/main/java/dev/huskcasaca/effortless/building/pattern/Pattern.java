package dev.huskcasaca.effortless.building.pattern;

import dev.huskcasaca.effortless.building.pattern.modifier.Modifier;

import java.util.List;

public record Pattern(
        String name,
        List<Modifier> holders
) {

//    public static final Pattern EMPTY = create("");
//    private static Pattern create(String name, Map.Entry<Item, Integer>... entries) {
//        var holders = new ArrayList<ItemProbability>() {
//        };
//        for (var entry : entries) {
//            holders.add(new ItemProbability(entry.getKey(), entry.getValue()));
//        }
//        return new Pattern(name, Collections.unmodifiableList(holders));
//    }

    public boolean isEmpty() {
        return holders.isEmpty();
    }

//    public ItemSource createRandomSource() {
//        return new ItemRandom(this);
//    }


}
