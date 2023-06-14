package dev.huskcasaca.effortless.core.keybinding;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.KeyMapping;

public class KeyBindingHelper {

    public static KeyMapping registerKeyBinding(KeyMapping keyBinding) {
        return KeyBindingRegistryImpl.registerKeyBinding(keyBinding);
    }

}
