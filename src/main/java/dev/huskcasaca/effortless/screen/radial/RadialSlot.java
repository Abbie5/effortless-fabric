package dev.huskcasaca.effortless.screen.radial;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;


public interface RadialSlot {

    Component getComponentName();

    ResourceLocation getIcon();

    Color getTintColor();

}
