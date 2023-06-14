package dev.huskcasaca.effortless.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.EffortlessBuilder;
import dev.huskcasaca.effortless.building.mode.BuildMode;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.render.preview.StructurePreviewRenderer;
import dev.huskcasaca.effortless.screen.mode.EffortlessModeRadialScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Direction;

import java.util.concurrent.atomic.AtomicInteger;

public class BuildInfoOverlay extends GuiComponent {

    private static final BuildInfoOverlay INSTANCE = new BuildInfoOverlay(Minecraft.getInstance());

    private static final int ITEM_SPACING_X = 18;
    private static final int ITEM_SPACING_Y = 18;
    private static int lastBuildInfoTextHeight = 0;
    private final Minecraft minecraft;

    public BuildInfoOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public static BuildInfoOverlay getInstance() {
        return INSTANCE;
    }

    public static int getLastRightEndTextHeight() {
        if (ConfigManager.getGlobalPreviewConfig().getBuildInfoPosition().getAxis() == Direction.AxisDirection.POSITIVE) {
            return lastBuildInfoTextHeight;
        } else {
            return 0;
        }
    }

    private void renderBuildMode(PoseStack poseStack) {
//        lastBuildInfoTextHeight = 0;
//        var textSide = ConfigManager.getGlobalPreviewConfig().getBuildInfoPosition().getAxis();
//        if (textSide == null) {
//            return;
//        }
//        if (textSide == Direction.AxisDirection.POSITIVE && (minecraft.options.showAutosaveIndicator().get() && (minecraft.gui.autosaveIndicatorValue > 0.0F || minecraft.gui.lastAutosaveIndicatorValue > 0.0F)) && Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), minecraft.gui.lastAutosaveIndicatorValue, minecraft.gui.autosaveIndicatorValue), 0.0F, 1.0F)) > 8) {
//            return;
//        }
//        if (EffortlessModeRadialScreen.getInstance().isVisible()) {
//            return;
//        }
//        var player = minecraft.player;
//        var mode = EffortlessBuilder.getInstance().getBuildMode(player);
//
//        if (mode == BuildMode.DISABLED) {
//            return;
//        }
//
//        var texts = new ArrayList<Component>();
//
//        var modifier = EffortlessBuilder.getInstance().getModifierSettings(player);
//
//        if (modifier.radialMirrorSettings().enabled()) {
//            texts.add(Component.literal(
//                    ChatFormatting.GOLD + "Radial Mirror" + ChatFormatting.RESET + " "
//                            + ChatFormatting.WHITE + "S" + modifier.radialMirrorSettings().slices() + " "
//                            + ChatFormatting.WHITE + "R" + ChatFormatting.WHITE + modifier.radialMirrorSettings().radius() + ChatFormatting.RESET
//            ));
//        }
//
//        if (modifier.mirrorSettings().enabled()) {
//            texts.add(Component.literal(
//                    ChatFormatting.GOLD + "Mirror" + ChatFormatting.RESET + " "
//                            + (modifier.mirrorSettings().mirrorX() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "X" + (modifier.mirrorSettings().mirrorY() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "Y" + (modifier.mirrorSettings().mirrorZ() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "Z" + " "
//                            + ChatFormatting.WHITE + "R" + ChatFormatting.WHITE + modifier.mirrorSettings().radius() + ChatFormatting.RESET
//            ));
//        }
//
//        if (modifier.arraySettings().enabled()) {
//            texts.add(Component.literal(
//                    ChatFormatting.GOLD + "Array" + ChatFormatting.RESET + " "
//                            + ChatFormatting.WHITE + "X" + modifier.arraySettings().offset().getX() + "Y" + modifier.arraySettings().offset().getY() + "Z" + modifier.arraySettings().offset().getZ() + " "
//                            + ChatFormatting.WHITE + "C" + ChatFormatting.WHITE + modifier.arraySettings().count() + ChatFormatting.RESET
//            ));
//        }
//
//        if (modifier.enableReplace()) {
//            texts.add(EffortlessBuilder.getInstance().getReplaceModeName(player));
//        }
//
//        texts.add(Component.literal(ChatFormatting.WHITE + EffortlessBuilder.getInstance().getTranslatedModeOptionName(player) + ChatFormatting.RESET));
//
//        lastBuildInfoTextHeight = texts.size() * 10;
//        var font = minecraft.font;
//        var positionY = minecraft.getWindow().getGuiScaledHeight() - 15;
//        for (Component text : texts) {
//            var positionX = textSide == Direction.AxisDirection.POSITIVE ? minecraft.getWindow().getGuiScaledWidth() - font.width(text) - 10 : 10;
//            font.drawShadow(poseStack, text, positionX, positionY, 16777215);
//            positionY -= 10;
//        }

    }

