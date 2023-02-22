package dev.huskcasaca.effortless.buildmode.options;

import dev.huskcasaca.effortless.screen.radial.OptionSet;

public enum Operation implements OptionSet.Entry {
    UNDO("undo"),
    REDO("redo"),
    SETTINGS("settings"),
    REPLACE("replace"),
    ;

    private final String name;

    Operation(String name) {
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
