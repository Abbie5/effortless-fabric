package dev.huskcasaca.effortless.core.event.lifecycle;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientTickEvents {

    public static final Event<StartTick> START_CLIENT_TICK = new Event<>() {
        @Override
        public void register(StartTick listener) {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.register(listener::onStartTick);
        }
    };

    public static final Event<EndTick> END_CLIENT_TICK = new Event<>() {
        @Override
        public void register(EndTick listener) {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(listener::onEndTick);
        }
    };

    public static final Event<StartWorldTick> START_WORLD_TICK = new Event<>() {
        @Override
        public void register(StartWorldTick listener) {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_WORLD_TICK.register(listener::onStartWorldTick);
        }
    };

    public static final Event<EndWorldTick> END_WORLD_TICK = new Event<>() {
        @Override
        public void register(EndWorldTick listener) {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_WORLD_TICK.register(listener::onEndWorldTick);
        }
    };


    @FunctionalInterface
    public interface StartTick {
        void onStartTick(Minecraft minecraft);
    }

    @FunctionalInterface
    public interface EndTick {
        void onEndTick(Minecraft minecraft);
    }

    @FunctionalInterface
    public interface StartWorldTick {
        void onStartWorldTick(ClientLevel level);
    }

    @FunctionalInterface
    public interface EndWorldTick {
        void onEndWorldTick(ClientLevel level);
    }


}
