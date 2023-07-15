package dev.effortless.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.effortless.renderer.modifier.Shaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.awt.*;
import java.util.OptionalDouble;

import static dev.effortless.renderer.ExtendedRenderStateShard.RENDERTYPE_TINTED_SOLID_SHADER;

public class CustomRenderType extends RenderType {

    private static final RenderType EF_LINES = RenderType.create("ef_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.DEBUG_LINES,
            256,
            CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private static final RenderType EF_PLANES = RenderType.create("ef_planes",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
//                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public CustomRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    private static RenderType createBlockPreviewRenderType(Color color) {
        var name = Integer.toString(color.getRGB());
        var texture = new TexturingStateShard("ef_block_texturing_" + name, () -> {
            var colorUniform = Shaders.getTintedSolidShaderInstance().getUniform("TintColor");
            if (colorUniform != null) {
                colorUniform.set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        }, () -> {});
        var renderState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_TINTED_SOLID_SHADER)
                .setTexturingState(texture)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false);
        return RenderType.create("ef_block_previews_" + name, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, renderState);
    }

    public static RenderType lines() {
        return EF_LINES;
    }

    public static RenderType planes() {
        return EF_PLANES;
    }

    public static RenderType solid(Color color) {
        return createBlockPreviewRenderType(color);
    }

}
