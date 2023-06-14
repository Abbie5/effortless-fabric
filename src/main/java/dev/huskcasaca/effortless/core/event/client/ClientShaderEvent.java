package dev.huskcasaca.effortless.core.event.client;

import dev.huskcasaca.effortless.core.event.api.Event;
import dev.huskcasaca.effortless.core.event.api.EventFactory;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.function.Consumer;

public class ClientShaderEvent {

    public static final Event<ShaderRegister> REGISTER_SHADER = EventFactory.createArrayBacked(ShaderRegister.class, callbacks -> (provider, sink) -> {
        for (ShaderRegister callback : callbacks) {
            callback.onRegisterShader(provider, sink);
        }
    });

    @FunctionalInterface
    public interface ShaderRegister {
        void onRegisterShader(ResourceProvider provider, ShadersSink sink) throws IOException;

        @FunctionalInterface
        interface ShadersSink {
            void registerShader(ShaderInstance shader, Consumer<ShaderInstance> consumer);
        }
    }


}
