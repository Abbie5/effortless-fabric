package dev.effortless.core.event.api;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

class ArrayBackedEvent<T> extends Event<T> {

    private final Function<T[], T> invokerFactory;
    private final Object lock = new Object();
    private final Map<ResourceLocation, EventPhaseData<T>> phases = new LinkedHashMap<>();
    private final List<EventPhaseData<T>> sortedPhases = new ArrayList<>();
    private T[] handlers;

    @SuppressWarnings("unchecked")
    ArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.handlers = (T[]) Array.newInstance(type, 0);
        update();
    }

    void update() {
        this.invoker = invokerFactory.apply(handlers);
    }

    @Override
    public void register(T listener) {
        register(DEFAULT_PHASE, listener);
    }

    @Override
    public void register(ResourceLocation phaseIdentifier, T listener) {
        Objects.requireNonNull(phaseIdentifier, "Tried to register a listener for a null phase!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");

        synchronized (lock) {
            getOrCreatePhase(phaseIdentifier, true).addListener(listener);
            rebuildInvoker(handlers.length + 1);
        }
    }

    private EventPhaseData<T> getOrCreatePhase(ResourceLocation id, boolean sortIfCreate) {
        var phase = phases.get(id);

        if (phase == null) {
            phase = new EventPhaseData<>(id, handlers.getClass().getComponentType());
            phases.put(id, phase);
            sortedPhases.add(phase);

            if (sortIfCreate) {
                PhaseSorting.sortPhases(sortedPhases);
            }
        }

        return phase;
    }

    private void rebuildInvoker(int newLength) {
        if (sortedPhases.size() == 1) {
            handlers = sortedPhases.get(0).listeners;
        } else {
            @SuppressWarnings("unchecked")
            var newHandlers = (T[]) Array.newInstance(handlers.getClass().getComponentType(), newLength);
            var newHandlersIndex = 0;

            for (var existingPhase : sortedPhases) {
                var length = existingPhase.listeners.length;
                System.arraycopy(existingPhase.listeners, 0, newHandlers, newHandlersIndex, length);
                newHandlersIndex += length;
            }

            handlers = newHandlers;
        }
        update();
    }

    @Override
    public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase))
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

        synchronized (lock) {
            var first = getOrCreatePhase(firstPhase, false);
            var second = getOrCreatePhase(secondPhase, false);
            first.subsequentPhases.add(second);
            second.previousPhases.add(first);
            PhaseSorting.sortPhases(this.sortedPhases);
            rebuildInvoker(handlers.length);
        }
    }
}
