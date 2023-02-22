package dev.huskcasaca.effortless.buildmode.options;

import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum PlaneOrientation implements OptionSet.Entry {
    HORIZONTAL("face_horizontal"),
    VERTICAL("face_vertical");

    private final String name;

    PlaneOrientation(String name) {
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
