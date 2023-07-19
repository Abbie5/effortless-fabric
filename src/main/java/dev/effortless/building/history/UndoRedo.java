package dev.effortless.building.history;

import dev.effortless.building.base.Option;

public enum UndoRedo implements Option {
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
