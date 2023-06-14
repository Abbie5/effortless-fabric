package dev.huskcasaca.effortless.building.history;

import dev.huskcasaca.effortless.building.mode.BuildOption;

public enum UndoRedo implements BuildOption {
    UNDO("undo"),
    REDO("redo"),
    ;

    private final String name;

    UndoRedo(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "history";
    }

}
