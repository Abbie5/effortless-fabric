package dev.huskcasaca.effortless.render.modifier;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.huskcasaca.effortless.core.event.client.ClientShaderEvent;
import dev.huskcasaca.effortless.render.BuildRenderTypes;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Shaders {
    // logger instance
    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerShaders(ResourceProvider provider, ClientShaderEvent.ShaderRegister.ShadersSink sink) {
        try {
            sink.registerShader(
                    // TODO: 10/9/22 use custom namespace
                    new ShaderInstance(provider, "dissolve", DefaultVertexFormat.BLOCK),
                    (shaderInstance) -> BuildRenderTypes.setDissolveShaderInstance(shaderInstance)
            );
        } catch (IOException e) {
            LOGGER.error("Failed to register dissolve shader");
            e.printStackTrace();
        }
    }
}
