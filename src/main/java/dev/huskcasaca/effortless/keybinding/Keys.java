package dev.huskcasaca.effortless.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.core.keybinding.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public enum Keys {

    SETTINGS("settings", InputConstants.UNKNOWN.getValue()),
    BUILD_MODE_SETTINGS("build_mode_settings", InputConstants.UNKNOWN.getValue()),
    BUILD_MODIFIER_SETTINGS("build_modifier_settings", InputConstants.UNKNOWN.getValue()),
    ITEM_RANDOMIZER_SETTINGS("item_randomizer_settings", InputConstants.UNKNOWN.getValue()),

    BUILD_MODE_RADIAL("build_mode_radial", InputConstants.KEY_LALT),
    BUILD_MODIFIER_RADIAL("build_modifier_radial", InputConstants.KEY_LWIN),
    ITEM_RANDOMIZER_RADIAL("item_randomizer_radial", InputConstants.KEY_RALT),
    UNDO("undo", InputConstants.KEY_LBRACKET),
    REDO("redo", InputConstants.KEY_RBRACKET),
    //    CYCLE_REPLACE_MODE("cycle_replace", InputConstants.UNKNOWN.getValue()),
    TOGGLE_REPLACE("toggle_replace", InputConstants.UNKNOWN.getValue()),

//    TOGGLE_QUICK_REPLACE("toggle_quick_replace", InputConstants.UNKNOWN.getValue()),
//	TOGGLE_ALT_PLACE("toggle_alt_place", InputConstants.UNKNOWN.getValue()),
    ;

    private final String description;
    private final int key;
    private final boolean modifiable;
    private KeyMapping keyMapping;

    Keys(String description, int defaultKey) {
        this.description = String.join(".", "key", Effortless.MOD_ID, description, "desc");
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
    }

    public static void register() {
        for (Keys key : values()) {
            key.keyMapping = new KeyMapping(key.description, InputConstants.Type.KEYSYM, key.key, "key.effortless.category");
            if (!key.modifiable)
                continue;

            KeyBindingHelper.registerKeyBinding(key.keyMapping);
        }

    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance()
                .getWindow()
                .getWindow(), key);
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance()
                .getWindow()
                .getWindow(), button) == 1;
    }

    public static boolean ctrlDown() {
        return Screen.hasControlDown();
    }

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean altDown() {
        return Screen.hasAltDown();
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public boolean isDown() {
        if (!modifiable)
            return isKeyDown(key);
        return keyMapping.isDown();
    }

    public boolean isKeyDown() {
        if (!modifiable)
            return isKeyDown(key);
        return isKeyDown(keyMapping.key.getValue());
    }

    public String getBoundKey() {
        return keyMapping.getTranslatedKeyMessage()
                .getString()
                .toUpperCase();
    }

    public int getBoundCode() {
        return keyMapping.key
                .getValue();
    }

}
