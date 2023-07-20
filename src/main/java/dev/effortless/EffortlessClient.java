package dev.effortless;

import dev.effortless.event.ClientEvents;
import dev.effortless.event.InputEvents;
import dev.effortless.keybinding.Keys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EffortlessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keys.register();

        ClientEvents.register();
        InputEvents.register();
    }

}
