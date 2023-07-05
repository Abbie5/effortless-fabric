package dev.effortless.core.event.client;

import dev.effortless.core.event.api.Event;
import dev.effortless.core.event.api.EventFactory;

public class ClientScreenInputEvent {

    public static final Event<KeyPress> KEY_PRESS_EVENT = EventFactory.createArrayBacked(KeyPress.class, callbacks -> (int key, int scanCode, int action, int modifiers) -> {
        for (KeyPress callback : callbacks) {
            callback.onKeyPress(key, scanCode, action, modifiers);
        }
    });

    @FunctionalInterface
    public interface KeyPress {
        void onKeyPress(int key, int scanCode, int action, int modifiers);
    }

}