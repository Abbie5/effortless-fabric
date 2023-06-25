package dev.huskcasaca.effortless.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildContext;
import dev.huskcasaca.effortless.building.EffortlessBuilder;
import dev.huskcasaca.effortless.building.operation.StructureOperation;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.screen.mode.EffortlessModeRadialScreen;
import dev.huskcasaca.effortless.screen.radial.RadialButton;
import dev.huskcasaca.effortless.utils.AnimationTicker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    private boolean hasLastMessage = false; // prevent clearing vanilla message

    private void renderBuildMode(PoseStack poseStack) {
        lastBuildInfoTextHeight = 0;
        var contentSide = ConfigManager.getGlobalPreviewConfig().getBuildInfoPosition().getAxis();
        if (contentSide == null) {
            return;
        }
        if (contentSide == Direction.AxisDirection.POSITIVE
                && (minecraft.options.showAutosaveIndicator().get()
                && (minecraft.gui.autosaveIndicatorValue > 0.0F
                || minecraft.gui.lastAutosaveIndicatorValue > 0.0F))
                && Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), minecraft.gui.lastAutosaveIndicatorValue, minecraft.gui.autosaveIndicatorValue), 0.0F, 1.0F)) > 8 || EffortlessModeRadialScreen.getInstance().isVisible()) {
            return;
        }
        var player = minecraft.player;
        var context = EffortlessBuilder.getInstance().getContext(player);

        if (context.buildMode().isDisabled()) {
            return;
        }

        var items = (List<ItemStack>) new ArrayList<ItemStack>();
        var result = EffortlessBuilder.getInstance().getLastResult();
        if (result instanceof StructureOperation.Result) {
            items = ((StructureOperation.Result) result).usages().sufficientItems();
        }

        var texts = new ArrayList<Component>();
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
//                            +` ChatFormatting.WHITE + "X" + modifier.arraySettings().offset().getX() + "Y" + modifier.arraySettings().offset().getY() + "Z" + modifier.arraySettings().offset().getZ() + " "
//                            + ChatFormatting.WHITE + "C" + ChatFormatting.WHITE + modifier.arraySettings().count() + ChatFormatting.RESET
//            ));
//        }
//

        texts.add(Component.literal(context.state().toString()));


        for (var supportedFeature : context.buildMode().getSupportedFeatures()) {
            var option = Arrays.stream(context.buildFeatures()).filter((feature) -> Objects.equals(feature.getCategory(), supportedFeature.getName())).findFirst();
            if (option.isEmpty()) continue;
            var button = RadialButton.option(option.get());
            texts.add(Component.literal(ChatFormatting.WHITE + button.getCategoryComponent().getString() + " " + ChatFormatting.GOLD + button.getNameComponent().getString() + ChatFormatting.RESET));
        }
        var replace = RadialButton.option(context.structureParams().replaceMode());
        texts.add(Component.literal(ChatFormatting.WHITE + replace.getCategoryComponent().getString() + " " + ChatFormatting.GOLD + replace.getNameComponent().getString() + ChatFormatting.RESET));

        texts.add(Component.literal(ChatFormatting.WHITE + "Structure " + ChatFormatting.GOLD + context.buildMode().getNameComponent().getString() + ChatFormatting.RESET));

        poseStack.pushPose();
        poseStack.translate(-1f * contentSide.getStep(), 0, 0);

        lastBuildInfoTextHeight = texts.size() * 10;
        var font = minecraft.font;

        var paddingX = 10;
        var paddingY = 2;

        var baseY = minecraft.getWindow().getGuiScaledHeight();
        var maxCol = 9;

        var textSizeX = texts.stream().mapToInt(font::width).max().orElse(0);
        var textSizeY = texts.size() * paddingX;
        var textBgSizeX = textSizeX + paddingX * 2;
        var textBgSizeY = textSizeY + 2 * paddingY + 2;
        var textBgPositionX = contentSide == Direction.AxisDirection.POSITIVE ? minecraft.getWindow().getGuiScaledWidth() : textBgSizeX;
        var textBgPositionY = baseY - 8;
        var textRenderPositionY = textBgPositionY - paddingY - 10;

        if (!texts.isEmpty()) {
            fill(poseStack, textBgPositionX, textBgPositionY, textBgPositionX - textBgSizeX, textBgPositionY - textBgSizeY, this.minecraft.options.getBackgroundColor(0.8F));
        }

        for (var text : texts) {
            var positionX = contentSide == Direction.AxisDirection.POSITIVE ? minecraft.getWindow().getGuiScaledWidth() - font.width(text) - 10 : 10;
            font.drawShadow(poseStack, text, positionX, textRenderPositionY, 16777215);
            textRenderPositionY -= 10;
        }

        var itemSizeX = Math.min(maxCol, items.size()) * ITEM_SPACING_X - 4;
        var itemSizeY = Mth.ceil(1f * items.size() / maxCol) * ITEM_SPACING_Y + (items.isEmpty() ? 0 : 6);
        var itemBgSizeX = itemSizeX + paddingX * 2;
        var itemBgSizeY = itemSizeY + 2 * paddingY - 4;
        var itemBgPositionX = contentSide == Direction.AxisDirection.POSITIVE ? minecraft.getWindow().getGuiScaledWidth() : itemBgSizeX;
        var itemBgPositionY = textRenderPositionY + 5;
        var itemRenderPositionY = itemBgPositionY - paddingY - ITEM_SPACING_Y;

        if (!items.isEmpty()) {
            fill(poseStack, itemBgPositionX, itemBgPositionY, itemBgPositionX - itemBgSizeX, itemBgPositionY - itemBgSizeY, this.minecraft.options.getBackgroundColor(0.8F));
        }

        var itemCol = 0;
        var itemRow = 0;

        for (var stack : items) {
            var width = (contentSide == Direction.AxisDirection.POSITIVE ? minecraft.getWindow().getGuiScaledWidth() - ITEM_SPACING_X - 8 : 8) - itemCol * ITEM_SPACING_X * contentSide.getStep();
            var height = itemRenderPositionY - itemRow * ITEM_SPACING_Y;
            minecraft.getItemRenderer().renderGuiItem(stack, width, height);
            minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, stack, width, height, Integer.toString(stack.getCount()));
            if (itemCol < maxCol - 1) {
                itemCol += 1;
            } else {
                itemCol = 0;
                itemRow += 1;
            }
        }

        poseStack.popPose();

    }

    public void render(PoseStack poseStack) {
        renderBuildMode(poseStack);
        renderActionBarMessage();
    }

    private void renderActionBarMessage() {

    }

    private void showMessage(Player player, BuildContext context, StructureOperation.Result result) {
        if (result.type().isSuccess()) {
            showBlockPlaceMessage(player, context, result);
        } else {
            showTracingFailedMessage(player, context);
        }
    }

    private void showBlockPlaceMessage(Player player, BuildContext context, StructureOperation.Result result) {
        var volume = result.size();

        var dimensions = "(";
        if (volume.getX() > 1) dimensions += volume.getX() + "x";
        if (volume.getZ() > 1) dimensions += volume.getZ() + "x";
        if (volume.getY() > 1) dimensions += volume.getY() + "x";
        dimensions = dimensions.substring(0, dimensions.length() - 1);
        if (dimensions.length() > 1) dimensions += ")";

        var blockCounter = "" + ChatFormatting.WHITE + result.usages().sufficientCount() + ChatFormatting.RESET + (result.usages().isFilled() ? " " : " + " + ChatFormatting.RED + result.usages().insufficientCount() + ChatFormatting.RESET + " ") + (result.usages().totalCount() == 1 ? "block" : "blocks");

        var buildingText = switch (context.state()) {
            case IDLE -> "idle";
            case PLACING -> "placing";
            case BREAKING -> "breaking";
        };

        displayMessage(player, "%s%s%s of %s %s %s".formatted(ChatFormatting.GOLD, context.getTranslatedModeOptionName(), ChatFormatting.RESET, buildingText, blockCounter, dimensions));
    }


    private void showTracingFailedMessage(Player player, BuildContext context) {
        displayMessage(player, "%s%s%s %s".formatted(ChatFormatting.GOLD, context.getTranslatedModeOptionName(), ChatFormatting.RESET, "cannot be traced"));
    }

    private void displayMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
        hasLastMessage = true;
    }

    private void clearMessage(Player player) {
        if (!hasLastMessage) return;
        player.displayClientMessage(Component.literal(""), true);
        hasLastMessage = false;
    }

    private int getGameTime() {
        return AnimationTicker.getTicks();
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
