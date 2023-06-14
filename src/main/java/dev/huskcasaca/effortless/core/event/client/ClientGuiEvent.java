package dev.huskcasaca.effortless.core.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.core.event.api.Event;
import dev.huskcasaca.effortless.core.event.api.EventFactory;

public class ClientGuiEvent {

    public static final Event<RenderGui> RENDER_GUI = EventFactory.createArrayBacked(RenderGui.class, callbacks -> (poseStack) -> {
        for (RenderGui callback : callbacks) {
            callback.onRenderGui(poseStack);
        }
    });

    @FunctionalInterface
    public interface RenderGui {
        void onRenderGui(PoseStack poseStack);
    }

}