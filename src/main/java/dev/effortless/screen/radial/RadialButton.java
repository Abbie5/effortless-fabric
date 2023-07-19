package dev.effortless.screen.radial;

import dev.effortless.Effortless;
import dev.effortless.building.base.Option;
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
                return getIcon() != null ? getIcon().equals(other.getIcon()) : other.getIcon() == null;
            }
        };
    }

    static <T extends Option> RadialButton<T> option(T option) {
        return of(
                Component.translatable(Effortless.asKey("option", option.getCategory())),
                Component.translatable(Effortless.asKey("action", option.getName())),
                Effortless.asResource("textures/option/" + option.getName() + ".png"),
                option
        );
    }

    Component getNameComponent();

    Component getCategoryComponent();

    ResourceLocation getIcon();

    T getOption();

}