    public void render(PoseStack poseStack) {
        renderBuildMode(poseStack);
        renderBuildingStack(poseStack);
    }

    private void renderBuildingStack(PoseStack poseStack) {
        var itemSide = ConfigManager.getGlobalPreviewConfig().getItemUsagePosition().getAxis();
        if (itemSide == null) {
            return;
        }
        if (EffortlessModeRadialScreen.getInstance().isVisible()) {
            return;
        }
        var player = minecraft.player;
        var mode = EffortlessBuilder.getInstance().getContext(player).buildMode();

        if (mode == BuildMode.DISABLED) {
            return;
        }

        var itemUsages = StructurePreviewRenderer.getInstance().getCurrentPreview().usages();
        var sufficientItems = itemUsages.sufficientItems();
        var insufficientItems = itemUsages.insufficientItems();

        var defaultWidth = minecraft.getWindow().getGuiScaledWidth() / 2 + ((itemSide == Direction.AxisDirection.POSITIVE) ? 10 : -10 - ITEM_SPACING_X);
        var defaultHeight = minecraft.getWindow().getGuiScaledHeight() / 2 - 8 - (sufficientItems.size() + insufficientItems.size() - 1) / 9 * ITEM_SPACING_Y / 2;
        var positionX = new AtomicInteger(0);
        var positionY = new AtomicInteger(0);

        var sign = itemSide == Direction.AxisDirection.POSITIVE ? 1 : -1;

        for (var stack : sufficientItems) {
            var width = defaultWidth + positionX.get() * ITEM_SPACING_X * sign;
            var height = defaultHeight + positionY.get() * ITEM_SPACING_Y;
            minecraft.getItemRenderer().renderGuiItem(stack, width, height);
            minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, stack, width, height, Integer.toString(stack.getCount()));
            if (positionX.get() >= 8) {
                positionX.set(0);
                positionY.getAndIncrement();
            } else {
                positionX.getAndIncrement();
            }
        }

        for (var stack : insufficientItems) {
            var width = defaultWidth + positionX.get() * ITEM_SPACING_X * sign;
            var height = defaultHeight + positionY.get() * ITEM_SPACING_Y;
            minecraft.getItemRenderer().renderGuiItem(stack, width, height);
            minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, stack, width, height, ChatFormatting.RED + Integer.toString(stack.getCount()) + ChatFormatting.RESET);
            if (positionX.get() >= 8) {
                positionX.set(0);
                positionY.getAndIncrement();
            } else {
                positionX.getAndIncrement();
            }
        }

    }

    public enum Position {

        DISABLED("disabled"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        Position(String name) {
            this.name = name;
        }

        public String getNameKey() {
            // TODO: 15/9/22 use ResourceLocation
            return Effortless.MOD_ID + ".position." + name;
        }

        public Direction.AxisDirection getAxis() {
            return switch (this) {
                case LEFT -> Direction.AxisDirection.NEGATIVE;
                case RIGHT -> Direction.AxisDirection.POSITIVE;
                default -> null;
            };
        }
    }
}
