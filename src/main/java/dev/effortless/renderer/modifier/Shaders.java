package dev.effortless.renderer.modifier;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.effortless.core.event.client.ClientShaderEvent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;

public class Shaders {
    // logger instance
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private static ShaderInstance tintedSolidShaderInstance;

    public static void registerShaders(ResourceProvider provider, ClientShaderEvent.ShaderRegister.ShadersSink sink) {
        try {
            sink.registerShader(
                    // TODO: 10/9/22 use custom namespace
                    new ShaderInstance(provider, "rendertype_tinted_solid", DefaultVertexFormat.BLOCK),
                    (shaderInstance) -> { tintedSolidShaderInstance = shaderInstance; }
            );
        } catch (IOException e) {
            LOGGER.error("Failed to register effortless shaders");
            e.printStackTrace();
        }
    }

    public static ShaderInstance getTintedSolidShaderInstance() {
        return tintedSolidShaderInstance;
    }

}
