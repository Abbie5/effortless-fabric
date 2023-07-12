package dev.effortless;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.effortless.screen.config.EffortlessSettingsScreen;

public class EffortlessModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EffortlessSettingsScreen::createConfigScreen;
    }
}