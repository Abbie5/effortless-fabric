package dev.effortless.event;

import dev.effortless.Effortless;
import dev.effortless.building.history.UndoRedoProvider;
import dev.effortless.building.pattern.randomizer.RandomizerSettings;
import dev.effortless.building.reach.ReachHelper;
import dev.effortless.core.event.client.ClientScreenInputEvent;
import dev.effortless.keybinding.Keys;
import dev.effortless.screen.config.EffortlessSettingsScreen;
import dev.effortless.screen.mode.EffortlessModeRadialScreen;
import dev.effortless.screen.mode.PlayerSettingsScreen;
import dev.effortless.screen.pattern.buildmodifier.EffortlessModifierSettingsScreen;
import dev.effortless.screen.pattern.randomizer.EffortlessRandomizerSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class InputEvents {

    public static void onKeyPress(int key, int scanCode, int action, int modifiers) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (Keys.BUILD_MODE_RADIAL.isDown()) {
            showBuildModelMenu();
        }
        if (Keys.BUILD_MODIFIER_RADIAL.isDown()) {
        }
        if (Keys.ITEM_RANDOMIZER_RADIAL.isDown()) {
        }
//        // remember to send packet to server if necessary
        if (Keys.BUILD_MODE_SETTINGS.getKeyMapping().consumeClick()) {
//            openModeSettings();
        }
        if (Keys.BUILD_MODIFIER_SETTINGS.getKeyMapping().consumeClick()) {
            openBuildModifierSettings();
        }
        if (Keys.ITEM_RANDOMIZER_SETTINGS.getKeyMapping().consumeClick()) {
            openItemRandomizerSettings();
        }
        if (Keys.UNDO.getKeyMapping().consumeClick()) {
            UndoRedoProvider.undo(player);
        }
        if (Keys.REDO.getKeyMapping().consumeClick()) {
            UndoRedoProvider.undo(player);
        }
        if (Keys.SETTINGS.getKeyMapping().consumeClick()) {
            openSettings();
        }
        if (Keys.TOGGLE_REPLACE.getKeyMapping().consumeClick()) {
            cycleReplaceMode(player);
        }
    }

    public static void cycleReplaceMode(Player player) {
        // TODO: 23/5/23
//        setReplaceMode(player, ReplaceMode.values()[(getReplaceMode(player).ordinal() + 1) % ReplaceMode.values().length]);
    }

    public static void showBuildModelMenu() {
        if (!EffortlessModeRadialScreen.getInstance().isVisible()) {
            Minecraft.getInstance().setScreen(EffortlessModeRadialScreen.getInstance());
        }
    }

    public static void openBuildModifierSettings() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        //Disabled if max reach is 0, might be set in the config that way.
        if (ReachHelper.getMaxReachDistance(player) == 0) {
            Effortless.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
        } else {
            Minecraft.getInstance().setScreen(null);
            Minecraft.getInstance().setScreen(new EffortlessModifierSettingsScreen());
        }
    }

    public static void openItemRandomizerSettings() {
        Minecraft.getInstance().setScreen(new EffortlessRandomizerSettingsScreen(
                Minecraft.getInstance().screen,
                (settings) -> {
                },
                RandomizerSettings.getSamples()
        ));
    }

    public static void openPlayerSettings() {
        Minecraft.getInstance().setScreen(new PlayerSettingsScreen());

    }

    public static void openSettings() {
        Minecraft.getInstance().setScreen(EffortlessSettingsScreen.createConfigScreen(Minecraft.getInstance().screen));
    }

    public static void register() {
        ClientScreenInputEvent.KEY_PRESS_EVENT.register(InputEvents::onKeyPress);
    }

}
