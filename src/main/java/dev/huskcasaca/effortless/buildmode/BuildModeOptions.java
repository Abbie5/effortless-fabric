package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.options.*;
import dev.huskcasaca.effortless.screen.radial.OptionSet;
import net.minecraft.network.chat.Component;

public enum BuildModeOptions implements OptionSet {
    UNDO_REDO("undo_redo", Operation.REDO, Operation.UNDO),
    SETTINGS("settings", Operation.REPLACE, Operation.SETTINGS),

    CIRCLE_START("circle_start", CircleStart.values()),
    CUBE_FILLING("cube_filling", CubeFilling.values()),
    ORIENTATION("plane_orientation", PlaneOrientation.values()),
    PLANE_FILLING("plane_filling", PlaneFilling.values()),
    RAISED_EDGE("raised_edge", RaisedEdge.values()),
    ;

    private final String name;
    private final OptionSet.Entry[] entries;

    <T extends OptionSet.Entry> BuildModeOptions(String name, T... defaultEntries) {
        this.name = name;
        this.entries = defaultEntries;
    }

    public String getName() {
        return name;
    }

    public String getNameKey() {
        return String.join(".", Effortless.MOD_ID, "option", name);
    }

    @Override
    public Component getComponentName() {
        return Component.translatable(getNameKey());
    }

    // TODO: 18/2/23  
    @Override
    public Entry[] getEntries() {
        return entries;
    }

}
