package dev.huskcasaca.effortless.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class NumberField extends AbstractContainerWidget {

    private final int buttonWidth = 10;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final EditBox textField;
    private final Button minusButton;
    private final Button plusButton;
    public NumberField(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal(""));

        textField = new EditBox(minecraft.font, x + buttonWidth + 1, y + 1, width - 2 * buttonWidth - 2, height - 2, Component.empty());
        minusButton = new Button(x, y - 1, buttonWidth, height + 2, Component.literal("-"), button -> {
            float valueChanged = 1f;
            if (Screen.hasControlDown()) valueChanged = 5f;
            if (Screen.hasShiftDown()) valueChanged = 10f;

            setNumber(getNumber() - valueChanged);
        }, Button.DEFAULT_NARRATION);
        plusButton = new Button(x + width - buttonWidth, y - 1, buttonWidth, height + 2, Component.literal("+"), button -> {
            float valueChanged = 1f;
            if (Screen.hasControlDown()) valueChanged = 5f;
            if (Screen.hasShiftDown()) valueChanged = 10f;

            setNumber(getNumber() + valueChanged);
        }, Button.DEFAULT_NARRATION);

    }

    List<Component> tooltip = new ArrayList<>();

    public EditBox getTextField() {
        return textField;
    }

    public double getNumber() {
        if (textField.getValue().isEmpty()) return 0;
        try {
            return DecimalFormat.getInstance().parse(textField.getValue()).doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    public void setNumber(double number) {
        textField.setValue(DecimalFormat.getInstance().format(number));
    }

    public void setTooltip(Component tooltip) {
        setTooltip(Collections.singletonList(tooltip));
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    protected List<? extends AbstractWidget> getContainedChildren() {
        return List.of(textField, minusButton, plusButton);
    }

    public void drawTooltip(PoseStack poseStack, Screen screen, int mouseX, int mouseY) {
        boolean insideTextField = mouseX >= x + buttonWidth && mouseX < x + width - buttonWidth && mouseY >= y && mouseY < y + height;
        boolean insideMinusButton = mouseX >= x && mouseX < x + buttonWidth && mouseY >= y && mouseY < y + height;
        boolean insidePlusButton = mouseX >= x + width - buttonWidth && mouseX < x + width && mouseY >= y && mouseY < y + height;

        List<Component> textLines = new ArrayList<>();


        if (insideTextField) {
            textLines.addAll(tooltip);
//            textLines.add(TextFormatting.GRAY + "Tip: try scrolling.");
        }

        if (insideMinusButton) {
            textLines.add(Component.literal("Hold ").append(Component.literal("shift ").withStyle(ChatFormatting.AQUA)).append("for ")
                    .append(Component.literal("10").withStyle(ChatFormatting.RED)));
            textLines.add(Component.literal("Hold ").append(Component.literal("ctrl ").withStyle(ChatFormatting.AQUA)).append("for ")
                    .append(Component.literal("5").withStyle(ChatFormatting.RED)));
        }

        if (insidePlusButton) {
            textLines.add(Component.literal("Hold ").append(Component.literal("shift ").withStyle(ChatFormatting.DARK_GREEN)).append("for ")
                    .append(Component.literal("10").withStyle(ChatFormatting.RED)));
            textLines.add(Component.literal("Hold ").append(Component.literal("ctrl ").withStyle(ChatFormatting.DARK_GREEN)).append("for ")
                    .append(Component.literal("5").withStyle(ChatFormatting.RED)));
        }

        minecraft.screen.renderComponentTooltip(poseStack, textLines, mouseX - 10, mouseY + 25);

    }

    public void tick() {
        textField.tick();
    }

}
