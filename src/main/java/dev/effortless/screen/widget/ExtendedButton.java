package dev.effortless.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.screen.ScreenUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ExtendedButton extends Button {
    public ExtendedButton(int posX, int posY, int width, int height, Component displayString, OnPress handler) {
        super(posX, posY, width, height, displayString, handler, Button.DEFAULT_NARRATION);
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        var minecraft = Minecraft.getInstance();
        int k = this.getTextureY(this.isHovered);
        ScreenUtils.blitWithBorder(gui, WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, 0);

        Component buttonText = this.getMessage();
        int strWidth = minecraft.font.width(buttonText);
        int ellipsisWidth = minecraft.font.width("...");

        if (strWidth > width - 6 && strWidth > ellipsisWidth)
            //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
            buttonText = Component.literal(minecraft.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

        gui.drawCenteredString(minecraft.font, buttonText, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 14737632);
    }

    private int getTextureY(boolean active) {
        int i = 1;
        if (active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}
