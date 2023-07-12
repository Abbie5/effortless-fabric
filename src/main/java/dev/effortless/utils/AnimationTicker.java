package dev.effortless.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public class AnimationTicker {

    private static final AnimationTicker INSTANCE = new AnimationTicker();
    private static int ticks;
    private static int pausedTicks;

    public static AnimationTicker getInstance() {
        return INSTANCE;
    }

    public void reset() {
        ticks = 0;
        pausedTicks = 0;
    }

    public void tick() {
        if (!Minecraft.getInstance().isPaused()) {
            ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
        } else {
            pausedTicks = (pausedTicks + 1) % 1_728_000;
        }
    }

    public int getTicks() {
        return getTicks(false);
    }

    public int getTicks(boolean includePaused) {
        return includePaused ? ticks + pausedTicks : ticks;
    }

    public float getRenderTime() {
        return getTicks() + getPartialTicks();
    }

    public float getPartialTicks() {
        var minecraft = Minecraft.getInstance();
        return (minecraft.isPaused() ? minecraft.pausePartialTick : minecraft.getFrameTime());
    }

    public int getTicks(LevelAccessor world) {
        return getTicks();
    }

    public float getRenderTime(LevelAccessor world) {
        return getTicks(world) + getPartialTicks(world);
    }

    public float getPartialTicks(LevelAccessor world) {
        return getPartialTicks();
    }
}

