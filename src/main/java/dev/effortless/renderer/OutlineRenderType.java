package dev.effortless.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.effortless.Effortless;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import static dev.effortless.renderer.ExtendedRenderStateShard.NEVER_DEPTH_TEST;
import static dev.effortless.renderer.ExtendedRenderStateShard.NOTEQUAL_DEPTH_TEST;

public class OutlineRenderType extends RenderType {

    public static final ResourceLocation BLANK_TEXTURE_LOCATION = Effortless.asResource("textures/misc/blank.png");
    public static final ResourceLocation CHECKERED_TEXTURE_LOCATION = Effortless.asResource("textures/misc/checkerboard.png");
    public static final ResourceLocation CHECKERED_HIGHLIGHT_TEXTURE_LOCATION = Effortless.asResource("textures/misc/checkerboard_highlight.png");

    public static final ResourceLocation CHECKERED_THIN_TEXTURE_LOCATION = Effortless.asResource("textures/misc/checkerboard_thin.png");
    public static final ResourceLocation CHECKERED_CUTOUT_TEXTURE_LOCATION = Effortless.asResource("textures/misc/checkerboard_cutout.png");
    public static final ResourceLocation SELECTION_TEXTURE_LOCATION = Effortless.asResource("textures/misc/selection.png");
    public static final ResourceLocation GLUE_TEXTURE_LOCATION = Effortless.asResource("textures/misc/glue.png");

    private static final RenderType OUTLINE_SOLID = RenderType.create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
            false, RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                    .setTextureState(new TextureStateShard(BLANK_TEXTURE_LOCATION, false, false))
                    .setCullState(CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));
    private static final RenderType GLOWING_SOLID_DEFAULT = glowingSolid(InventoryMenu.BLOCK_ATLAS);
    private static final RenderType ADDITIVE = RenderType.create(createLayerName("additive"), DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setShaderState(BLOCK_SHADER)
                    .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));
    private static final RenderType GLOWING_TRANSLUCENT_DEFAULT = glowingTranslucent(InventoryMenu.BLOCK_ATLAS);
    private static final RenderType ITEM_PARTIAL_SOLID =
            RenderType.create(createLayerName("item_partial_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true,
                    false, RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                            .setTextureState(BLOCK_SHEET)
                            .setCullState(CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .createCompositeState(true));
    private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.create(createLayerName("item_partial_translucent"),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(BLOCK_SHEET)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));
    private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    public OutlineRenderType(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
    }

    public static RenderType outlineSolid() {
        return OUTLINE_SOLID;
    }

    public static RenderType outlineSolid(boolean overlap) {
        return RenderType.create(createLayerName("outline_solid" + (overlap ? "_overlap" : "")), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
                false, RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setDepthTestState(overlap ? NOTEQUAL_DEPTH_TEST : NEVER_DEPTH_TEST)
                        .setTextureState(new TextureStateShard(BLANK_TEXTURE_LOCATION, false, false))
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true));
    }

    public static RenderType outlineTranslucent(ResourceLocation texture, boolean cull) {
        return RenderType.create(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                        .setShaderState(cull ? RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER : RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(cull ? CULL : NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
    }

    public static RenderType glowingSolid(ResourceLocation texture) {
        return RenderType.create(createLayerName("glowing_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                true, false, RenderType.CompositeState.builder()
//				.setShaderState(GLOWING_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true));
    }

    public static RenderType glowingSolid() {
        return GLOWING_SOLID_DEFAULT;
    }

    public static RenderType glowingTranslucent(ResourceLocation texture) {
        return RenderType.create(createLayerName("glowing_translucent"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, true, RenderType.CompositeState.builder()
//				.setShaderState(GLOWING_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true));
    }

    public static RenderType additive() {
        return ADDITIVE;
    }

    public static RenderType glowingTranslucent() {
        return GLOWING_TRANSLUCENT_DEFAULT;
    }

    public static RenderType itemPartialSolid() {
        return ITEM_PARTIAL_SOLID;
    }

    public static RenderType itemPartialTranslucent() {
        return ITEM_PARTIAL_TRANSLUCENT;
    }

    public static RenderType fluid() {
        return FLUID;
    }

    private static String createLayerName(String name) {
        return Effortless.asResource(name).toString();
    }


}
