package dev.huskcasaca.effortless.screen.radial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.keybinding.Keys;
import dev.huskcasaca.effortless.screen.widget.RadialSection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;


@Environment(EnvType.CLIENT)
public class AbstractRadialScreen extends Screen {

    private static final float FADE_SPEED = 0.5f;
    private static final int WATERMARK_TEXT_COLOR = 0x8d7f7f7f;
    protected RadialSection radial;
    private float visibility;

    public AbstractRadialScreen(Component component) {
        super(component);
    }

    protected static void fillGradient2(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient2(poseStack.last().pose(), bufferBuilder, i, j, k, l, o, m, n);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    protected static void fillGradient2(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o) {
        float f = (float)(n >> 24 & 255) / 255.0F;
        float g = (float)(n >> 16 & 255) / 255.0F;
        float h = (float)(n >> 8 & 255) / 255.0F;
        float p = (float)(n & 255) / 255.0F;
        float q = (float)(o >> 24 & 255) / 255.0F;
        float r = (float)(o >> 16 & 255) / 255.0F;
        float s = (float)(o >> 8 & 255) / 255.0F;
        float t = (float)(o & 255) / 255.0F;
        bufferBuilder.vertex(matrix4f, (float)k, (float)j, (float)m).color(r, s, t, q).endVertex();
        bufferBuilder.vertex(matrix4f, (float)i, (float)j, (float)m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)i, (float)l, (float)m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)k, (float)l, (float)m).color(r, s, t, q).endVertex();
    }

    @Override
    protected void init() {
        super.init();
        visibility = 0f;
        radial = addRenderableWidget(
                new RadialSection(0, 0, this.width, this.height, Component.empty())
        );
    }

    @Override
    public void tick() {
        if (!Keys.BUILD_MODE_RADIAL.isKeyDown()) {
            onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        visibility = Math.min(visibility + FADE_SPEED * partialTicks, 1f);
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        radial.renderTooltip(poseStack, this, mouseX, mouseY);
        font.drawShadow(poseStack, title, width - font.width(title) - 10, height - 15, WATERMARK_TEXT_COLOR);
//        fillGradient2(poseStack, this.width - 80, 0, this.width, this.height, 0x00101010, 0xb4101010);
//        fillGradient2(poseStack, 80, 0, this.width, 0, 0x00101010, 0xb4101010);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int i) {
        if (minecraft != null && minecraft.level != null) {
            fillGradient(poseStack, 0, 0, this.width, this.height, (int) (visibility * 0xC0) << 24 | 0x101010, (int) (visibility * 0xD0) << 24 | 0x101010);
        } else {
            this.renderDirtBackground(0);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // horizontal fill
//    protected void fillGradient2(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
//        fillGradient2(poseStack, i, j, k, l, m, n, this.getBlitOffset());
//    }

}

