package dev.effortless.building.settings;

import dev.effortless.building.pattern.randomizer.Randomizer;
import dev.effortless.building.pattern.randomizer.Randomizers;

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
                        Randomizers.COBBLESTONE_VARIANT,
                        Randomizers.STONE_BRICK_VARIANT,
                        Randomizers.ORE,
                        Randomizers.DEEPSLATE_ORE,
                        Randomizers.COLORFUL_CARPET,
                        Randomizers.COLORFUL_CONCRETE,
                        Randomizers.COLORFUL_CONCRETE_POWDER,
                        Randomizers.COLORFUL_WOOL,
                        Randomizers.COLORFUL_STAINED_GLASS,
                        Randomizers.COLORFUL_STAINED_GLASS_PANE,
                        Randomizers.COLORFUL_TERRACOTTA,
                        Randomizers.COLORFUL_GLAZED_TERRACOTTA,
                        Randomizers.COLORFUL_SHULKER_BOX,
                        Randomizers.COLORFUL_BED,
                        Randomizers.COLORFUL_BANNER
                )
        );
    }

}
