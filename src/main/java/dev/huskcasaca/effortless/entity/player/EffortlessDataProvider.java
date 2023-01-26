package dev.huskcasaca.effortless.entity.player;

public interface EffortlessDataProvider {

    ModeSettings getModeSettings();

    void setModeSettings(ModeSettings modeSettings);

    ModifierSettings getModifierSettings();

    void setModifierSettings(ModifierSettings modifierSettings);

    ReachSettings getReachSettings();

    void setReachSettings(ReachSettings reachSettings);

}
