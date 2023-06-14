package dev.huskcasaca.effortless.screen.radial;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.mode.BuildOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface RadialButton<T> {

    static <T> RadialButton<T> of(Component category, Component name, ResourceLocation icon, T option) {
        return new RadialButton<T>() {
            @Override
            public Component getNameComponent() {
                return name;
            }
            @Override
            public Component getCategoryComponent() {
                return category;
            }
            @Override
            public ResourceLocation getIcon() {
                return icon;
            }
            @Override
            public T getOption() {
                return option;
            }
            @Override
            public int hashCode() {
                int result = name != null ? name.hashCode() : 0;
                result = 31 * result + (category != null ? category.hashCode() : 0);
                result = 31 * result + (icon != null ? icon.hashCode() : 0);
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
                RadialButton<?> other = (RadialButton<?>) obj;
                if (getNameComponent() != null ? !getNameComponent().equals(other.getNameComponent()) : other.getNameComponent() != null) {
                    return false;
                }
                if (getCategoryComponent() != null ? !getCategoryComponent().equals(other.getCategoryComponent()) : other.getCategoryComponent() != null) {
                    return false;
                }
                if (getIcon() != null ? !getIcon().equals(other.getIcon()) : other.getIcon() != null) {
                    return false;
                }
                return true;
            }
        };
    }

    static <T extends BuildOption> RadialButton<T> option(T option) {
        return of(
                Component.translatable(String.join(".", Effortless.MOD_ID, "action", option.getName())),
                Component.translatable(String.join(".", Effortless.MOD_ID, "option", option.getCategory())),
                new ResourceLocation(Effortless.MOD_ID, "textures/option/" + option.getName() + ".png"),
                option
        );
    }

    Component getNameComponent();

    Component getCategoryComponent();

    ResourceLocation getIcon();

    T getOption();

}
