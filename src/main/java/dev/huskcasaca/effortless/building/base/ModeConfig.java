package dev.huskcasaca.effortless.building.base;

import dev.huskcasaca.effortless.building.mode.BuildMode;

// removed
public record ModeConfig(
        BuildMode buildMode,
        boolean enableMagnet
) {

    public ModeConfig() {
        this(BuildMode.DISABLED, false);
    }

}