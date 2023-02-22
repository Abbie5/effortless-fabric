package dev.huskcasaca.effortless.buildmode.options;

import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum RaisedEdge implements OptionSet.Entry {

    RAISE_SHORT_EDGE("raise_short_edge"),
    RAISE_LONG_EDGE("raise_long_edge");

    private final String name;

    RaisedEdge(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RaisedEdge[] getEntries() {
        return values();
    }
}
