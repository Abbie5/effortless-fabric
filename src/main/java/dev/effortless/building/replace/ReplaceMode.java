package dev.effortless.building.replace;

import dev.effortless.building.mode.BuildOption;

public enum ReplaceMode implements BuildOption {
    DISABLED("replace_disabled"),
    NORMAL("normal_replace"),
    QUICK("quick_replace");

    private final String name;

    ReplaceMode(String name) {
        this.name = name;
    }

    public String getCommandName() {
        return switch (this) {
            case DISABLED -> "disabled";
            case NORMAL -> "normal";
            case QUICK -> "quick";
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "replace";
    }

    public boolean isReplace() {
        return this != DISABLED;
    }

}
