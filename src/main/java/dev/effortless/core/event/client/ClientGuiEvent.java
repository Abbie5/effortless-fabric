package dev.effortless.core.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.core.event.api.Event;
import dev.effortless.core.event.api.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

public class ClientGuiEvent {

    public static final Event<RenderGui> RENDER_GUI = EventFactory.createArrayBacked(RenderGui.class, callbacks -> (gui) -> {
        for (RenderGui callback : callbacks) {
            callback.onRenderGui(gui);
        }
    });

    @FunctionalInterface
    public interface RenderGui {
        void onRenderGui(GuiGraphics gui);
    }

}