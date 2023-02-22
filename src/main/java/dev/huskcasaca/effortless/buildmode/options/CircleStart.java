package dev.huskcasaca.effortless.buildmode.options;

import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum CircleStart implements OptionSet.Entry {
    CIRCLE_START_CORNER("circle_start_corner"),
    CIRCLE_START_CENTER("circle_start_center"),
    ;

    private final String name;

    CircleStart(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OptionSet.Entry[] getEntries() {
        return values();
    }
}
