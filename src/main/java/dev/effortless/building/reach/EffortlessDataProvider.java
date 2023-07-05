package dev.effortless.building.reach;

import dev.effortless.building.base.ModeConfig;
import dev.effortless.building.base.ModifierConfig;

public interface EffortlessDataProvider {

    ModeConfig getModeSettings();

    void setModeSettings(ModeConfig modeSettings);

    ModifierConfig getModifierSettings();

    void setModifierSettings(ModifierConfig modifierSettings);

    ReachConfig getReachSettings();

    void setReachSettings(ReachConfig reachConfig);

}
