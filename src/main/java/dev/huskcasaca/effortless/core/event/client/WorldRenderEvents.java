package dev.huskcasaca.effortless.core.event.client;

import net.fabricmc.fabric.api.event.Event;

public class WorldRenderEvents {
    public static final Event<AfterEntities> AFTER_ENTITIES = new Event<>() {
        @Override
        public void register(AfterEntities listener) {
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
                listener.afterEntities(
                        new WorldRenderContext(
                                context.matrixStack(),
                                context.camera()
                        )
                );
            });
        }
    };
    public static final Event<End> END = new Event<>() {
        @Override
        public void register(End listener) {
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.END.register((context) -> {
                listener.onEnd(
                        new WorldRenderContext(
                                context.matrixStack(),
                                context.camera()
                        )
                );
            });
        }
    };

    private WorldRenderEvents() { }

    @FunctionalInterface
    public interface AfterEntities {
        void afterEntities(WorldRenderContext context);
    }

    @FunctionalInterface
    public interface End {
        void onEnd(WorldRenderContext context);
    }
}

