package dev.huskcasaca.effortless.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.screen.radial.OptionSet;
import dev.huskcasaca.effortless.screen.radial.RadialSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class RadialSection extends AbstractWidget {

    private static final Color SIDE_BUTTON_COLOR = new Color(0f, 0f, 0f, .42f);
    private static final Color SIDE_BUTTON_SELECTED_COLOR = new Color(.5f, .5f, .5f, .78f);
    private static final Color SIDE_BUTTON_HIGHLIGHT_COLOR = new Color(0.42f, 0.42f, 0.42f,  0.5f);
    private static final Color SIDE_BUTTON_HIGHLIGHT_SELECTED_COLOR = new Color(0.60f, 0.60f, 0.60f, .78f);

    private static final Color RADIAL_BUTTON_COLOR = new Color(0f, 0f, 0f, .42f);
    private static final Color RADIAL_BUTTON_SELECTED_COLOR = new Color(.5f, .5f, .5f, .78f);
    private static final Color RADIAL_BUTTON_HIGHLIGHT_COLOR = new Color(0.42f, 0.42f, 0.42f,  0.5f);
    private static final Color RADIAL_BUTTON_HIGHLIGHT_SELECTED_COLOR = new Color(0.60f, 0.60f, 0.60f, .78f);

    private static final int WHITE_TEXT_COLOR = 0xffffffff;
    private static final int OPTION_TEXT_COLOR = 0xeeeeeeff;

    private static final double RING_INNER_EDGE = 32;
    private static final double RING_OUTER_EDGE = 67;
    private static final double CATEGORY_LINE_OUTER_EDGE = 36;

    private static final double TEXT_DISTANCE = 84;

    private static final double SECTION_OFFSET_X = 112;
    private static final double SECTION_OFFSET_Y = 0;

    private static final int BUTTON_WIDTH = 22;
    private static final int BUTTON_HEIGHT = 22;

    private static final double BUTTON_OFFSET_X = 26;
    private static final double BUTTON_OFFSET_Y = 26;

    private static final double TITLE_HEIGHT = 10;

    private static final int MIN_RADIAL_SIZE = 8;

    private static final float MOUSE_SCROLL_THRESHOLD = 1;
    private final Minecraft minecraft;
    private Consumer<RadialSlot> radialSelectResponder;
    private Consumer<RadialSlot> radialSwipeResponder;
    private Consumer<OptionSet.Entry> radialOptionSelectResponder;
    private float lastScrollOffset = 0;

    private RadialSlot highlightedSlot;
    private OptionSet highlightedOption;
    private OptionSet.Entry highlightedOptionEntry;

    private Set<RadialSlot> selectedSlot = new HashSet<>();
    private Set<OptionSet.Entry> selectedOptionEntry = new HashSet<>();

    private List<? extends RadialSlot> radialSlots = Collections.emptyList();
    private List<? extends OptionSet> fixedOptions = Collections.emptyList();
    private List<? extends OptionSet> localOptions = Collections.emptyList();

    // TODO: 20/2/23 rename
    private float visibility = 0;

    public RadialSection(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
        this.minecraft = Minecraft.getInstance();
    }

    private static void playRadialMenuSound() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        visibility = Math.min(visibility + 0.5f * f, 1f);


        highlightedSlot = null;
        highlightedOption = null;
        highlightedOptionEntry = null;

        var regions = radialSlots.stream().map(Region::new).toList();
        var left = fixedOptions.stream().map((entry) -> new Section(entry, AxisDirection.NEGATIVE)).toList();
        var right = localOptions.stream().map((entry) -> new Section(entry, AxisDirection.POSITIVE)).toList();

        renderRadialSlotBackgrounds(poseStack, i, j, regions);
        renderSideButtonBackgrounds(poseStack, i, j, left);
        renderSideButtonBackgrounds(poseStack, i, j, right);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var result = false;
        if (this.active && this.visible) {
            if (radialSelectResponder != null && highlightedSlot != null) {
                radialSelectResponder.accept(highlightedSlot);
                result = true;
            }

            if (radialOptionSelectResponder != null && highlightedOptionEntry != null) {
                radialOptionSelectResponder.accept(highlightedOptionEntry);
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        var sign = lastScrollOffset * f;
        if (sign < 0) {
            lastScrollOffset = 0;
        }
        lastScrollOffset += f;
        if (lastScrollOffset > MOUSE_SCROLL_THRESHOLD) {
            BuildModeHelper.reverseBuildMode(minecraft.player);
            lastScrollOffset = 0;
        } else if (lastScrollOffset < -MOUSE_SCROLL_THRESHOLD) {
            BuildModeHelper.cycleBuildMode(minecraft.player);
            lastScrollOffset = 0;
        }
        return true;
    }

    public void setRadialSelectResponder(Consumer<RadialSlot> consumer) {
        this.radialSelectResponder = consumer;
    }

    public void setRadialSwipeResponder(Consumer<RadialSlot> consumer) {
        this.radialSwipeResponder = consumer;
    }

    public void setRadialOptionSelectResponder(Consumer<OptionSet.Entry> consumer) {
        this.radialOptionSelectResponder = consumer;
    }

    public void setRadialSlots(List<? extends RadialSlot> radialSlots) {
        this.radialSlots = radialSlots;
    }

    public void setFixedOptions(List<? extends OptionSet> fixedOptions) {
        this.fixedOptions = fixedOptions;
    }

    public void setLocalOptions(List<? extends OptionSet> localOptions) {
        this.localOptions = localOptions;
    }

    private void renderRadialSlotBackgrounds(PoseStack poseStack, int mouseX, int mouseY, List<Region> regions) {

        double middleX = width / 2.0;
        double middleY = height / 2.0;

        var mouseCenterX = mouseX - middleX;
        var mouseCenterY = mouseY - middleY;

        var mouseRad = (Math.atan2(mouseCenterY, mouseCenterX) + 2 * Math.PI) % (2 * Math.PI);

        var ringInnerEdge = RING_INNER_EDGE * 0.72f + RING_INNER_EDGE * visibility * 0.28f;
        var ringOuterEdge = RING_OUTER_EDGE * 0.5f + RING_OUTER_EDGE * visibility * 0.5f;
        var categoryOuterEdge = CATEGORY_LINE_OUTER_EDGE * 0.72f + CATEGORY_LINE_OUTER_EDGE * visibility * 0.28f;

        var innerGap = Math.PI * 0.007; // gap between buttons in radians at inner edge
        var outerGap = innerGap * ringInnerEdge / ringOuterEdge; // gap between buttons in radians at outer edge
        var rad = 2.0 * Math.PI / Math.max(MIN_RADIAL_SIZE, regions.size());

        for (int i = 0; i < regions.size(); i++) {

            var region = regions.get(i);
            var slot = region.slot;
            var begRad = (i - 0.5) * rad - Math.PI / 2.0;
            var endRad = (i + 0.5) * rad - Math.PI / 2.0;

            var x1 = Math.cos(begRad);
            var x2 = Math.cos(endRad);
            var y1 = Math.sin(begRad);
            var y2 = Math.sin(endRad);

            var x1m1 = Math.cos(begRad + innerGap) * ringInnerEdge;
            var x2m1 = Math.cos(endRad - innerGap) * ringInnerEdge;
            var y1m1 = Math.sin(begRad + innerGap) * ringInnerEdge;
            var y2m1 = Math.sin(endRad - innerGap) * ringInnerEdge;

            var x1m2 = Math.cos(begRad + outerGap) * ringOuterEdge;
            var x2m2 = Math.cos(endRad - outerGap) * ringOuterEdge;
            var y1m2 = Math.sin(begRad + outerGap) * ringOuterEdge;
            var y2m2 = Math.sin(endRad - outerGap) * ringOuterEdge;

            var isSelected = selectedSlot.contains(slot);
            var isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseCenterX, mouseCenterY) || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseCenterX, mouseCenterY);
            var isHighlighted = ((begRad <= mouseRad && mouseRad <= endRad) || (begRad <= (mouseRad - 2 * Math.PI) && (mouseRad - 2 * Math.PI) <= endRad)) && isMouseInQuad;

            var color = RADIAL_BUTTON_COLOR;
            if (isSelected) color = RADIAL_BUTTON_SELECTED_COLOR;
            if (isHighlighted) color = RADIAL_BUTTON_HIGHLIGHT_COLOR;
            if (isSelected && isHighlighted) color = RADIAL_BUTTON_HIGHLIGHT_SELECTED_COLOR;

            if (isHighlighted) {
                highlightedSlot = slot;
            }

            // background tint
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            var tesselator = Tesselator.getInstance();
            var bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            bufferBuilder.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m2, middleY + y2m2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x1m2, middleY + y1m2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            // category line
            color = slot.getTintColor();

            var x1m3 = Math.cos(begRad + innerGap) * categoryOuterEdge;
            var x2m3 = Math.cos(endRad - innerGap) * categoryOuterEdge;
            var y1m3 = Math.sin(begRad + innerGap) * categoryOuterEdge;
            var y2m3 = Math.sin(endRad - innerGap) * categoryOuterEdge;

            bufferBuilder.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m3, middleY + y2m3, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x1m3, middleY + y1m3, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            tesselator.end();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();

            // icon
            RenderSystem.enableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            var iconX = (x1 + x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            var iconY = (y1 + y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            RenderSystem.setShaderTexture(0, region.slot().getIcon());
            blit(poseStack, (int) Math.round(middleX + iconX - 8), (int) Math.round(middleY + iconY - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

    }

    private void renderSideButtonBackgrounds(PoseStack poseStack, int mouseX, int mouseY, List<Section> sections) {

        double middleX = width / 2.0;
        double middleY = height / 2.0;

        var mouseCenterX = mouseX - middleX;
        var mouseCenterY = mouseY - middleY;

        for (var row = 0; row < sections.size(); row++) {
            var section = sections.get(row);
            var buttons = section.buttons();
            var option = section.option();

            for (int col = 0; col < buttons.size(); col++) {
                var button = buttons.get(col);
                var entry = button.entry();

                var x = (SECTION_OFFSET_X + BUTTON_OFFSET_X * col) * section.direction.getStep();
                var y = (SECTION_OFFSET_Y + BUTTON_OFFSET_Y * (row - (sections.size() - 1) / 2f)) * 1;

                var x1 = x - BUTTON_WIDTH / 2;
                var y1 = y - BUTTON_HEIGHT / 2;

                var x2 = x + BUTTON_WIDTH / 2;
                var y2 = y + BUTTON_HEIGHT / 2;

                var isSelected = selectedOptionEntry != null && selectedOptionEntry.contains(entry);
                var isHighlighted = x1 <= mouseCenterX && x2 >= mouseCenterX && y1 <= mouseCenterY && y2 >= mouseCenterY;

                var color = SIDE_BUTTON_COLOR;
                if (isSelected) color = SIDE_BUTTON_SELECTED_COLOR;
                if (isHighlighted) color = SIDE_BUTTON_HIGHLIGHT_COLOR;
                if (isSelected && isHighlighted) color = SIDE_BUTTON_HIGHLIGHT_SELECTED_COLOR;

                if (isHighlighted) {
                    highlightedOption = option;
                    highlightedOptionEntry = entry;
                }

                RenderSystem.enableBlend();
                RenderSystem.disableTexture();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                var tesselator = Tesselator.getInstance();
                var bufferBuilder = tesselator.getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                bufferBuilder.vertex(middleX + x1, middleY + y1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x1, middleY + y2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x2, middleY + y2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x2, middleY + y1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                tesselator.end();
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();

                // icon
                RenderSystem.enableTexture();
                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/action/" + button.entry().getName() + ".png"));
                blit(poseStack, (int) Math.round(middleX + x - 8), (int) Math.round(middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);

            }
        }
    }

    public void renderTooltip(PoseStack poseStack, Screen screen, int mouseX, int mouseY) {
        if (highlightedOption != null && highlightedOptionEntry != null) {
            var tooltip = new ArrayList<Component>();

            tooltip.add(highlightedOptionEntry.getComponentName().copy().withStyle(ChatFormatting.WHITE));
            tooltip.add(highlightedOption.getComponentName().copy().withStyle(ChatFormatting.DARK_GRAY));
//            tooltip.add(Component.empty());


//        var keybind = findKeybind(button, currentBuildMode);
//            var keybindFormatted = "";
//        if (!keybind.isEmpty())
//            keybindFormatted = ChatFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";

            screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    private boolean inTriangle(double x1, double y1, double x2, double y2,
                               double x3, double y3, double x, double y) {
        var ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        var bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        var ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return Mth.sign(ab) == Mth.sign(bc) && Mth.sign(bc) == Mth.sign(ca);
    }

    private record Section(OptionSet option, AxisDirection direction) {

        public List<Button> buttons() {
            return Arrays.stream(option.getEntries()).map((entry -> new Button(entry))).toList();
        }

    }

    private record Button(OptionSet.Entry entry) { }

    private record Region(RadialSlot slot) { }

//    private static void drawRadialSlotTexts(PoseStack poseStack, Font font, double middleX, double middleY, ArrayList<Region> modes) {
//        for (var region : modes) {
//            if (!region.highlighted) {
//                continue;
//            }
//            final double x = (region.x1 + region.x2) * 0.5;
//            final double y = (region.y1 + region.y2) * 0.5;
//
//            int posX = (int) (x * RadialSection.TEXT_DISTANCE);
//            int posY = (int) (y * RadialSection.TEXT_DISTANCE) - font.lineHeight / 2;
//            var text = I18n.get(region.mode.getNameKey());
//
//            if (x <= -0.2) {
//                posX -= font.width(text);
//            } else if (-0.2 <= x && x <= 0.2) {
//                posX -= font.width(text) / 2;
//            }
//
//            font.drawShadow(poseStack, text, (int) middleX + posX, (int) middleY + posY, WHITE_TEXT_COLOR);
//        }
//    }
//
//    private static void drawSideButtonDes(PoseStack poseStack, Font font, double middleX, double middleY, ArrayList<Button> buttons, BuildMode.Option[] options) {
//        String credits = I18n.get(String.join(".", Effortless.MOD_ID, "building", "credits"));
//        font.drawShadow(poseStack, credits, width - font.width(credits) - 10, height - 15, WATERMARK_TEXT_COLOR);
//

//    }
//
//    private static void drawSideButtonTexts(PoseStack poseStack, Font font, double middleX, double middleY, ArrayList<Button> buttons, BuildMode.Option[] options) {
//        String credits = I18n.get(String.join(".", Effortless.MOD_ID, "building", "credits"));
//        font.drawShadow(poseStack, credits, width - font.width(credits) - 10, height - 15, WATERMARK_TEXT_COLOR);
//
//        for (int row = 0; row < options.length; row++) {
//            BuildMode.Option option = options[row];
//            if (option == null) continue;
//            font.drawShadow(poseStack, I18n.get(option.getNameKey()), (int) (middleX + RadialSection.SECTION_OFFSET_X - 9), (int) middleY + options.length / -2f * MODE_OPTION_ROW_HEIGHT + 3 + row * MODE_OPTION_ROW_HEIGHT, OPTION_TEXT_COLOR);
//        }
//
//        for (final Button button : buttons) {
//            if (button.highlighted) {
//                String text = ChatFormatting.AQUA + button.name;
//
//                //Add keybind in brackets
//                String keybind = findKeybind(button, currentBuildMode);
//                String keybindFormatted = "";
//                if (!keybind.isEmpty())
//                    keybindFormatted = ChatFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";
//
//                switch (button.side) {
//                    case WEST -> {
//                        font.draw(poseStack, text, (int) (middleX + x1 - 8) - font.width(text), (int) (middleY + y1 + 6), WHITE_TEXT_COLOR);
//                    }
//                    case EAST -> {
//                        font.draw(poseStack, text, (int) (middleX + x2 + 8), (int) (middleY + y1 + 6), WHITE_TEXT_COLOR);
//                    }
//                    case UP, NORTH -> {
//                        font.draw(poseStack, keybindFormatted, (int) (middleX + (x1 + x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + y1 - 26), WHITE_TEXT_COLOR);
//                        font.draw(poseStack, text, (int) (middleX + (x1 + x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + y1 - 14), WHITE_TEXT_COLOR);
//                    }
//                    case DOWN, SOUTH -> {
//                        font.draw(poseStack, text, (int) (middleX + (x1 + x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + y1 + 26), WHITE_TEXT_COLOR);
//                        font.draw(poseStack, keybindFormatted, (int) (middleX + (x1 + x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + y1 + 38), WHITE_TEXT_COLOR);
//                    }
//                }
//
//            }
//        }
//    }


//    private String findKeybind(Button button, BuildMode currentBuildMode) {
//        Keys keybindingIndex = null;
//
//        switch (button.action) {
//            case SETTINGS -> keybindingIndex = Keys.SETTINGS;
//            case MODIFIER -> keybindingIndex = Keys.BUILD_MODIFIER_SETTINGS;
//            case UNDO -> keybindingIndex = Keys.UNDO;
//            case REDO -> keybindingIndex = Keys.REDO;
//            case REPLACE -> keybindingIndex = Keys.TOGGLE_REPLACE;
//        }
//
//        if (keybindingIndex == null) {
//            return "";
//        }
//
//        return keybindingIndex.getKeyMapping().key.getName();
//
////        if (currentBuildMode.options.length > 0) {
////            //Add (ctrl) to first two actions of first option
////            if (button.action == currentBuildMode.options[0].actions[0]
////                    || button.action == currentBuildMode.options[0].actions[1]) {
////                result = I18n.get(((KeyMappingAccessor) EffortlessClient.keyBindings[5]).getKey().getName());
////                if (result.equals("Left Control")) result = "Ctrl";
////            }
////        }
//    }

//    private boolean isButtonHighlighted(Button btn, double mouseXCenter, double mouseYCenter) {
//        return btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;
//    }
//
//    private boolean isButtonHighlighted(ArrayList<Button> btns, double mouseXCenter, double mouseYCenter) {
//        for (var btn : btns) {
//            if (isButtonHighlighted(btn, mouseXCenter, mouseYCenter)) {
//                return true;
//            }
//        }
//        return false;
//
//    }
//
//    private boolean isMouseInButtonGroup(ArrayList<Button> btns, double mouseXCenter, double mouseYCenter) {
//        if (btns.isEmpty()) return false;
//
//
//        return btns.stream().map(btn -> btn.x1).min(Double::compare).get() <= mouseXCenter && btns.stream().map(btn -> btn.x2).max(Double::compare).get() >= mouseXCenter && btns.stream().map(btn -> btn.y1).min(Double::compare).get() <= mouseYCenter && btns.stream().map(btn -> btn.y2).max(Double::compare).get() >= mouseYCenter;
//    }


}
