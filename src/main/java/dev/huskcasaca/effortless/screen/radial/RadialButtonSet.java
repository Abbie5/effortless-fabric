package dev.huskcasaca.effortless.screen.radial;

import net.minecraft.network.chat.Component;


public interface RadialButtonSet {

    static RadialButtonSet of(RadialButton<?>... entries) {
        return new RadialButtonSet() {
            @Override
            public Component getComponentName() {
                return null;
            }

            @Override
            public RadialButton<?>[] getEntries() {
                return entries;
            }
        };
    }

    Component getComponentName();

    RadialButton<?>[] getEntries();

}