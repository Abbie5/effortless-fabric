package dev.huskcasaca.effortless.screen.buildmode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import org.joml.Vector4f;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.*;
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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;

import static dev.huskcasaca.effortless.building.BuildActionHandler.*;

/**
 * Initially from Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RadialMenuScreen extends Screen {

    private static final RadialMenuScreen INSTANCE = new RadialMenuScreen();
    private static final Vector4f radialButtonColor = new Vector4f(0f, 0f, 0f, .5f);
    private static final Vector4f sideButtonColor = new Vector4f(.5f, .5f, .5f, .5f);
    private static final Vector4f highlightColor = new Vector4f(.6f, .8f, 1f, .6f);
    private static final Vector4f selectedColor = new Vector4f(0f, .5f, 1f, .5f);
    private static final Vector4f highlightSelectedColor = new Vector4f(0.2f, .7f, 1f, .7f);
    private static final int whiteTextColor = 0xffffffff;
    private static final int watermarkTextColor = 0x88888888;
    private static final int descriptionTextColor = 0xdd888888;
    private static final int optionTextColor = 0xeeeeeeff;
    private static final double ringInnerEdge = 38;
    private static final double ringOuterEdge = 75;
    private static final double categoryLineOuterEdge = 42;
    private static final double textDistance = 90;
    private static final double buttonDistance = 120;
    private static final float fadeSpeed = 0.3f;
    private static final int descriptionHeight = 100;

    public static final int MODE_OPTION_ROW_HEIGHT = 39;

    public BuildMode switchTo = null;
    public BuildAction doAction = null;
    public boolean performedActionUsingMouse;

    private float visibility;

    private BuildAction lastAction = null;

    public RadialMenuScreen() {
        super(Component.translatable("effortless.screen.radial_menu"));
    }

    public static RadialMenuScreen getInstance() {
        return INSTANCE;
    }

    public static void playRadialMenuSound() {
        final float volume = 0.1f;
        if (volume >= 0.0001f) {
            SimpleSoundInstance sound = new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, volume, 1.0f, RandomSource.create(), Minecraft.getInstance().player.blockPosition());
            Minecraft.getInstance().getSoundManager().play(sound);
        }
    }

    public boolean isVisible() {
        return Minecraft.getInstance().screen instanceof RadialMenuScreen;
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

        if (!Keys.SHOW_RADIAL_MENU.isKeyDown()) {
            onClose();
        }
    }

    @Override
    public void render(PoseStack ms, final int mouseX, final int mouseY, final float partialTicks) {
        BuildMode currentBuildMode = BuildModeHelper.getModeSettings(minecraft.player).buildMode();

        ms.pushPose();
//        ms.translate(0, 0, 200);

        visibility += fadeSpeed * partialTicks;
        if (visibility > 1f) visibility = 1f;

//        final int startColor = ((int) (visibility * 98) << 24) + 0x282828
//        final int endColor = ((int) (visibility * 128) << 24) + 0x282828;
//        fillGradient(ms, 0, 0, width, height, startColor, endColor);
        fill(ms, 0, 0, width, height, ((int) (visibility * 128) << 24) + 0x212121);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        final var tessellator = Tesselator.getInstance();
        final var buffer = tessellator.getBuilder();

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

//        -26 -13    0 -13
//        -26  13    0  13
//        -26  39    0  39

        //Add actions

        int baseY = -13;
        int buttonOffset = 26;

        buttons.add(new MenuButton(BuildAction.UNDO,     -buttonDistance - buttonOffset, baseY + 0,            Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REDO,     -buttonDistance - 0,            baseY + 0,            Direction.EAST));
        buttons.add(new MenuButton(BuildAction.MODIFIER, -buttonDistance - buttonOffset, baseY + buttonOffset, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REPLACE,  -buttonDistance - 0,            baseY + buttonOffset, Direction.EAST));

        //Add buildmode dependent options

        var options = currentBuildMode.getOptions();
        var optionsTexting = options.clone();
        var optionButtons = new ArrayList<MenuButton>();

        for (int row = 0; row < options.length; row++) {
            var buttonsInRow = new ArrayList<MenuButton>();
            for (int col = 0; col < options[row].getActions().length; col++) {
                var action = options[row].getActions()[col];
                var button = new MenuButton(action, buttonDistance + col * buttonOffset, options.length / -2f * MODE_OPTION_ROW_HEIGHT + 26 + row * MODE_OPTION_ROW_HEIGHT, Direction.DOWN);
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

        //Draw buildmode backgrounds
        drawRadialButtonBackgrounds(currentBuildMode, buffer, middleX, middleY, mouseXCenter, mouseYCenter, mouseRadians, ringInnerEdge, ringOuterEdge, quarterCircle, modes);

        //Draw action backgrounds
        drawSideButtonBackgrounds(buffer, middleX, middleY, mouseXCenter, mouseYCenter, buttons);

        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        drawIcons(ms, tessellator, buffer, middleX, middleY, ringInnerEdge, ringOuterEdge, modes, buttons);

        drawTexts(ms, currentBuildMode, middleX, middleY, textDistance, buttonDistance, modes, buttons, optionsTexting);

        ms.popPose();
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

    private void drawRadialButtonBackgrounds(BuildMode currentBuildMode, BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, double mouseRadians, double ringInnerEdge, double ringOuterEdge, double quarterCircle, ArrayList<ModeRegion> modes) {
        if (modes.isEmpty()) {
            return;
        }
        final int totalModes = Math.max(3, modes.size());
        final double fragment = Math.PI * 0.005; //gap between buttons in radians at inner edge
        final double fragment2 = Math.PI * 0.0025; //gap between buttons in radians at outer edge
        final double radiansPerObject = 2.0 * Math.PI / totalModes;

        for (int i = 0; i < modes.size(); i++) {
            ModeRegion modeRegion = modes.get(i);
            final double beginRadians = i * radiansPerObject - quarterCircle;
            final double endRadians = (i + 1) * radiansPerObject - quarterCircle;

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
            final boolean isHighlighted = beginRadians <= mouseRadians && mouseRadians <= endRadians && isMouseInQuad;

            Vector4f color = radialButtonColor;
            if (isSelected) color = selectedColor;
            if (isHighlighted) color = highlightColor;
            if (isSelected && isHighlighted) color = highlightSelectedColor;

            if (isHighlighted) {
                modeRegion.highlighted = true;
                switchTo = modeRegion.mode;
            }

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m2, middleY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x1m2, middleY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();

            //Category line
            color = modeRegion.mode.getCategory().getColor();

            final double x1m3 = Math.cos(beginRadians + fragment) * categoryLineOuterEdge;
            final double x2m3 = Math.cos(endRadians - fragment) * categoryLineOuterEdge;
            final double y1m3 = Math.sin(beginRadians + fragment) * categoryLineOuterEdge;
            final double y2m3 = Math.sin(endRadians - fragment) * categoryLineOuterEdge;

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m3, middleY + y2m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x1m3, middleY + y1m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
        }
    }

    private void drawSideButtonBackgrounds(BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, ArrayList<MenuButton> buttons) {
        for (final MenuButton btn : buttons) {

            final boolean isSelected = Arrays.stream(getOptions()).toList().contains(btn.action);

            final boolean isHighlighted = btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;

            Vector4f color = sideButtonColor;
            if (isSelected) color = selectedColor;
            if (isHighlighted) color = highlightColor;
            if (isSelected && isHighlighted) color = highlightSelectedColor;

            if (isHighlighted) {
                btn.highlighted = true;
                doAction = btn.action;
            }

            buffer.vertex(middleX + btn.x1, middleY + btn.y1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x1, middleY + btn.y2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
        }
    }

    private void drawIcons(PoseStack ms, Tesselator tessellator, BufferBuilder buffer, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons) {
        ms.pushPose();
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //Draw buildmode icons
        for (final ModeRegion modeRegion : modes) {

            final double x = (modeRegion.x1 + modeRegion.x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            final double y = (modeRegion.y1 + modeRegion.y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/mode/" + modeRegion.mode.name().toLowerCase() + ".png"));
            blit(ms, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        //Draw action icons
        for (final MenuButton button : buttons) {

            final double x = (button.x1 + button.x2) / 2 + 0.01;
            final double y = (button.y1 + button.y2) / 2 + 0.01;

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/action/" + button.action.name().toLowerCase() + ".png"));
            blit(ms, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        ms.popPose();
    }

    private void drawTexts(PoseStack ms, BuildMode currentBuildMode, double middleX, double middleY, double textDistance, double buttonDistance, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons, BuildMode.Option[] options) {
        //font.drawStringWithShadow("Actions", (int) (middleX - buttonDistance - 13) - font.getStringWidth("Actions") * 0.5f, (int) middleY - 38, 0xffffffff);

        //Draw option strings
        for (int row = 0; row < options.length; row++) {
            BuildMode.Option option = options[row];
            if (option == null) continue;
            font.drawShadow(ms, I18n.get(option.getNameKey()), (int) (middleX + buttonDistance - 9), (int) middleY + options.length / -2f * MODE_OPTION_ROW_HEIGHT + 3 + row * MODE_OPTION_ROW_HEIGHT, optionTextColor);
        }

        String credits = I18n.get("effortless.credits");
        font.drawShadow(ms, credits, width - font.width(credits) - 10, height - 15, watermarkTextColor);

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

                font.drawShadow(ms, text, (int) middleX + fixed_x, (int) middleY + fixed_y, whiteTextColor);

                //Draw description
                text = I18n.get(modeRegion.mode.getDescriptionKey());
                font.drawShadow(ms, text, (int) middleX - font.width(text) / 2f, (int) middleY + descriptionHeight, descriptionTextColor);
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
                        font.draw(ms, text, (int) (middleX + button.x1 - 8) - font.width(text), (int) (middleY + button.y1 + 6), whiteTextColor);
                    }
                    case EAST -> {
                        font.draw(ms, text, (int) (middleX + button.x2 + 8), (int) (middleY + button.y1 + 6), whiteTextColor);
                    }
                    case UP, NORTH -> {
                        font.draw(ms, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 - 26), whiteTextColor);
                        font.draw(ms, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 - 14), whiteTextColor);
                    }
                    case DOWN, SOUTH -> {
                        font.draw(ms, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 + 26), whiteTextColor);
                        font.draw(ms, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 + 38), whiteTextColor);
                    }
                }

            }
        }
    }

    private String findKeybind(MenuButton button, BuildMode currentBuildMode) {
        Keys keybindingIndex = null;
        if (button.action == BuildAction.REPLACE) keybindingIndex = Keys.TOGGLE_REPLACE;
        if (button.action == BuildAction.MODIFIER) keybindingIndex = Keys.MODIFIER_MENU;

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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        performAction(true);

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onClose() {
        //After onClose so it can open another screen
        if (!performedActionUsingMouse) {
            performAction(false);
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (lastAction == null) {
                BuildMode mode = BuildModeHelper.getModeSettings(player).buildMode();
                if (mode == BuildMode.DISABLE) {
                    Effortless.log(player, BuildModeHelper.getTranslatedModeOptionName(player), true);
                } else {
                    Effortless.log(player, ChatFormatting.GOLD + BuildModeHelper.getTranslatedModeOptionName(player) + ChatFormatting.RESET, true);
                }
            } else {
                var modeSettings = BuildModeHelper.getModeSettings(player);
                var modifierSettings = BuildModifierHelper.getModifierSettings(player);
                switch (lastAction) {
                    case UNDO -> {
                        Effortless.log(player, "Undo", true);
                    }
                    case REDO -> {
                        Effortless.log(player, "Redo", true);
                    }
                    case REPLACE -> {
                        Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (modifierSettings.enableReplace() ? (modifierSettings.enableQuickReplace() ? (ChatFormatting.GREEN + "QUICK") : (ChatFormatting.GREEN + "ON")) : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                    }
                    case MAGNET -> {
                        Effortless.log(player, ChatFormatting.GOLD + "Item Magnet " + ChatFormatting.RESET + (modeSettings.enableMagnet() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                    }
                }
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
        var player = Minecraft.getInstance().player;

        var modeSettings = BuildModeHelper.getModeSettings(player);

        if (switchTo != null) {
            playRadialMenuSound();

            lastAction = null;
            modeSettings = new ModeSettings(switchTo, modeSettings.enableMagnet());
            BuildModeHelper.setModeSettings(player, modeSettings);
            if (player != null) {
                BuildModeHandler.initializeMode(player);
            }
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(modeSettings));

            if (fromMouseClick) performedActionUsingMouse = true;
        }

        //Perform button action
        BuildAction action = doAction;
        if (action != null) {
            playRadialMenuSound();
            lastAction = action;

            BuildActionHandler.performAction(player, action);
            Packets.sendToServer(new ServerboundPlayerBuildActionPacket(action));

            if (fromMouseClick) performedActionUsingMouse = true;
        }
    }

    static class MenuButton {

        private final BuildAction action;
        private final String name;
        private final Direction textSide;
        private double x1, x2;
        private double y1, y2;
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

