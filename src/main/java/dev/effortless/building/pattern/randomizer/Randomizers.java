package dev.effortless.building.pattern.randomizer;

import net.minecraft.world.item.Items;

import static dev.effortless.building.pattern.randomizer.Randomizer.create;

public abstract class Randomizers {

    public static final Randomizer EMPTY = create("");
    public static final Randomizer COBBLESTONE_VARIANT = create(
            "Cobblestone Variant",
            ItemChance.of(Items.COBBLESTONE, 1),
            ItemChance.of(Items.MOSSY_COBBLESTONE, 1)
    );
    public static final Randomizer STONE_BRICK_VARIANT = create(
            "Stone Brick Variant",
            ItemChance.of(Items.STONE_BRICKS, 1),
            ItemChance.of(Items.MOSSY_STONE_BRICKS, 1),
            ItemChance.of(Items.CRACKED_STONE_BRICKS, 1)
    );
    public static final Randomizer ORE = create(
            "Ore",
            ItemChance.of(Items.COAL_ORE, 1),
            ItemChance.of(Items.COPPER_ORE, 1),
            ItemChance.of(Items.LAPIS_ORE, 1),
            ItemChance.of(Items.IRON_ORE, 1),
            ItemChance.of(Items.REDSTONE_ORE, 1),
            ItemChance.of(Items.GOLD_ORE, 1),
            ItemChance.of(Items.DIAMOND_ORE, 1),
            ItemChance.of(Items.EMERALD_ORE, 1)
    );
    public static final Randomizer DEEPSLATE_ORE = create(
            "Deepslate Ore",
            ItemChance.of(Items.DEEPSLATE_COAL_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_COPPER_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_LAPIS_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_IRON_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_REDSTONE_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_GOLD_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_DIAMOND_ORE, 1),
            ItemChance.of(Items.DEEPSLATE_EMERALD_ORE, 1)
    );
    public static final Randomizer COLORFUL_CARPET = create(
            "Colorful Carpet",
            ItemChance.of(Items.WHITE_CARPET, 1),
            ItemChance.of(Items.LIGHT_GRAY_CARPET, 1),
            ItemChance.of(Items.GRAY_CARPET, 1),
            ItemChance.of(Items.BLACK_CARPET, 1),
            ItemChance.of(Items.BROWN_CARPET, 1),
            ItemChance.of(Items.RED_CARPET, 1),
            ItemChance.of(Items.ORANGE_CARPET, 1),
            ItemChance.of(Items.YELLOW_CARPET, 1),
            ItemChance.of(Items.LIME_CARPET, 1),
            ItemChance.of(Items.GREEN_CARPET, 1),
            ItemChance.of(Items.CYAN_CARPET, 1),
            ItemChance.of(Items.LIGHT_BLUE_CARPET, 1),
            ItemChance.of(Items.BLUE_CARPET, 1),
            ItemChance.of(Items.PURPLE_CARPET, 1),
            ItemChance.of(Items.MAGENTA_CARPET, 1),
            ItemChance.of(Items.PINK_CARPET, 1)
    );
    public static final Randomizer COLORFUL_CONCRETE = create(
            "Colorful Concrete",
            ItemChance.of(Items.WHITE_CONCRETE, 1),
            ItemChance.of(Items.LIGHT_GRAY_CONCRETE, 1),
            ItemChance.of(Items.GRAY_CONCRETE, 1),
            ItemChance.of(Items.BLACK_CONCRETE, 1),
            ItemChance.of(Items.BROWN_CONCRETE, 1),
            ItemChance.of(Items.RED_CONCRETE, 1),
            ItemChance.of(Items.ORANGE_CONCRETE, 1),
            ItemChance.of(Items.YELLOW_CONCRETE, 1),
            ItemChance.of(Items.LIME_CONCRETE, 1),
            ItemChance.of(Items.GREEN_CONCRETE, 1),
            ItemChance.of(Items.CYAN_CONCRETE, 1),
            ItemChance.of(Items.LIGHT_BLUE_CONCRETE, 1),
            ItemChance.of(Items.BLUE_CONCRETE, 1),
            ItemChance.of(Items.PURPLE_CONCRETE, 1),
            ItemChance.of(Items.MAGENTA_CONCRETE, 1),
            ItemChance.of(Items.PINK_CONCRETE, 1)
    );
    public static final Randomizer COLORFUL_CONCRETE_POWDER = create(
            "Colorful Concrete Powder",
            ItemChance.of(Items.WHITE_CONCRETE_POWDER, 1),
            ItemChance.of(Items.LIGHT_GRAY_CONCRETE_POWDER, 1),
            ItemChance.of(Items.GRAY_CONCRETE_POWDER, 1),
            ItemChance.of(Items.BLACK_CONCRETE_POWDER, 1),
            ItemChance.of(Items.BROWN_CONCRETE_POWDER, 1),
            ItemChance.of(Items.RED_CONCRETE_POWDER, 1),
            ItemChance.of(Items.ORANGE_CONCRETE_POWDER, 1),
            ItemChance.of(Items.YELLOW_CONCRETE_POWDER, 1),
            ItemChance.of(Items.LIME_CONCRETE_POWDER, 1),
            ItemChance.of(Items.GREEN_CONCRETE_POWDER, 1),
            ItemChance.of(Items.CYAN_CONCRETE_POWDER, 1),
            ItemChance.of(Items.LIGHT_BLUE_CONCRETE_POWDER, 1),
            ItemChance.of(Items.BLUE_CONCRETE_POWDER, 1),
            ItemChance.of(Items.PURPLE_CONCRETE_POWDER, 1),
            ItemChance.of(Items.MAGENTA_CONCRETE_POWDER, 1),
            ItemChance.of(Items.PINK_CONCRETE_POWDER, 1)
    );
    public static final Randomizer COLORFUL_WOOL = create(
            "Colorful Wool",
            ItemChance.of(Items.WHITE_WOOL, 1),
            ItemChance.of(Items.LIGHT_GRAY_WOOL, 1),
            ItemChance.of(Items.GRAY_WOOL, 1),
            ItemChance.of(Items.BLACK_WOOL, 1),
            ItemChance.of(Items.BROWN_WOOL, 1),
            ItemChance.of(Items.RED_WOOL, 1),
            ItemChance.of(Items.ORANGE_WOOL, 1),
            ItemChance.of(Items.YELLOW_WOOL, 1),
            ItemChance.of(Items.LIME_WOOL, 1),
            ItemChance.of(Items.GREEN_WOOL, 1),
            ItemChance.of(Items.CYAN_WOOL, 1),
            ItemChance.of(Items.LIGHT_BLUE_WOOL, 1),
            ItemChance.of(Items.BLUE_WOOL, 1),
            ItemChance.of(Items.PURPLE_WOOL, 1),
            ItemChance.of(Items.MAGENTA_WOOL, 1),
            ItemChance.of(Items.PINK_WOOL, 1)
    );
    public static final Randomizer COLORFUL_STAINED_GLASS = create(
            "Colorful Stained Glass",
            ItemChance.of(Items.WHITE_STAINED_GLASS, 1),
            ItemChance.of(Items.LIGHT_GRAY_STAINED_GLASS, 1),
            ItemChance.of(Items.GRAY_STAINED_GLASS, 1),
            ItemChance.of(Items.BLACK_STAINED_GLASS, 1),
            ItemChance.of(Items.BROWN_STAINED_GLASS, 1),
            ItemChance.of(Items.RED_STAINED_GLASS, 1),
            ItemChance.of(Items.ORANGE_STAINED_GLASS, 1),
            ItemChance.of(Items.YELLOW_STAINED_GLASS, 1),
            ItemChance.of(Items.LIME_STAINED_GLASS, 1),
            ItemChance.of(Items.GREEN_STAINED_GLASS, 1),
            ItemChance.of(Items.CYAN_STAINED_GLASS, 1),
            ItemChance.of(Items.LIGHT_BLUE_STAINED_GLASS, 1),
            ItemChance.of(Items.BLUE_STAINED_GLASS, 1),
            ItemChance.of(Items.PURPLE_STAINED_GLASS, 1),
            ItemChance.of(Items.MAGENTA_STAINED_GLASS, 1),
            ItemChance.of(Items.PINK_STAINED_GLASS, 1)
    );
    public static final Randomizer COLORFUL_STAINED_GLASS_PANE = create(
            "Colorful Stained Glass Pane",
            ItemChance.of(Items.WHITE_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.LIGHT_GRAY_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.GRAY_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.BLACK_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.BROWN_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.RED_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.ORANGE_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.YELLOW_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.LIME_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.GREEN_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.CYAN_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.LIGHT_BLUE_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.BLUE_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.PURPLE_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.MAGENTA_STAINED_GLASS_PANE, 1),
            ItemChance.of(Items.PINK_STAINED_GLASS_PANE, 1)
    );
    public static final Randomizer COLORFUL_TERRACOTTA = create(
            "Colorful Terracotta",
            ItemChance.of(Items.WHITE_TERRACOTTA, 1),
            ItemChance.of(Items.LIGHT_GRAY_TERRACOTTA, 1),
            ItemChance.of(Items.GRAY_TERRACOTTA, 1),
            ItemChance.of(Items.BLACK_TERRACOTTA, 1),
            ItemChance.of(Items.BROWN_TERRACOTTA, 1),
            ItemChance.of(Items.RED_TERRACOTTA, 1),
            ItemChance.of(Items.ORANGE_TERRACOTTA, 1),
            ItemChance.of(Items.YELLOW_TERRACOTTA, 1),
            ItemChance.of(Items.LIME_TERRACOTTA, 1),
            ItemChance.of(Items.GREEN_TERRACOTTA, 1),
            ItemChance.of(Items.CYAN_TERRACOTTA, 1),
            ItemChance.of(Items.LIGHT_BLUE_TERRACOTTA, 1),
            ItemChance.of(Items.BLUE_TERRACOTTA, 1),
            ItemChance.of(Items.PURPLE_TERRACOTTA, 1),
            ItemChance.of(Items.MAGENTA_TERRACOTTA, 1),
            ItemChance.of(Items.PINK_TERRACOTTA, 1)
    );
    public static final Randomizer COLORFUL_GLAZED_TERRACOTTA = create(
            "Colorful Glazed Terracotta",
            ItemChance.of(Items.WHITE_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.LIGHT_GRAY_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.GRAY_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.BLACK_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.BROWN_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.RED_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.ORANGE_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.YELLOW_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.LIME_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.GREEN_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.CYAN_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.LIGHT_BLUE_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.BLUE_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.PURPLE_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.MAGENTA_GLAZED_TERRACOTTA, 1),
            ItemChance.of(Items.PINK_GLAZED_TERRACOTTA, 1)
    );
    public static final Randomizer COLORFUL_SHULKER_BOX = create(
            "Colorful Shulker Box",
            ItemChance.of(Items.WHITE_SHULKER_BOX, 1),
            ItemChance.of(Items.LIGHT_GRAY_SHULKER_BOX, 1),
            ItemChance.of(Items.GRAY_SHULKER_BOX, 1),
            ItemChance.of(Items.BLACK_SHULKER_BOX, 1),
            ItemChance.of(Items.BROWN_SHULKER_BOX, 1),
            ItemChance.of(Items.RED_SHULKER_BOX, 1),
            ItemChance.of(Items.ORANGE_SHULKER_BOX, 1),
            ItemChance.of(Items.YELLOW_SHULKER_BOX, 1),
            ItemChance.of(Items.LIME_SHULKER_BOX, 1),
            ItemChance.of(Items.GREEN_SHULKER_BOX, 1),
            ItemChance.of(Items.CYAN_SHULKER_BOX, 1),
            ItemChance.of(Items.LIGHT_BLUE_SHULKER_BOX, 1),
            ItemChance.of(Items.BLUE_SHULKER_BOX, 1),
            ItemChance.of(Items.PURPLE_SHULKER_BOX, 1),
            ItemChance.of(Items.MAGENTA_SHULKER_BOX, 1),
            ItemChance.of(Items.PINK_SHULKER_BOX, 1)
    );
    public static final Randomizer COLORFUL_BED = create(
            "Colorful Bed",
            ItemChance.of(Items.WHITE_BED, 1),
            ItemChance.of(Items.LIGHT_GRAY_BED, 1),
            ItemChance.of(Items.GRAY_BED, 1),
            ItemChance.of(Items.BLACK_BED, 1),
            ItemChance.of(Items.BROWN_BED, 1),
            ItemChance.of(Items.RED_BED, 1),
            ItemChance.of(Items.ORANGE_BED, 1),
            ItemChance.of(Items.YELLOW_BED, 1),
            ItemChance.of(Items.LIME_BED, 1),
            ItemChance.of(Items.GREEN_BED, 1),
            ItemChance.of(Items.CYAN_BED, 1),
            ItemChance.of(Items.LIGHT_BLUE_BED, 1),
            ItemChance.of(Items.BLUE_BED, 1),
            ItemChance.of(Items.PURPLE_BED, 1),
            ItemChance.of(Items.MAGENTA_BED, 1),
            ItemChance.of(Items.PINK_BED, 1)
    );
    public static final Randomizer COLORFUL_BANNER = create(
            "Colorful Banner",
            ItemChance.of(Items.WHITE_BANNER, 1),
            ItemChance.of(Items.LIGHT_GRAY_BANNER, 1),
            ItemChance.of(Items.GRAY_BANNER, 1),
            ItemChance.of(Items.BLACK_BANNER, 1),
            ItemChance.of(Items.BROWN_BANNER, 1),
            ItemChance.of(Items.RED_BANNER, 1),
            ItemChance.of(Items.ORANGE_BANNER, 1),
            ItemChance.of(Items.YELLOW_BANNER, 1),
            ItemChance.of(Items.LIME_BANNER, 1),
            ItemChance.of(Items.GREEN_BANNER, 1),
            ItemChance.of(Items.CYAN_BANNER, 1),
            ItemChance.of(Items.LIGHT_BLUE_BANNER, 1),
            ItemChance.of(Items.BLUE_BANNER, 1),
            ItemChance.of(Items.PURPLE_BANNER, 1),
            ItemChance.of(Items.MAGENTA_BANNER, 1),
            ItemChance.of(Items.PINK_BANNER, 1)
    );



}
