package dev.effortless.renderer.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.building.operation.*;
import dev.effortless.renderer.preview.result.BlockResultRenderer;
import dev.effortless.renderer.preview.result.ResultRenderer;
import dev.effortless.renderer.preview.result.StructureResultRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OperationRenderer {

    private static final OperationRenderer INSTANCE = new OperationRenderer();
    private final Map<Object, Entry> results = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<? extends Operation<?>>, ResultRenderer> resultRendererMap = Collections.synchronizedMap(new HashMap<>());

    public OperationRenderer() {
        registerRenderers();
    }

    private <O extends Operation<R>, R extends OperationResult<R>> void registerRenderer(Class<O> clazz, ResultRenderer<R> renderer) {
        resultRendererMap.put(clazz, renderer);
    }

    public ResultRenderer getRenderer(Operation<?> operation) {
        return resultRendererMap.get(operation.getClass());
    }

    public void renderResult(PoseStack poseStack, MultiBufferSource multiBufferSource, OperationResult<?> result) {
        var renderer = resultRendererMap.get(result.operation().getClass());
        if (renderer != null) {
            renderer.render(poseStack, multiBufferSource, result);
        }
    }

    public void registerRenderers() {
        registerRenderer(BlockPlaceOperation.class, new BlockResultRenderer.Place());
        registerRenderer(BlockBreakOperation.class, new BlockResultRenderer.Break());
        registerRenderer(BlockBreakOperation.class, new BlockResultRenderer.Break());
        registerRenderer(StructureBuildOperation.class, new StructureResultRenderer());
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
            renderResult(poseStack, multiBufferSource, entry.getResult());
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
