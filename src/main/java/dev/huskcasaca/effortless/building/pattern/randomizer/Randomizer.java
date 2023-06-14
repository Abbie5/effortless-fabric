package dev.huskcasaca.effortless.building.pattern.randomizer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public record Randomizer(
        String name,
        List<ItemProbability> holders
) {

    public static final Randomizer EMPTY = create("");
    public static final Randomizer COBBLESTONE_VARIANT = create(
            "Cobblestone Variant",
            entry(Items.COBBLESTONE, 1),
            entry(Items.MOSSY_COBBLESTONE, 1)
    );
    public static final Randomizer STONE_BRICK_VARIANT = create(
            "Stone Brick Variant",
            entry(Items.STONE_BRICKS, 1),
            entry(Items.MOSSY_STONE_BRICKS, 1),
            entry(Items.CRACKED_STONE_BRICKS, 1)
    );
    public static final Randomizer ORE = create(
            "Ore",
            entry(Items.COAL_ORE, 1),
            entry(Items.COPPER_ORE, 1),
            entry(Items.LAPIS_ORE, 1),
            entry(Items.IRON_ORE, 1),
            entry(Items.REDSTONE_ORE, 1),
            entry(Items.GOLD_ORE, 1),
            entry(Items.DIAMOND_ORE, 1),
            entry(Items.EMERALD_ORE, 1)
    );
    public static final Randomizer DEEPSLATE_ORE = create(
            "Deepslate Ore",
            entry(Items.DEEPSLATE_COAL_ORE, 1),
            entry(Items.DEEPSLATE_COPPER_ORE, 1),
            entry(Items.DEEPSLATE_LAPIS_ORE, 1),
            entry(Items.DEEPSLATE_IRON_ORE, 1),
            entry(Items.DEEPSLATE_REDSTONE_ORE, 1),
            entry(Items.DEEPSLATE_GOLD_ORE, 1),
            entry(Items.DEEPSLATE_DIAMOND_ORE, 1),
            entry(Items.DEEPSLATE_EMERALD_ORE, 1)
    );
    public static final Randomizer COLORFUL_CARPET = create(
            "Colorful Carpet",
            entry(Items.WHITE_CARPET, 1),
            entry(Items.LIGHT_GRAY_CARPET, 1),
            entry(Items.GRAY_CARPET, 1),
            entry(Items.BLACK_CARPET, 1),
            entry(Items.BROWN_CARPET, 1),
            entry(Items.RED_CARPET, 1),
            entry(Items.ORANGE_CARPET, 1),
            entry(Items.YELLOW_CARPET, 1),
            entry(Items.LIME_CARPET, 1),
            entry(Items.GREEN_CARPET, 1),
            entry(Items.CYAN_CARPET, 1),
            entry(Items.LIGHT_BLUE_CARPET, 1),
            entry(Items.BLUE_CARPET, 1),
            entry(Items.PURPLE_CARPET, 1),
            entry(Items.MAGENTA_CARPET, 1),
            entry(Items.PINK_CARPET, 1)
    );
    public static final Randomizer COLORFUL_CONCRETE = create(
            "Colorful Concrete",
            entry(Items.WHITE_CONCRETE, 1),
            entry(Items.LIGHT_GRAY_CONCRETE, 1),
            entry(Items.GRAY_CONCRETE, 1),
            entry(Items.BLACK_CONCRETE, 1),
            entry(Items.BROWN_CONCRETE, 1),
            entry(Items.RED_CONCRETE, 1),
            entry(Items.ORANGE_CONCRETE, 1),
            entry(Items.YELLOW_CONCRETE, 1),
            entry(Items.LIME_CONCRETE, 1),
            entry(Items.GREEN_CONCRETE, 1),
            entry(Items.CYAN_CONCRETE, 1),
            entry(Items.LIGHT_BLUE_CONCRETE, 1),
            entry(Items.BLUE_CONCRETE, 1),
            entry(Items.PURPLE_CONCRETE, 1),
            entry(Items.MAGENTA_CONCRETE, 1),
            entry(Items.PINK_CONCRETE, 1)
    );
    public static final Randomizer COLORFUL_CONCRETE_POWDER = create(
            "Colorful Concrete Powder",
            entry(Items.WHITE_CONCRETE_POWDER, 1),
            entry(Items.LIGHT_GRAY_CONCRETE_POWDER, 1),
            entry(Items.GRAY_CONCRETE_POWDER, 1),
            entry(Items.BLACK_CONCRETE_POWDER, 1),
            entry(Items.BROWN_CONCRETE_POWDER, 1),
            entry(Items.RED_CONCRETE_POWDER, 1),
            entry(Items.ORANGE_CONCRETE_POWDER, 1),
            entry(Items.YELLOW_CONCRETE_POWDER, 1),
            entry(Items.LIME_CONCRETE_POWDER, 1),
            entry(Items.GREEN_CONCRETE_POWDER, 1),
            entry(Items.CYAN_CONCRETE_POWDER, 1),
            entry(Items.LIGHT_BLUE_CONCRETE_POWDER, 1),
            entry(Items.BLUE_CONCRETE_POWDER, 1),
            entry(Items.PURPLE_CONCRETE_POWDER, 1),
            entry(Items.MAGENTA_CONCRETE_POWDER, 1),
            entry(Items.PINK_CONCRETE_POWDER, 1)
    );
    public static final Randomizer COLORFUL_WOOL = create(
            "Colorful Wool",
            entry(Items.WHITE_WOOL, 1),
            entry(Items.LIGHT_GRAY_WOOL, 1),
            entry(Items.GRAY_WOOL, 1),
            entry(Items.BLACK_WOOL, 1),
            entry(Items.BROWN_WOOL, 1),
            entry(Items.RED_WOOL, 1),
            entry(Items.ORANGE_WOOL, 1),
            entry(Items.YELLOW_WOOL, 1),
            entry(Items.LIME_WOOL, 1),
            entry(Items.GREEN_WOOL, 1),
            entry(Items.CYAN_WOOL, 1),
            entry(Items.LIGHT_BLUE_WOOL, 1),
            entry(Items.BLUE_WOOL, 1),
            entry(Items.PURPLE_WOOL, 1),
            entry(Items.MAGENTA_WOOL, 1),
            entry(Items.PINK_WOOL, 1)
    );
    public static final Randomizer COLORFUL_STAINED_GLASS = create(
            "Colorful Stained Glass",
            entry(Items.WHITE_STAINED_GLASS, 1),
            entry(Items.LIGHT_GRAY_STAINED_GLASS, 1),
            entry(Items.GRAY_STAINED_GLASS, 1),
            entry(Items.BLACK_STAINED_GLASS, 1),
            entry(Items.BROWN_STAINED_GLASS, 1),
            entry(Items.RED_STAINED_GLASS, 1),
            entry(Items.ORANGE_STAINED_GLASS, 1),
            entry(Items.YELLOW_STAINED_GLASS, 1),
            entry(Items.LIME_STAINED_GLASS, 1),
            entry(Items.GREEN_STAINED_GLASS, 1),
            entry(Items.CYAN_STAINED_GLASS, 1),
            entry(Items.LIGHT_BLUE_STAINED_GLASS, 1),
            entry(Items.BLUE_STAINED_GLASS, 1),
            entry(Items.PURPLE_STAINED_GLASS, 1),
            entry(Items.MAGENTA_STAINED_GLASS, 1),
            entry(Items.PINK_STAINED_GLASS, 1)
    );
    public static final Randomizer COLORFUL_STAINED_GLASS_PANE = create(
            "Colorful Stained Glass Pane",
            entry(Items.WHITE_STAINED_GLASS_PANE, 1),
            entry(Items.LIGHT_GRAY_STAINED_GLASS_PANE, 1),
            entry(Items.GRAY_STAINED_GLASS_PANE, 1),
            entry(Items.BLACK_STAINED_GLASS_PANE, 1),
            entry(Items.BROWN_STAINED_GLASS_PANE, 1),
            entry(Items.RED_STAINED_GLASS_PANE, 1),
            entry(Items.ORANGE_STAINED_GLASS_PANE, 1),
            entry(Items.YELLOW_STAINED_GLASS_PANE, 1),
            entry(Items.LIME_STAINED_GLASS_PANE, 1),
            entry(Items.GREEN_STAINED_GLASS_PANE, 1),
            entry(Items.CYAN_STAINED_GLASS_PANE, 1),
            entry(Items.LIGHT_BLUE_STAINED_GLASS_PANE, 1),
            entry(Items.BLUE_STAINED_GLASS_PANE, 1),
            entry(Items.PURPLE_STAINED_GLASS_PANE, 1),
            entry(Items.MAGENTA_STAINED_GLASS_PANE, 1),
            entry(Items.PINK_STAINED_GLASS_PANE, 1)
    );
    public static final Randomizer COLORFUL_TERRACOTTA = create(
            "Colorful Terracotta",
            entry(Items.WHITE_TERRACOTTA, 1),
            entry(Items.LIGHT_GRAY_TERRACOTTA, 1),
            entry(Items.GRAY_TERRACOTTA, 1),
            entry(Items.BLACK_TERRACOTTA, 1),
            entry(Items.BROWN_TERRACOTTA, 1),
            entry(Items.RED_TERRACOTTA, 1),
            entry(Items.ORANGE_TERRACOTTA, 1),
            entry(Items.YELLOW_TERRACOTTA, 1),
            entry(Items.LIME_TERRACOTTA, 1),
            entry(Items.GREEN_TERRACOTTA, 1),
            entry(Items.CYAN_TERRACOTTA, 1),
            entry(Items.LIGHT_BLUE_TERRACOTTA, 1),
            entry(Items.BLUE_TERRACOTTA, 1),
            entry(Items.PURPLE_TERRACOTTA, 1),
            entry(Items.MAGENTA_TERRACOTTA, 1),
            entry(Items.PINK_TERRACOTTA, 1)
    );
    public static final Randomizer COLORFUL_GLAZED_TERRACOTTA = create(
            "Colorful Glazed Terracotta",
            entry(Items.WHITE_GLAZED_TERRACOTTA, 1),
            entry(Items.LIGHT_GRAY_GLAZED_TERRACOTTA, 1),
            entry(Items.GRAY_GLAZED_TERRACOTTA, 1),
            entry(Items.BLACK_GLAZED_TERRACOTTA, 1),
            entry(Items.BROWN_GLAZED_TERRACOTTA, 1),
            entry(Items.RED_GLAZED_TERRACOTTA, 1),
            entry(Items.ORANGE_GLAZED_TERRACOTTA, 1),
            entry(Items.YELLOW_GLAZED_TERRACOTTA, 1),
            entry(Items.LIME_GLAZED_TERRACOTTA, 1),
            entry(Items.GREEN_GLAZED_TERRACOTTA, 1),
            entry(Items.CYAN_GLAZED_TERRACOTTA, 1),
            entry(Items.LIGHT_BLUE_GLAZED_TERRACOTTA, 1),
            entry(Items.BLUE_GLAZED_TERRACOTTA, 1),
            entry(Items.PURPLE_GLAZED_TERRACOTTA, 1),
            entry(Items.MAGENTA_GLAZED_TERRACOTTA, 1),
            entry(Items.PINK_GLAZED_TERRACOTTA, 1)
    );
    public static final Randomizer COLORFUL_SHULKER_BOX = create(
            "Colorful Shulker Box",
            entry(Items.WHITE_SHULKER_BOX, 1),
            entry(Items.LIGHT_GRAY_SHULKER_BOX, 1),
            entry(Items.GRAY_SHULKER_BOX, 1),
            entry(Items.BLACK_SHULKER_BOX, 1),
            entry(Items.BROWN_SHULKER_BOX, 1),
            entry(Items.RED_SHULKER_BOX, 1),
            entry(Items.ORANGE_SHULKER_BOX, 1),
            entry(Items.YELLOW_SHULKER_BOX, 1),
            entry(Items.LIME_SHULKER_BOX, 1),
            entry(Items.GREEN_SHULKER_BOX, 1),
            entry(Items.CYAN_SHULKER_BOX, 1),
            entry(Items.LIGHT_BLUE_SHULKER_BOX, 1),
            entry(Items.BLUE_SHULKER_BOX, 1),
            entry(Items.PURPLE_SHULKER_BOX, 1),
            entry(Items.MAGENTA_SHULKER_BOX, 1),
            entry(Items.PINK_SHULKER_BOX, 1)
    );
    public static final Randomizer COLORFUL_BED = create(
            "Colorful Bed",
            entry(Items.WHITE_BED, 1),
            entry(Items.LIGHT_GRAY_BED, 1),
            entry(Items.GRAY_BED, 1),
            entry(Items.BLACK_BED, 1),
            entry(Items.BROWN_BED, 1),
            entry(Items.RED_BED, 1),
            entry(Items.ORANGE_BED, 1),
            entry(Items.YELLOW_BED, 1),
            entry(Items.LIME_BED, 1),
            entry(Items.GREEN_BED, 1),
            entry(Items.CYAN_BED, 1),
            entry(Items.LIGHT_BLUE_BED, 1),
            entry(Items.BLUE_BED, 1),
            entry(Items.PURPLE_BED, 1),
            entry(Items.MAGENTA_BED, 1),
            entry(Items.PINK_BED, 1)
    );
    public static final Randomizer COLORFUL_BANNER = create(
            "Colorful Banner",
            entry(Items.WHITE_BANNER, 1),
            entry(Items.LIGHT_GRAY_BANNER, 1),
            entry(Items.GRAY_BANNER, 1),
            entry(Items.BLACK_BANNER, 1),
            entry(Items.BROWN_BANNER, 1),
            entry(Items.RED_BANNER, 1),
            entry(Items.ORANGE_BANNER, 1),
            entry(Items.YELLOW_BANNER, 1),
            entry(Items.LIME_BANNER, 1),
            entry(Items.GREEN_BANNER, 1),
            entry(Items.CYAN_BANNER, 1),
            entry(Items.LIGHT_BLUE_BANNER, 1),
            entry(Items.BLUE_BANNER, 1),
            entry(Items.PURPLE_BANNER, 1),
            entry(Items.MAGENTA_BANNER, 1),
            entry(Items.PINK_BANNER, 1)
    );

    @SafeVarargs
    private static Randomizer create(String name, Map.Entry<Item, Integer>... entries) {
        var holders = new ArrayList<ItemProbability>() {
        };
        for (var entry : entries) {
            holders.add(new ItemProbability(entry.getKey(), entry.getValue()));
        }
        return new Randomizer(name, Collections.unmodifiableList(holders));
    }

    public boolean isEmpty() {
        return holders.isEmpty();
    }

    public ItemSource createRandomSource() {
        return new ItemRandom(this);
    }


}
