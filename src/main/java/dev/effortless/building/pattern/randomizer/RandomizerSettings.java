package dev.effortless.building.pattern.randomizer;

import java.util.Collections;
import java.util.List;

public record RandomizerSettings(
        List<Randomizer> randomizers
) {

    public RandomizerSettings() {
        this(Collections.emptyList());
    }

    public static RandomizerSettings getDefault() {
        return new RandomizerSettings(Collections.emptyList());
    }

    public static RandomizerSettings getSamples() {

        return new RandomizerSettings(
                List.of(
                        Randomizer.COBBLESTONE_VARIANT,
                        Randomizer.STONE_BRICK_VARIANT,
                        Randomizer.ORE,
                        Randomizer.DEEPSLATE_ORE,
                        Randomizer.COLORFUL_CARPET,
                        Randomizer.COLORFUL_CONCRETE,
                        Randomizer.COLORFUL_CONCRETE_POWDER,
                        Randomizer.COLORFUL_WOOL,
                        Randomizer.COLORFUL_STAINED_GLASS,
                        Randomizer.COLORFUL_STAINED_GLASS_PANE,
                        Randomizer.COLORFUL_TERRACOTTA,
                        Randomizer.COLORFUL_GLAZED_TERRACOTTA,
                        Randomizer.COLORFUL_SHULKER_BOX,
                        Randomizer.COLORFUL_BED,
                        Randomizer.COLORFUL_BANNER
                )
        );
    }

}
