package dev.huskcasaca.effortless.building.pattern;

import java.util.Collections;
import java.util.List;

public record PatternSettings(
        List<Pattern> patterns
) {

    public PatternSettings() {
        this(Collections.emptyList());
    }

    public static PatternSettings getDefault() {
        return new PatternSettings(Collections.emptyList());
    }

    public static PatternSettings getSamples() {

        return new PatternSettings(
                List.of(
                )
        );
    }

}
