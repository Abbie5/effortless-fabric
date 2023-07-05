package dev.effortless.building.settings;

import dev.effortless.building.mode.BuildOption;

public enum SettingType implements BuildOption {
    MODE_SETTINGS("mode_settings"),
    ;

    private final String name;

    SettingType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "settings";
    }

}
