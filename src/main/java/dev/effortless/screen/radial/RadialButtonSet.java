package dev.effortless.screen.radial;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface RadialButtonSet {

    static RadialButtonSet of(RadialButton<?>... entries) {
        return of(List.of(entries));
    }

    static RadialButtonSet of(List<? extends RadialButton<?>> entries) {
        return new RadialButtonSet() {
            @Override
            public Component getComponentName() {
                return null;
            }

            @Override
            public List<? extends RadialButton<?>> getEntries() {
                return entries;
            }
        };
    }

    Component getComponentName();

    List<? extends RadialButton<?>> getEntries();

}