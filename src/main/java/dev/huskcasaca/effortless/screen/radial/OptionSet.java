package dev.huskcasaca.effortless.screen.radial;

import dev.huskcasaca.effortless.Effortless;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public interface OptionSet {

    Component getComponentName();

    Entry[] getEntries();

    interface Entry {

        String getName();

        default Component getComponentName() {
            return Component.translatable(String.join(".", Effortless.MOD_ID, "action", getName()));
        };

        default ResourceLocation getIcon(){
            return null;
        };
        default Entry[] getEntries() {
            return new Entry[]{};
        };

    }
}