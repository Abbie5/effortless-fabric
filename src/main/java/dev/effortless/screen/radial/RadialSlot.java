package dev.effortless.screen.radial;

import dev.effortless.building.mode.BuildMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;


public interface RadialSlot<T> {

    static <T> RadialSlot<T> of(Component name, ResourceLocation icon, Color tintColor, T slot) {
        return new RadialSlot<T>() {
            @Override
            public Component getNameComponent() {
                return name;
            }

            @Override
            public ResourceLocation getIcon() {
                return icon;
            }

            @Override
            public Color getTintColor() {
                return tintColor;
            }

            @Override
            public T getSlot() {
                return slot;
            }

            @Override
            public int hashCode() {
                int result = name != null ? name.hashCode() : 0;
                result = 31 * result + (icon != null ? icon.hashCode() : 0);
                result = 31 * result + (tintColor != null ? tintColor.hashCode() : 0);
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null || getClass() != obj.getClass()) {
                    return false;
                }
                RadialSlot other = (RadialSlot) obj;
                if (getNameComponent() != null ? !getNameComponent().equals(other.getNameComponent()) : other.getNameComponent() != null) {
                    return false;
                }
                if (getIcon() != null ? !getIcon().equals(other.getIcon()) : other.getIcon() != null) {
                    return false;
                }
                return getTintColor() != null ? getTintColor().equals(other.getTintColor()) : other.getTintColor() == null;
            }
        };
    }

    static <T extends BuildMode> RadialSlot<T> mode(T mode) {
        return of(
                mode.getNameComponent(),
                mode.getIcon(),
                mode.getTintColor(),
                mode
        );
    }

    ResourceLocation getIcon();

    Color getTintColor();

    Component getNameComponent();

    T getSlot();

}
