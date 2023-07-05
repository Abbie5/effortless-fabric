package dev.effortless.core.event.client;

import dev.effortless.core.event.api.Event;
import dev.effortless.core.event.api.EventFactory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;

public class ClientPlayerEvent {

    public static final Event<StartAttack> START_ATTACH = EventFactory.createArrayBacked(StartAttack.class, callbacks -> (player) -> {
        for (var callback : callbacks) {
            var result = callback.onStartAttack(player);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    });

    public static final Event<StartUse> START_USE = EventFactory.createArrayBacked(StartUse.class, callbacks -> (player) -> {
        for (var callback : callbacks) {
            var result = callback.onStartUse(player);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    });

    public static final Event<ContinueAttack> CONTINUE_ATTACK = EventFactory.createArrayBacked(ContinueAttack.class, callbacks -> (player) -> {
        for (var callback : callbacks) {
            if (!callback.onContinueAttack(player)) {
                return false;
            }
        }
        return true;
    });

    @FunctionalInterface
    public interface StartAttack {
        InteractionResult onStartAttack(LocalPlayer player);
    }

    @FunctionalInterface
    public interface StartUse {
        InteractionResult onStartUse(LocalPlayer player);
    }

    @FunctionalInterface
    public interface ContinueAttack {
        boolean onContinueAttack(LocalPlayer player);
    }

}