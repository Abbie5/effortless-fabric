package dev.effortless.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ScissorsHandler {

    private static final List<Rectangle> scissorsAreas = Lists.newArrayList();

    public static void clearScissors() {
        scissorsAreas.clear();
        applyScissors();
    }

    public static List<Rectangle> getScissorsAreas() {
        return Collections.unmodifiableList(scissorsAreas);
    }

    public static void scissor(Rectangle rectangle) {
        scissorsAreas.add(rectangle);
        applyScissors();
    }

    public static void removeLastScissor() {
        if (!scissorsAreas.isEmpty()) {
            scissorsAreas.remove(scissorsAreas.size() - 1);
        }
        applyScissors();
    }

    public static void applyScissors() {
        if (!scissorsAreas.isEmpty()) {
            var rectangle = (Rectangle) scissorsAreas.get(0).clone();

            for (int i = 1; i < scissorsAreas.size(); ++i) {
                Rectangle r1 = scissorsAreas.get(i);
                if (!rectangle.intersects(r1)) {
                    applyScissorInternal(new Rectangle());
                    return;
                }
                rectangle.setBounds(rectangle.intersection(r1));
            }

            rectangle.setBounds(Math.min(rectangle.x, rectangle.x + rectangle.width), Math.min(rectangle.y, rectangle.y + rectangle.height), Math.abs(rectangle.width), Math.abs(rectangle.height));
            applyScissorInternal(rectangle);
        } else {
            applyScissorInternal(null);
        }

    }

    private static void applyScissorInternal(Rectangle r) {
        if (r != null) {
            GlStateManager._enableScissorTest();
            if (r.isEmpty()) {
                GlStateManager._scissorBox(0, 0, 0, 0);
            } else {
                Window window = Minecraft.getInstance().getWindow();
                double scaleFactor = window.getGuiScale();
                GlStateManager._scissorBox((int) ((double) r.x * scaleFactor), (int) ((double) (window.getGuiScaledHeight() - r.height - r.y) * scaleFactor), (int) ((double) r.width * scaleFactor), (int) ((double) r.height * scaleFactor));
            }
        } else {
            GlStateManager._disableScissorTest();
        }

    }
}
