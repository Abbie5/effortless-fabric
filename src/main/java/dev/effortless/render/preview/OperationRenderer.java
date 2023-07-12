package dev.effortless.render.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.operation.OperationResult;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OperationRenderer {

    private static final OperationRenderer INSTANCE = new OperationRenderer();
    private final Map<Object, Entry> results = Collections.synchronizedMap(new HashMap<>());
    //    private int soundTime = 0;

    public OperationRenderer() {
    }

    public static OperationRenderer getInstance() {
        return INSTANCE;
    }

    public void showResult(Object id, OperationResult result) {
        var entry = new Entry(result);
        results.put(id, entry);
    }

    public void renderOperationResults(PoseStack poseStack, MultiBufferSource multiBufferSource, float pt) {
        results.forEach((key, entry) -> {
            entry.getResult().render(poseStack, multiBufferSource);
        });
    }

    public void tick() {
        var iterator = results.values().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.tick();
            if (!entry.isAlive()) {
                iterator.remove();
            }
        }
    }

    public static class Entry {

        private static final int FADE_TICKS = 0;

        private final OperationResult result;
        private int ticksTillRemoval;

        public Entry(OperationResult outline) {
            this.result = outline;
            ticksTillRemoval = 5;
        }

        public void tick() {
            ticksTillRemoval--;
        }

        public boolean isAlive() {
            return ticksTillRemoval >= FADE_TICKS;
        }

        public boolean isFading() {
            return ticksTillRemoval < 0;
        }

        public OperationResult getResult() {
            return result;
        }

    }

}
