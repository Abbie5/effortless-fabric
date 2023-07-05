package dev.effortless.building.mode;

import java.util.Collections;
import java.util.List;

public record ModeSettings(
        List<BuildMode> modes
) {

    public ModeSettings() {
        this(Collections.emptyList());
    }

    public static ModeSettings getDefault() {
        return new ModeSettings(Collections.emptyList());
    }

    public static ModeSettings getSamples() {

        return new ModeSettings(
                List.of(
                )
        );
    }

}
