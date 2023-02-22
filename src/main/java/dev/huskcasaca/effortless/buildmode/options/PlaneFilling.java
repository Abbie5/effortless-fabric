package dev.huskcasaca.effortless.buildmode.options;

import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum PlaneFilling implements OptionSet.Entry {

    PLANE_FULL("plane_full"),
    PLANE_HOLLOW("plane_hollow");

    private final String name;

    PlaneFilling(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlaneFilling[] getEntries() {
        return values();
    }
}
