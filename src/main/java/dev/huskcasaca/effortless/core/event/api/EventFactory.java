package dev.huskcasaca.effortless.core.event.api;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class EventFactory {

    public static void invalidate() {
        EventFactoryImpl.invalidate();
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        return EventFactoryImpl.createArrayBacked(type, invokerFactory);
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
        return createArrayBacked(type, listeners -> {
            if (listeners.length == 0) {
                return emptyInvoker;
            } else if (listeners.length == 1) {
                return listeners[0];
            } else {
                return invokerFactory.apply(listeners);
            }
        });
    }

    public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, ResourceLocation... defaultPhases) {
        EventFactoryImpl.ensureContainsDefault(defaultPhases);
        EventFactoryImpl.ensureNoDuplicates(defaultPhases);

        var event = createArrayBacked(type, invokerFactory);

        for (var i = 1; i < defaultPhases.length; ++i) {
            event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
        }

        return event;
    }

    public static String getHandlerName(Object handler) {
        return handler.getClass().getName();
    }
}
