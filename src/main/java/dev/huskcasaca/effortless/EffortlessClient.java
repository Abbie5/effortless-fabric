package dev.huskcasaca.effortless;

import dev.huskcasaca.effortless.event.ClientEvents;
import dev.huskcasaca.effortless.event.InputEvents;
import dev.huskcasaca.effortless.keybinding.Keys;
import dev.huskcasaca.effortless.network.Packets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EffortlessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keys.register();

        Packets.registerClient();

        ClientEvents.register();
        InputEvents.register();
    }

}
