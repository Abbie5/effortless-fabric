package dev.huskcasaca.effortless.screen.buildmode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerBuildActionPacket;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerSetBuildModePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static dev.huskcasaca.effortless.building.BuildActionHandler.getOptions;

/**
 * Initially from Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */
@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EffortlessModeRadialScreen extends Screen {

    private static final EffortlessModeRadialScreen INSTANCE = new EffortlessModeRadialScreen();
    private static final Color SIDE_BUTTON_COLOR = new Color(.33f, .33f, .33f, .5f);
    private static final Color RADIAL_BUTTON_COLOR = new Color(0f, 0f, 0f, .5f);
    private static final Color SELECTED_COLOR = new Color(.5f, .5f, .5f, .78f);
    private static final Color HIGHLIGHT_COLOR = new Color(0.42f, 0.42f, 0.42f,  0.5f);
    private static final Color HIGHLIGHT_SELECTED_COLOR = new Color(0.60f, 0.60f, 0.60f, .78f);
    private static final int WHITE_TEXT_COLOR = 0xffffffff;
    private static final int WATERMARK_TEXT_COLOR = 0x8d7f7f7f;
    private static final int OPTION_TEXT_COLOR = 0xeeeeeeff;
    private static final double RING_INNER_EDGE = 36;
    private static final double RING_OUTER_EDGE = 74;
    private static final double CATEGORY_LINE_OUTER_EDGE = 40;
    private static final double TEXT_DISTANCE = 84;
    private static final double BUTTON_DISTANCE = 112;
    private static final float FADE_SPEED = 0.5f;
    private static final int MODE_OPTION_ROW_HEIGHT = 39;
    private static final float MOUSE_SCROLL_THRESHOLD = 1;

    private float visibility;
    public BuildMode switchTo = null;
    private BuildAction lastAction = null;
    public BuildAction doAction = null;
    public boolean performedActionUsingMouse;
    private float lastScrollOffset = 0;

    public EffortlessModeRadialScreen() {
        super(Component.translatable(String.join(".", Effortless.MOD_ID, "screen", "radial_menu")));
    }

    public static EffortlessModeRadialScreen getInstance() {
        return INSTANCE;
    }

    public static void playRadialMenuSound() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public boolean isVisible() {
        return Minecraft.getInstance().screen instanceof EffortlessModeRadialScreen;
    }

    @Override
    protected void init() {
        super.init();
        performedActionUsingMouse = false;
        visibility = 0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (!Keys.BUILD_MODE_RADIAL.isKeyDown()) {
            onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        visibility = Math.min(visibility + FADE_SPEED * partialTicks, 1f);
        if (minecraft != null && minecraft.level != null) {
            fillGradient(poseStack, 0, 0, this.width, this.height, (int) (visibility * 0xC0) << 24 | 0x101010, (int) (visibility * 0xD0) << 24 | 0x101010);
        } else {
            this.renderDirtBackground(0);
        }

        BuildMode currentBuildMode = BuildModeHelper.getModeSettings(minecraft.player).buildMode();

        poseStack.pushPose();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        final var tesselator = Tesselator.getInstance();
        final var buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        final double middleX = width / 2.0;
        final double middleY = height / 2.0;

        //Fix for high def (retina) displays: use custom mouse coordinates
        //Borrowed from GameRenderer::updateCameraAndRender
        int mouseXX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
        int mouseYY = (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());

        final double mouseXCenter = mouseXX - middleX;
        final double mouseYCenter = mouseYY - middleY;
        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        final double quarterCircle = Math.PI / 2.0;

        if (mouseRadians < -quarterCircle) {
            mouseRadians = mouseRadians + Math.PI * 2;
        }

        final ArrayList<ModeRegion> modes = new ArrayList<>();
        final ArrayList<MenuButton> buttons = new ArrayList<>();

        //Add build modes
        for (final BuildMode mode : BuildMode.values()) {
            modes.add(new ModeRegion(mode));
        }

        //Add actions
        int baseY = -13;
        int buttonOffset = 26;

        buttons.add(new MenuButton(BuildAction.UNDO, -BUTTON_DISTANCE - buttonOffset, baseY, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REDO, -BUTTON_DISTANCE - 0, baseY, Direction.EAST));
        buttons.add(new MenuButton(BuildAction.MODIFIER, -BUTTON_DISTANCE - buttonOffset, baseY + buttonOffset, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REPLACE, -BUTTON_DISTANCE - 0, baseY + buttonOffset, Direction.EAST));

        //Add buildmode dependent options
        var options = currentBuildMode.getOptions();
        var optionsTexting = options.clone();
        var optionButtons = new ArrayList<MenuButton>();

        for (int row = 0; row < options.length; row++) {
            var buttonsInRow = new ArrayList<MenuButton>();
            for (int col = 0; col < options[row].getActions().length; col++) {
                var action = options[row].getActions()[col];
                var button = new MenuButton(action, BUTTON_DISTANCE + col * buttonOffset, options.length / -2f * MODE_OPTION_ROW_HEIGHT + 26 + row * MODE_OPTION_ROW_HEIGHT, Direction.DOWN);
                buttons.add(button);
                optionButtons.add(button);
                buttonsInRow.add(button);
            }
            if (isButtonHighlighted(buttonsInRow, mouseXCenter, mouseYCenter) && row + 1 < options.length) {
                optionsTexting[row + 1] = null;
            }
        }

        switchTo = null;
        doAction = null;

        var innerEdge = RING_INNER_EDGE * 0.72f + RING_INNER_EDGE * visibility * 0.28f;
        var outerEdge = RING_OUTER_EDGE * 0.5f + RING_OUTER_EDGE * visibility * 0.5f;
        var categoryOuterEdge = CATEGORY_LINE_OUTER_EDGE * 0.72f + CATEGORY_LINE_OUTER_EDGE * visibility * 0.28f;
        //Draw buildmode backgrounds
        drawRadialButtonBackgrounds(currentBuildMode, buffer, middleX, middleY, mouseXCenter, mouseYCenter, mouseRadians, innerEdge, outerEdge, categoryOuterEdge, quarterCircle, modes);

        //Draw action backgrounds
        drawSideButtonBackgrounds(buffer, middleX, middleY, mouseXCenter, mouseYCenter, buttons);

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        drawIcons(poseStack, tesselator, buffer, middleX, middleY, innerEdge, outerEdge, modes, buttons);

        drawTexts(poseStack, currentBuildMode, middleX, middleY, TEXT_DISTANCE, BUTTON_DISTANCE, modes, buttons, optionsTexting);

        poseStack.popPose();
    }

    private boolean isButtonHighlighted(MenuButton btn, double mouseXCenter, double mouseYCenter) {
        return btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;
    }

    private boolean isButtonHighlighted(ArrayList<MenuButton> btns, double mouseXCenter, double mouseYCenter) {
        for (var btn : btns) {
            if (isButtonHighlighted(btn, mouseXCenter, mouseYCenter)) {
                return true;
            }
        }
        return false;

    }

    private boolean isMouseInButtonGroup(ArrayList<MenuButton> btns, double mouseXCenter, double mouseYCenter) {
        if (btns.isEmpty()) return false;


        return btns.stream().map(btn -> btn.x1).min(Double::compare).get() <= mouseXCenter && btns.stream().map(btn -> btn.x2).max(Double::compare).get() >= mouseXCenter && btns.stream().map(btn -> btn.y1).min(Double::compare).get() <= mouseYCenter && btns.stream().map(btn -> btn.y2).max(Double::compare).get() >= mouseYCenter;
    }

    private void drawRadialButtonBackgrounds(BuildMode currentBuildMode, BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, double mouseRadians, double ringInnerEdge, double ringOuterEdge, double categoryOuterEdge, double quarterCircle, ArrayList<ModeRegion> modes) {
        if (modes.isEmpty()) {
            return;
        }
        final int totalModes = Math.max(3, modes.size());
        final double fragment = Math.PI * 0.005; //gap between buttons in radians at inner edge
        final double fragment2 = Math.PI * 0.0025; //gap between buttons in radians at outer edge
        final double radiansPerObject = 2.0 * Math.PI / totalModes;

        for (int i = 0; i < modes.size(); i++) {
            ModeRegion modeRegion = modes.get(i);
            final double beginRadians = (i - 0.5) * radiansPerObject - quarterCircle;
            final double endRadians = (i + 0.5) * radiansPerObject - quarterCircle;

            modeRegion.x1 = Math.cos(beginRadians);
            modeRegion.x2 = Math.cos(endRadians);
            modeRegion.y1 = Math.sin(beginRadians);
            modeRegion.y2 = Math.sin(endRadians);

            final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
            final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
            final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
            final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

            final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
            final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
            final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
            final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

            final boolean isSelected = currentBuildMode.ordinal() == i;
            final boolean isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseXCenter, mouseYCenter) || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseXCenter, mouseYCenter);
            final boolean isHighlighted = ((beginRadians <= mouseRadians && mouseRadians <= endRadians) || (beginRadians <= (mouseRadians - 2 * Math.PI) && (mouseRadians - 2 * Math.PI) <= endRadians)) && isMouseInQuad;

            var color = RADIAL_BUTTON_COLOR;
            if (isSelected) color = SELECTED_COLOR;
            if (isHighlighted) color = HIGHLIGHT_COLOR;
            if (isSelected && isHighlighted) color = HIGHLIGHT_SELECTED_COLOR;

            if (isHighlighted) {
                modeRegion.highlighted = true;
                switchTo = modeRegion.mode;
            }

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x2m2, middleY + y2m2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x1m2, middleY + y1m2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

            //Category line
            color = modeRegion.mode.getCategory().getColor();

            final double x1m3 = Math.cos(beginRadians + fragment) * categoryOuterEdge;
            final double x2m3 = Math.cos(endRadians - fragment) * categoryOuterEdge;
            final double y1m3 = Math.sin(beginRadians + fragment) * categoryOuterEdge;
            final double y2m3 = Math.sin(endRadians - fragment) * categoryOuterEdge;

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x2m3, middleY + y2m3, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + x1m3, middleY + y1m3, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }
    }

    private void drawSideButtonBackgrounds(BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, ArrayList<MenuButton> buttons) {
        for (final MenuButton btn : buttons) {

            final boolean isSelected = Arrays.stream(getOptions()).toList().contains(btn.action);

            final boolean isHighlighted = btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;

            var color = SIDE_BUTTON_COLOR;
            if (isSelected) color = SELECTED_COLOR;
            if (isHighlighted) color = HIGHLIGHT_COLOR;
            if (isSelected && isHighlighted) color = HIGHLIGHT_SELECTED_COLOR;

            if (isHighlighted) {
                btn.highlighted = true;
                doAction = btn.action;
            }

            buffer.vertex(middleX + btn.x1, middleY + btn.y1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + btn.x1, middleY + btn.y2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y2, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y1, getBlitOffset()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }
    }

    private void drawIcons(PoseStack poseStack, Tesselator tesselator, BufferBuilder buffer, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons) {
        poseStack.pushPose();
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //Draw buildmode icons
        for (final ModeRegion modeRegion : modes) {

            final double x = (modeRegion.x1 + modeRegion.x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            final double y = (modeRegion.y1 + modeRegion.y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/mode/" + modeRegion.mode.getName() + ".png"));
            blit(poseStack, (int) Math.round(middleX + x - 8), (int) Math.round(middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        //Draw action icons
        for (final MenuButton button : buttons) {

            final double x = (button.x1 + button.x2) / 2;
            final double y = (button.y1 + button.y2) / 2;

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/action/" + button.action.getName() + ".png"));
            blit(poseStack, (int) Math.round(middleX + x - 8), (int) Math.round(middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        poseStack.popPose();
    }

    private void drawTexts(PoseStack poseStack, BuildMode currentBuildMode, double middleX, double middleY, double textDistance, double buttonDistance, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons, BuildMode.Option[] options) {
        //font.drawStringWithShadow("Actions", (int) (middleX - buttonDistance - 13) - font.getStringWidth("Actions") * 0.5f, (int) middleY - 38, 0xffffffff);

        //Draw option strings
        for (int row = 0; row < options.length; row++) {
            BuildMode.Option option = options[row];
            if (option == null) continue;
            font.drawShadow(poseStack, I18n.get(option.getNameKey()), (int) (middleX + buttonDistance - 9), (int) middleY + options.length / -2f * MODE_OPTION_ROW_HEIGHT + 3 + row * MODE_OPTION_ROW_HEIGHT, OPTION_TEXT_COLOR);
        }

        String credits = I18n.get(String.join(".", Effortless.MOD_ID, "building", "credits"));
        font.drawShadow(poseStack, credits, width - font.width(credits) - 10, height - 15, WATERMARK_TEXT_COLOR);

        //Draw buildmode text
        for (final ModeRegion modeRegion : modes) {

            if (modeRegion.highlighted) {
                final double x = (modeRegion.x1 + modeRegion.x2) * 0.5;
                final double y = (modeRegion.y1 + modeRegion.y2) * 0.5;

                int fixed_x = (int) (x * textDistance);
                int fixed_y = (int) (y * textDistance) - font.lineHeight / 2;
                String text = I18n.get(modeRegion.mode.getNameKey());

                if (x <= -0.2) {
                    fixed_x -= font.width(text);
                } else if (-0.2 <= x && x <= 0.2) {
                    fixed_x -= font.width(text) / 2;
                }

                font.drawShadow(poseStack, text, (int) middleX + fixed_x, (int) middleY + fixed_y, WHITE_TEXT_COLOR);
            }
        }

        //Draw action text
        for (final MenuButton button : buttons) {
            if (button.highlighted) {
                String text = ChatFormatting.AQUA + button.name;

                //Add keybind in brackets
                String keybind = findKeybind(button, currentBuildMode);
                String keybindFormatted = "";
                if (!keybind.isEmpty())
                    keybindFormatted = ChatFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";

                switch (button.textSide) {
                    case WEST -> {
                        font.draw(poseStack, text, (int) (middleX + button.x1 - 8) - font.width(text), (int) (middleY + button.y1 + 6), WHITE_TEXT_COLOR);
                    }
                    case EAST -> {
                        font.draw(poseStack, text, (int) (middleX + button.x2 + 8), (int) (middleY + button.y1 + 6), WHITE_TEXT_COLOR);
                    }
                    case UP, NORTH -> {
                        font.draw(poseStack, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 - 26), WHITE_TEXT_COLOR);
                        font.draw(poseStack, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 - 14), WHITE_TEXT_COLOR);
                    }
                    case DOWN, SOUTH -> {
                        font.draw(poseStack, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 + 26), WHITE_TEXT_COLOR);
                        font.draw(poseStack, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 + 38), WHITE_TEXT_COLOR);
                    }
                }

            }
        }
    }

    private String findKeybind(MenuButton button, BuildMode currentBuildMode) {
        Keys keybindingIndex = null;

        switch (button.action) {
            case SETTINGS -> keybindingIndex = Keys.SETTINGS;
            case MODIFIER -> keybindingIndex = Keys.BUILD_MODIFIER_SETTINGS;
            case UNDO -> keybindingIndex = Keys.UNDO;
            case REDO -> keybindingIndex = Keys.REDO;
            case REPLACE -> keybindingIndex = Keys.TOGGLE_REPLACE;
        }

        if (keybindingIndex == null) {
            return "";
        }

        return keybindingIndex.getKeyMapping().key.getName();

//        if (currentBuildMode.options.length > 0) {
//            //Add (ctrl) to first two actions of first option
//            if (button.action == currentBuildMode.options[0].actions[0]
//                    || button.action == currentBuildMode.options[0].actions[1]) {
//                result = I18n.get(((KeyMappingAccessor) EffortlessClient.keyBindings[5]).getKey().getName());
//                if (result.equals("Left Control")) result = "Ctrl";
//            }
//        }
    }

    private boolean inTriangle(final double x1, final double y1, final double x2, final double y2,
                               final double x3, final double y3, final double x, final double y) {
        final double ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        final double bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        final double ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return sign(ab) == sign(bc) && sign(bc) == sign(ca);
    }

    private int sign(final double n) {
        return n > 0 ? 1 : -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        performAction(true);
        return super.mouseClicked(mouseX, mouseY, button);
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

    public void onClose() {
        //After onClose so it can open another screen
        if (!performedActionUsingMouse) {
            performAction(false);
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (lastAction == null) {
//                BuildMode mode = BuildModeHelper.getModeSettings(player).buildMode();
//                if (mode == BuildMode.DISABLE) {
//                    Effortless.log(player, BuildModeHelper.getTranslatedModeOptionName(player), true);
//                } else {
//                    Effortless.log(player, ChatFormatting.GOLD + BuildModeHelper.getTranslatedModeOptionName(player) + ChatFormatting.RESET, true);
//                }
            } else {
                var modeSettings = BuildModeHelper.getModeSettings(player);
                var modifierSettings = BuildModifierHelper.getModifierSettings(player);
                switch (lastAction) {
                    case UNDO -> Effortless.log(player, "Undo", true);
                    case REDO -> Effortless.log(player, "Redo", true);
                    case REPLACE ->
                            Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (modifierSettings.enableReplace() ? (modifierSettings.enableQuickReplace() ? (ChatFormatting.GREEN + "QUICK") : (ChatFormatting.GREEN + "ON")) : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                    case MAGNET ->
                            Effortless.log(player, ChatFormatting.GOLD + "Item Magnet " + ChatFormatting.RESET + (modeSettings.enableMagnet() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                }
                lastAction = null;
            }
        }
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
//        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void performAction(boolean fromMouseClick) {
        var player = minecraft.player;

        var modeSettings = BuildModeHelper.getModeSettings(player);

        if (switchTo != null) {
            playRadialMenuSound();

            lastAction = null;
            modeSettings = new ModeSettings(switchTo, modeSettings.enableMagnet());
            BuildModeHelper.setModeSettings(player, modeSettings);
            if (player != null) {
                BuildModeHandler.reset(player);
            }
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(modeSettings));

            if (fromMouseClick) {
                performedActionUsingMouse = true;
            }
        }

        //Perform button action
        BuildAction action = doAction;
        if (action != null) {
            playRadialMenuSound();
            lastAction = action;

            BuildActionHandler.performAction(player, action);
            Packets.sendToServer(new ServerboundPlayerBuildActionPacket(action));

            if (fromMouseClick) {
                performedActionUsingMouse = true;
            }
            switch (action) {
                case UNDO, REDO, SETTINGS, REPLACE -> super.onClose();
            }
        }
    }

    static class MenuButton {

        private final BuildAction action;
        private final String name;
        private final Direction textSide;
        private final double x1;
        private final double x2;
        private final double y1;
        private final double y2;
        private boolean highlighted;

        public MenuButton(final BuildAction action, final double x, final double y,
                          final Direction textSide) {
            this.name = I18n.get(action.getNameKey());
            this.action = action;
            this.x1 = x - 10;
            this.x2 = x + 10;
            this.y1 = y - 10;
            this.y2 = y + 10;
            this.textSide = textSide;
        }

    }

    static class ModeRegion {

        private final BuildMode mode;
        private double x1, x2;
        private double y1, y2;
        private boolean highlighted;

        public ModeRegion(final BuildMode mode) {
            this.mode = mode;
        }

    }

}
