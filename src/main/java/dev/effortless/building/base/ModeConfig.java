package dev.effortless.building.base;

import dev.effortless.building.mode.BuildMode;

// removed
public record ModeConfig(
        BuildMode buildMode,
        boolean enableMagnet
) {

    public ModeConfig() {
        this(BuildMode.DISABLED, false);
    }

}