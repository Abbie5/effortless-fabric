package dev.effortless.core.event.client;

import dev.effortless.core.event.api.Event;
import dev.effortless.core.event.api.EventFactory;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class ClientScreenEvent {

    public static final Event<ScreenOpening> OPENING = EventFactory.createArrayBacked(ScreenOpening.class, callbacks -> (screen) -> {
        for (ScreenOpening callback : callbacks) {
            callback.onScreenOpening(screen);
        }
    });

    public static final Event<ScreenClosing> CLOSING = EventFactory.createArrayBacked(ScreenClosing.class, callbacks -> (screen) -> {
        for (ScreenClosing callback : callbacks) {
            callback.onScreenClosing(screen);
        }
    });

    @FunctionalInterface
    public interface ScreenOpening {
        void onScreenOpening(@Nullable Screen screen);
    }

    @FunctionalInterface
    public interface ScreenClosing {
        void onScreenClosing(@Nullable Screen screen);
    }

}