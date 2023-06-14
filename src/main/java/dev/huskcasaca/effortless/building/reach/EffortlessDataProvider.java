package dev.huskcasaca.effortless.building.reach;

import dev.huskcasaca.effortless.building.base.ModeConfig;
import dev.huskcasaca.effortless.building.base.ModifierConfig;

public interface EffortlessDataProvider {

    ModeConfig getModeSettings();

    void setModeSettings(ModeConfig modeSettings);

    ModifierConfig getModifierSettings();

    void setModifierSettings(ModifierConfig modifierSettings);

    ReachConfig getReachSettings();

    void setReachSettings(ReachConfig reachConfig);

}
