package dev.effortless.event;

import dev.effortless.building.settings.RandomizerSettings;
import dev.effortless.core.event.client.ClientScreenInputEvent;
import dev.effortless.keybinding.Keys;
import dev.effortless.screen.config.EffortlessSettingsScreen;
import dev.effortless.screen.mode.EffortlessModeRadialScreen;
import dev.effortless.screen.modifier.EffortlessModifierSettingsScreen;
import dev.effortless.screen.randomizer.EffortlessRandomizerSettingsScreen;
import net.minecraft.client.Minecraft;

public class InputEvents {

    private InputEvents() {
    }

    public static void onKeyPress(int key, int scanCode, int action, int modifiers) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (Keys.BUILD_MODE_RADIAL.isDown()) {
            openModeRadialScreen();
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
            openModifierSettingsScreen();
        }
        if (Keys.ITEM_RANDOMIZER_SETTINGS.getKeyMapping().consumeClick()) {
            openRandomizerSettingsScreen();
        }
        if (Keys.UNDO.getKeyMapping().consumeClick()) {

        }
        if (Keys.REDO.getKeyMapping().consumeClick()) {

        }
        if (Keys.SETTINGS.getKeyMapping().consumeClick()) {
            openSettingsScreen();
        }
        if (Keys.TOGGLE_REPLACE.getKeyMapping().consumeClick()) {
//            cycleReplaceMode(player);
        }
    }

    public static void openModeRadialScreen() {
        if (!EffortlessModeRadialScreen.getInstance().isVisible()) {
            Minecraft.getInstance().setScreen(EffortlessModeRadialScreen.getInstance());
        }
    }

    public static void openModifierSettingsScreen() {
        Minecraft.getInstance().setScreen(new EffortlessModifierSettingsScreen());
//        var player = Minecraft.getInstance().player;
//        if (player == null) return;
//
//        //Disabled if max reach is 0, might be set in the config that way.
//        if (ReachHelper.getMaxReachDistance(player) == 0) {
//            Effortless.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
//        } else {
//            Minecraft.getInstance().setScreen(null);
//            Minecraft.getInstance().setScreen(new EffortlessModifierSettingsScreen());
//        }
    }

    public static void openRandomizerSettingsScreen() {
        Minecraft.getInstance().setScreen(new EffortlessRandomizerSettingsScreen(
                Minecraft.getInstance().screen,
                (settings) -> {
                },
                RandomizerSettings.getSamples()
        ));
    }

    public static void openSettingsScreen() {
        Minecraft.getInstance().setScreen(EffortlessSettingsScreen.createConfigScreen(Minecraft.getInstance().screen));
    }

    public static void register() {
        ClientScreenInputEvent.KEY_PRESS_EVENT.register(InputEvents::onKeyPress);
    }

}
