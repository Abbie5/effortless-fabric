package dev.huskcasaca.effortless.core.event.client;


import dev.huskcasaca.effortless.core.event.api.Event;
import dev.huskcasaca.effortless.core.event.api.EventFactory;
import net.minecraft.client.player.LocalPlayer;


public class ClientPlayerEvent {

    public static final Event<StartAttach> START_ATTACH = EventFactory.createArrayBacked(StartAttach.class, callbacks -> (player) -> {
        for (StartAttach callback : callbacks) {
            var result = callback.onStartAttach(player);
            if (result != null) {
                return result;
            };
        }
        return false;
    });

    public static final Event<ContinueAttack> CONTINUE_ATTACK = EventFactory.createArrayBacked(ContinueAttack.class, callbacks -> (player) -> {
        for (ContinueAttack callback : callbacks) {
            if (!callback.onContinueAttack(player)) {
                return false;
            };
        }
        return false;
    });

    public static final Event<StartUseItem> START_USE_ITEM = EventFactory.createArrayBacked(StartUseItem.class, callbacks -> (player) -> {
        for (StartUseItem callback : callbacks) {
            if (!callback.onStartUseItem(player)) {
                return false;
            };
        }
        return false;
    });

    @FunctionalInterface
    public interface StartAttach {
        Boolean onStartAttach(LocalPlayer player);
    }

    @FunctionalInterface
    public interface ContinueAttack {
        Boolean onContinueAttack(LocalPlayer player);
    }

    @FunctionalInterface
    public interface StartUseItem {
        Boolean onStartUseItem(LocalPlayer player);
    }

}