package dev.effortless.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.Context;
import dev.effortless.building.mode.BuildMode;
import dev.effortless.building.operation.StructureOperationResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ActionBarOverlay extends GuiComponent {

    private static final ActionBarOverlay INSTANCE = new ActionBarOverlay();

    private boolean hasLastMessage = false; // prevent clearing vanilla message

    public static ActionBarOverlay getInstance() {
        return INSTANCE;
    }

    public static String getTranslatedModeOptionName(BuildMode mode) {
        var modeName = new StringBuilder();
        for (var option : mode.getSupportedFeatures()) {
            // TODO: 20/3/23
//            modeName.append(I18n.get(getOptionSetting(option).getNameKey()));
            modeName.append(" ");
        }
        return modeName + I18n.get(mode.getNameKey());
    }

    private void renderBuildMode(PoseStack poseStack) {
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

    }

    private void showMessage(Player player, Context context, StructureOperationResult result) {
        if (result.type().isSuccess()) {
            showBlockPlaceMessage(player, context, result);
        } else {
            showTracingFailedMessage(player, context);
        }
    }

    private void showBlockPlaceMessage(Player player, Context context, StructureOperationResult result) {
        var volume = result.size();

        var dimensions = "(";
        if (volume.getX() > 1) dimensions += volume.getX() + "x";
        if (volume.getZ() > 1) dimensions += volume.getZ() + "x";
        if (volume.getY() > 1) dimensions += volume.getY() + "x";
        dimensions = dimensions.substring(0, dimensions.length() - 1);
        if (dimensions.length() > 1) dimensions += ")";

//        var blockCounter = String.valueOf(ChatFormatting.WHITE) + result.usages().sufficientCount() + ChatFormatting.RESET + (result.usages().isFilled() ? " " : " + " + ChatFormatting.RED + result.usages().insufficientCount() + ChatFormatting.RESET + " ") + (result.usages().totalCount() == 1 ? "block" : "blocks");

        var buildingText = switch (context.state()) {
            case IDLE -> "idle";
            case PLACE_BLOCK -> "placing";
            case BREAK_BLOCK -> "breaking";
        };

//        displayMessage(player, "%s%s%s of %s %s %s".formatted(ChatFormatting.GOLD, context.getTranslatedModeOptionName(), ChatFormatting.RESET, buildingText, blockCounter, dimensions));
    }

    private void showTracingFailedMessage(Player player, Context context) {
        displayMessage(player, "%s%s%s %s".formatted(ChatFormatting.GOLD, getTranslatedModeOptionName(context.buildMode()), ChatFormatting.RESET, "cannot be traced"));
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

}
