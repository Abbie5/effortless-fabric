package dev.huskcasaca.effortless;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.huskcasaca.effortless.screen.config.EffortlessSettingsScreen;

public class EffortlessModmenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EffortlessSettingsScreen::createConfigScreen;
    }
}
