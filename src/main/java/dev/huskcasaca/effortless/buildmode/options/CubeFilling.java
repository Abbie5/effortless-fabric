package dev.huskcasaca.effortless.buildmode.options;


import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum CubeFilling implements OptionSet.Entry {
    CUBE_FULL("cube_full"),
    CUBE_HOLLOW("cube_hollow"),
    CUBE_SKELETON("cube_skeleton");

    private final String name;

    CubeFilling(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public OptionSet.Entry[] getEntries() {
        return values();
    }
}
