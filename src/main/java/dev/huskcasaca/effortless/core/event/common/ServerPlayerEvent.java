package dev.huskcasaca.effortless.core.event.common;

import dev.huskcasaca.effortless.core.event.api.Event;
import dev.huskcasaca.effortless.core.event.api.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ServerPlayerEvent {

    public static final Event<ChangeDimension> CHANGE_DIMENSION = EventFactory.createArrayBacked(ChangeDimension.class, callbacks -> (ServerLevel level, ServerPlayer player) -> {
        for (ChangeDimension callback : callbacks) {
            callback.onChangeDimension(level, player);
        }
    });

    public static final Event<Clone> CLONE = EventFactory.createArrayBacked(Clone.class, callbacks -> (ServerPlayer player, ServerPlayer oldPlayer, boolean alive) -> {
        for (Clone callback : callbacks) {
            callback.onClone(player, oldPlayer, alive);
        }
    });

    @FunctionalInterface
    public interface ChangeDimension {
        void onChangeDimension(ServerLevel level, ServerPlayer player);
    }

    @FunctionalInterface
    public interface Clone {
        void onClone(ServerPlayer player, ServerPlayer oldPlayer, boolean alive);
    }

}