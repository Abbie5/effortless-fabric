package dev.effortless.render;

import dev.effortless.render.modifier.Shaders;
import net.minecraft.client.renderer.RenderStateShard;

import static org.lwjgl.opengl.GL11.*;

public class ExtendedRenderStateShard extends RenderStateShard {

    public static final ShaderStateShard RENDERTYPE_TINTED_SOLID_SHADER = new ShaderStateShard(Shaders::getTintedSolidShaderInstance);

    public static final DepthTestStateShard NEVER_DEPTH_TEST = new DepthTestStateShard("never", GL_NEVER);
    public static final DepthTestStateShard LESS_DEPTH_TEST = new DepthTestStateShard("less", GL_LESS);
    public static final DepthTestStateShard EQUAL_DEPTH_TEST = new DepthTestStateShard("equal", GL_EQUAL);
    public static final DepthTestStateShard LEQUAL_DEPTH_TEST = new DepthTestStateShard("lequal", GL_LEQUAL);
    public static final DepthTestStateShard GREATER_DEPTH_TEST = new DepthTestStateShard("greater", GL_GREATER);
    public static final DepthTestStateShard NOTEQUAL_DEPTH_TEST = new DepthTestStateShard("notequal", GL_NOTEQUAL);
    public static final DepthTestStateShard GEQUAL_DEPTH_TEST = new DepthTestStateShard("gequal", GL_GEQUAL);
    public static final DepthTestStateShard ALWAYS_DEPTH_TEST = new DepthTestStateShard("always", GL_ALWAYS);

    public ExtendedRenderStateShard(String string, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
    }
}
