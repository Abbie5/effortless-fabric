package dev.huskcasaca.effortless.core.event.api;

import net.minecraft.resources.ResourceLocation;

public abstract class Event<T> {

    public static final ResourceLocation DEFAULT_PHASE = new ResourceLocation("effortless", "default");

    protected volatile T invoker;

    public final T invoker() {
        return invoker;
    }

    public abstract void register(T listener);

    public void register(ResourceLocation phase, T listener) {
        register(listener);
    }

    public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
    }
}
