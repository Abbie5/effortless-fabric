package dev.effortless.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.effortless.building.EffortlessBuilder;
import dev.effortless.screen.radial.RadialButton;
import dev.effortless.screen.radial.RadialButtonSet;
import dev.effortless.screen.radial.RadialSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class RadialSection extends AbstractWidget {

    private static final ColorState RADIAL_SLOT_COLOR_STATE = new ColorState(
            new Color(0f, 0f, 0f, 0.42f),
            new Color(0f, 0f, 0f, 0.42f),
            new Color(0.24f, 0.24f, 0.24f, 0.50f),
            new Color(0.36f, 0.36f, 0.36f, 0.64f),
            new Color(0.42f, 0.42f, 0.42f, 0.64f)
    );
    private static final ColorState RADIAL_BUTTON_COLOR_STATE = RADIAL_SLOT_COLOR_STATE;
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
    private final EffortlessBuilder builder;
    private final Minecraft minecraft;
    private Consumer<RadialSlot<?>> radialSelectResponder;
    private Consumer<RadialSlot<?>> radialSwipeResponder;
    private Consumer<RadialButton<?>> radialOptionSelectResponder;
    private RadialSlot<?> hoveredSlot;
    private float lastScrollOffset = 0;
    private RadialButton<?> hoveredEntry;
    private Collection<? extends RadialSlot<?>> selectedSlot = new HashSet<>();
    private Collection<? extends RadialButton<?>> selectedButton = new HashSet<>();
    private List<? extends RadialSlot<?>> radialSlots = List.of();
    private List<? extends RadialButtonSet> leftButtons = List.of();
    private List<? extends RadialButtonSet> rightButtons = List.of();
    // TODO: 20/2/23 rename
    private float visibility = 0;

    public RadialSection(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
        this.minecraft = Minecraft.getInstance();
        this.builder = EffortlessBuilder.getInstance();
    }

    private static void playRadialMenuSound() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void renderWidget(GuiGraphics gui, int i, int j, float f) {
        visibility = Math.min(visibility + 0.5f * f, 1f);

        hoveredSlot = null;
//        highlightedOption = null;
        hoveredEntry = null;

        var regions = radialSlots.stream().map(Region::new).toList();
        var west = leftButtons.stream().map((entry) -> new Section(entry, AxisDirection.NEGATIVE)).toList();
        var east = rightButtons.stream().map((entry) -> new Section(entry, AxisDirection.POSITIVE)).toList();

        renderRadialSlotBackgrounds(gui, i, j, regions);
        renderRadialButtonBackgrounds(gui, i, j, west);
        renderRadialButtonBackgrounds(gui, i, j, east);

//        drawRadialSlotTexts(poseStack, minecraft.font, i, j, );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var result = false;
        if (this.active && this.visible) {
            if (radialSelectResponder != null && hoveredSlot != null) {
                radialSelectResponder.accept(hoveredSlot);
                result = true;
            }

            if (radialOptionSelectResponder != null && hoveredEntry != null) {
                radialOptionSelectResponder.accept(hoveredEntry);
                result = true;
            }
        }
        return result;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        var sign = lastScrollOffset * f;
        if (sign < 0) {
            lastScrollOffset = 0;
        }
        lastScrollOffset += f;
        if (lastScrollOffset > MOUSE_SCROLL_THRESHOLD) {
            cycleBuildMode(minecraft.player, true);
            lastScrollOffset = 0;
        } else if (lastScrollOffset < -MOUSE_SCROLL_THRESHOLD) {
            cycleBuildMode(minecraft.player, false);
            lastScrollOffset = 0;
        }
        return true;
    }

    public void setRadialSelectResponder(Consumer<RadialSlot<?>> consumer) {
        this.radialSelectResponder = consumer;
    }

    public void setRadialSwipeResponder(Consumer<RadialSlot<?>> consumer) {
        this.radialSwipeResponder = consumer;
    }

    public void setRadialOptionSelectResponder(Consumer<RadialButton<?>> consumer) {
        this.radialOptionSelectResponder = consumer;
    }

    public void setRadialSlots(List<? extends RadialSlot<?>> slots) {
        this.radialSlots = slots;
    }

    public void setLeftButtons(RadialButtonSet... options) {
        this.leftButtons = List.of(options);
    }

    public void setLeftButtons(List<? extends RadialButtonSet> options) {
        this.leftButtons = options;
    }

    public void setRightButtons(RadialButtonSet... options) {
        this.rightButtons = List.of(options);
    }

    public void setRightButtons(List<? extends RadialButtonSet> options) {
        this.rightButtons = options;
    }

    public void setSelectedSlots(RadialSlot<?>... slots) {
        this.selectedSlot = Set.of(slots);
    }

    public void setSelectedSlots(Collection<? extends RadialSlot<?>> slots) {
        this.selectedSlot = slots;
    }

    public void setSelectedButtons(Collection<? extends RadialButton<?>> options) {
        this.selectedButton = options;
    }

    private void renderRadialSlotBackgrounds(GuiGraphics gui, int mouseX, int mouseY, List<Region> regions) {

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

            var isActivated = selectedSlot.contains(slot);
            var isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseCenterX, mouseCenterY) || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseCenterX, mouseCenterY);
            var isHovered = ((begRad <= mouseRad && mouseRad <= endRad) || (begRad <= (mouseRad - 2 * Math.PI) && (mouseRad - 2 * Math.PI) <= endRad)) && isMouseInQuad;

            var color = RADIAL_SLOT_COLOR_STATE.defaultColor();
            if (isActivated) color = RADIAL_SLOT_COLOR_STATE.activedColor();
            if (isHovered) color = RADIAL_SLOT_COLOR_STATE.hoveredColor();
            if (isActivated && isHovered) color = RADIAL_SLOT_COLOR_STATE.activedHoveredColor();

            if (isHovered) {
                hoveredSlot = slot;
                final double x = (x1 + x2) * 0.5;
                final double y = (y1 + y2) * 0.5;

                int textX = (int) (x * RadialSection.TEXT_DISTANCE);
                int textY = (int) (y * RadialSection.TEXT_DISTANCE) - minecraft.font.lineHeight / 2;
                var text = region.slot().getNameComponent();

                if (x <= -0.2) {
                    textX -= minecraft.font.width(text);
                } else if (-0.2 <= x && x <= 0.2) {
                    textX -= minecraft.font.width(text) / 2;
                }
                gui.drawString(minecraft.font, text, (int) middleX + textX, (int) middleY + textY, WHITE_TEXT_COLOR, true);
            }

            // background tint
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            var tesselator = Tesselator.getInstance();
            var bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            bufferBuilder.vertex(middleX + x1m1, middleY + y1m1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m1, middleY + y2m1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m2, middleY + y2m2, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x1m2, middleY + y1m2, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            // category line
            color = slot.getTintColor();

            var x1m3 = Math.cos(begRad + innerGap) * categoryOuterEdge;
            var x2m3 = Math.cos(endRad - innerGap) * categoryOuterEdge;
            var y1m3 = Math.sin(begRad + innerGap) * categoryOuterEdge;
            var y2m3 = Math.sin(endRad - innerGap) * categoryOuterEdge;

            bufferBuilder.vertex(middleX + x1m1, middleY + y1m1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m1, middleY + y2m1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x2m3, middleY + y2m3, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            bufferBuilder.vertex(middleX + x1m3, middleY + y1m3, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            tesselator.end();
            RenderSystem.disableBlend();

            // icon
            var iconX = (x1 + x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            var iconY = (y1 + y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            gui.blit(region.slot().getIcon(), (int) Math.round(middleX + iconX - 8), (int) Math.round(middleY + iconY - 8), 16, 16, 0, 0, 18, 18, 18, 18);

        }

    }

    private void renderRadialButtonBackgrounds(GuiGraphics gui, int mouseX, int mouseY, List<Section> sections) {

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

                var isActivated = selectedButton != null && selectedButton.contains(entry);
                var isHovered = x1 <= mouseCenterX && x2 >= mouseCenterX && y1 <= mouseCenterY && y2 >= mouseCenterY;

                var color = RADIAL_BUTTON_COLOR_STATE.defaultColor();
                if (isActivated) color = RADIAL_BUTTON_COLOR_STATE.activedColor();
                if (isHovered) color = RADIAL_BUTTON_COLOR_STATE.hoveredColor();
                if (isActivated && isHovered) color = RADIAL_BUTTON_COLOR_STATE.activedHoveredColor();

                if (isHovered) {
//                    highlightedOption = option;
                    hoveredEntry = entry;
                }

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                var tesselator = Tesselator.getInstance();
                var bufferBuilder = tesselator.getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                bufferBuilder.vertex(middleX + x1, middleY + y1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x1, middleY + y2, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x2, middleY + y2, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                bufferBuilder.vertex(middleX + x2, middleY + y1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

                tesselator.end();
                RenderSystem.disableBlend();

                // icon
                gui.blit(entry.getIcon(), (int) Math.round(middleX + x - 8), (int) Math.round(middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);

            }
        }
    }

    public void renderTooltip(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        if (hoveredEntry == null /* highlightedOption != null && */) {
            return;
        }

        var tooltip = new ArrayList<Component>();

        // TODO: 23/6/23 keybinding
        tooltip.add(hoveredEntry.getCategoryComponent().copy().withStyle(ChatFormatting.WHITE));
        tooltip.add(hoveredEntry.getNameComponent().copy().withStyle(ChatFormatting.GOLD));

//            tooltip.add(Component.empty());

//        var keybind = findKeybind(button, currentBuildMode);
//            var keybindFormatted = "";
//        if (!keybind.isEmpty())
//            keybindFormatted = ChatFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";

        gui.renderComponentTooltip(minecraft.font, tooltip, mouseX, mouseY);
    }

    private boolean inTriangle(double x1, double y1, double x2, double y2,
                               double x3, double y3, double x, double y) {
        var ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        var bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        var ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return Mth.sign(ab) == Mth.sign(bc) && Mth.sign(bc) == Mth.sign(ca);
    }

    public void cycleBuildMode(Player player, boolean reverse) {
        // TODO: 23/5/23
//        setBuildMode(player, BuildMode.values()[(getBuildMode(player).ordinal() + 1) % BuildMode.values().length]);
//        Constructor.getInstance().reset(player);
    }

    record ColorState(
            Color disabledColor,
            Color defaultColor,
            Color hoveredColor,
            Color activedColor,
            Color activedHoveredColor
    ) {
    }

    private record Section(RadialButtonSet option, AxisDirection direction) {

        public List<Button> buttons() {
            return option.getEntries().stream().map((Button::new)).toList();
        }

    }

    private record Region(RadialSlot<?> slot) {
    }

    private record Button(RadialButton<?> entry) {
    }


//    private void drawRadialSlotTexts(PoseStack poseStack, Font font, double middleX, double middleY, ArrayList<Region> modes) {
//        for (var mode : modes) {
//            if (region.slot() != hoveredSlot) {
//                continue;
//            }
//            var region = mode.slot;
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
//        return keybindingIndex.getKeyMapping().key.getNameComponent();
//
////        if (currentBuildMode.options.length > 0) {
////            //Add (ctrl) to first two actions of first option
////            if (button.action == currentBuildMode.options[0].actions[0]
////                    || button.action == currentBuildMode.options[0].actions[1]) {
////                result = I18n.get(((KeyMappingAccessor) EffortlessClient.keyBindings[5]).getKey().getNameComponent());
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
