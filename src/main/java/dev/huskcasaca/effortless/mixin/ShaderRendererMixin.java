package dev.huskcasaca.effortless.mixin;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.datafixers.util.Pair;
import dev.huskcasaca.effortless.core.event.client.ClientShaderEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class ShaderRendererMixin {

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void reloadShaders(ResourceProvider resourceProvider, CallbackInfo ci, List<Program> programs, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaders) throws IOException {
        ClientShaderEvent.REGISTER_SHADER.invoker().onRegisterShader(
                resourceProvider,
                (shader, callback) -> shaders.add(Pair.of(shader, callback))
        );
    }
}