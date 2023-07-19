package dev.effortless.building.settings;

import dev.effortless.building.mode.BuildMode;

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
