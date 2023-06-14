package dev.huskcasaca.effortless.core.event.common;

import dev.huskcasaca.effortless.core.event.api.Event;
import dev.huskcasaca.effortless.core.event.api.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public class ServerPlayerListEvent {

    public static final Event<Login> LOGIN = EventFactory.createArrayBacked(Login.class, callbacks -> (ServerPlayer player) -> {
        for (Login callback : callbacks) {
            callback.onLogin(player);
        }
    });

    public static final Event<Logout> LOGOUT = EventFactory.createArrayBacked(Logout.class, callbacks -> (ServerPlayer player) -> {
        for (Logout callback : callbacks) {
            callback.onLogout(player);
        }
    });

    public static final Event<Respawn> RESPAWN = EventFactory.createArrayBacked(Respawn.class, callbacks -> (ServerPlayer player) -> {
        for (Respawn callback : callbacks) {
            callback.onRespawn(player);
        }
    });

    @FunctionalInterface
    public interface Login {
        void onLogin(ServerPlayer player);
    }

    @FunctionalInterface
    public interface Logout {
        void onLogout(ServerPlayer player);
    }

    @FunctionalInterface
    public interface Respawn {
        void onRespawn(ServerPlayer player);
    }


}