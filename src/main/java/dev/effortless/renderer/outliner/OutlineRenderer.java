package dev.effortless.renderer.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.renderer.outliner.LineOutline.EndChasingLineOutline;
import dev.effortless.renderer.outliner.Outline.OutlineParams;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OutlineRenderer {

    private static final OutlineRenderer INSTANCE = new OutlineRenderer();
    private final Map<Object, OutlineEntry> outlines = Collections.synchronizedMap(new HashMap<>());
    private final Map<Object, OutlineEntry> outlinesView = Collections.unmodifiableMap(outlines);

    public static OutlineRenderer getInstance() {
        return INSTANCE;
    }

    public OutlineParams showLine(Object id, Vec3 start, Vec3 end) {
        if (!outlines.containsKey(id)) {
            var outline = new LineOutline();
            outlines.put(id, new OutlineEntry(outline));
        }
        var entry = outlines.get(id);
        entry.ticksTillRemoval = 1;
        ((LineOutline) entry.outline).set(start, end);
        return entry.outline.getParams();
    }

    public OutlineParams endChasingLine(Object id, Vec3 start, Vec3 end, float chasingProgress, boolean lockStart) {
        if (!outlines.containsKey(id)) {
            var outline = new EndChasingLineOutline(lockStart);
            outlines.put(id, new OutlineEntry(outline));
        }
        var entry = outlines.get(id);
        entry.ticksTillRemoval = 1;
        ((EndChasingLineOutline) entry.outline).setProgress(chasingProgress)
                .set(start, end);
        return entry.outline.getParams();
    }

    public OutlineParams showAABB(Object id, AABB aabb, int ttl) {
        createAABBOutlineIfMissing(id, aabb);
        var outline = getAndRefreshAABB(id, ttl);
        outline.prevBB = outline.targetBB = outline.bb = aabb;
        return outline.getParams();
    }

    public OutlineParams showAABB(Object id, AABB aabb) {
        createAABBOutlineIfMissing(id, aabb);
        var outline = getAndRefreshAABB(id);
        outline.prevBB = outline.targetBB = outline.bb = aabb;
        return outline.getParams();
    }

    public OutlineParams chaseAABB(Object id, AABB aabb) {
        createAABBOutlineIfMissing(id, aabb);
        var outline = getAndRefreshAABB(id);
        outline.targetBB = aabb;
        return outline.getParams();
    }

    public OutlineParams showCluster(Object id, Iterable<BlockPos> selection) {
        BlockClusterOutline outline = new BlockClusterOutline(selection);
        var entry = new OutlineEntry(outline);
        outlines.put(id, entry);
        return entry.getOutline().getParams();
    }

    public void keep(Object id) {
        if (outlines.containsKey(id))
            outlines.get(id).ticksTillRemoval = 1;
    }

    public void keep(Object id, int ticks) {
        if (outlines.containsKey(id))
            outlines.get(id).ticksTillRemoval = ticks;
    }

    public void remove(Object id) {
        outlines.remove(id);
    }

    public Optional<OutlineParams> edit(Object id) {
        keep(id);
        if (outlines.containsKey(id))
            return Optional.of(outlines.get(id)
                    .getOutline()
                    .getParams());
        return Optional.empty();
    }

    public Map<Object, OutlineEntry> getOutlines() {
        return outlinesView;
    }

    private void createAABBOutlineIfMissing(Object id, AABB bb) {
        if (!outlines.containsKey(id) || !(outlines.get(id).outline instanceof AABBOutline)) {
            var outline = new ChasingAABBOutline(bb);
            outlines.put(id, new OutlineEntry(outline));
        }
    }

    private ChasingAABBOutline getAndRefreshAABB(Object id) {
        var entry = outlines.get(id);
        entry.ticksTillRemoval = 1;
        return (ChasingAABBOutline) entry.getOutline();
    }

    private ChasingAABBOutline getAndRefreshAABB(Object id, int ttl) {
        var entry = outlines.get(id);
        entry.ticksTillRemoval = ttl;
        return (ChasingAABBOutline) entry.getOutline();
    }

    public void tick() {
        var iterator = outlines.values().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.tick();
            if (!entry.isAlive()) {
                iterator.remove();
            }
        }
    }

    public void renderOutlines(PoseStack poseStack, MultiBufferSource multiBufferSource, float pt) {
        outlines.forEach((key, entry) -> {
            var outline = entry.getOutline();
            var params = outline.getParams();
            params.alpha = 1;
            if (entry.isFading()) {
                var prevTicks = entry.ticksTillRemoval + 1;
                float fadeticks = OutlineEntry.FADE_TICKS;
                float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / fadeticks);
                float currentAlpha = 1 + (entry.ticksTillRemoval / fadeticks);
                float alpha = Mth.lerp(pt, lastAlpha, currentAlpha);

                params.alpha = alpha * alpha * alpha;
                if (params.alpha < 1 / 8f)
                    return;
            }
            outline.render(poseStack, multiBufferSource, pt);
        });
    }

    public static class OutlineEntry {

        private static final int FADE_TICKS = 10;

        private final Outline outline;
        private int ticksTillRemoval;

        public OutlineEntry(Outline outline) {
            this.outline = outline;
            ticksTillRemoval = 1;
        }

        public void tick() {
            ticksTillRemoval--;
            outline.tick();
        }

        public boolean isAlive() {
            return ticksTillRemoval >= -FADE_TICKS;
        }

        public boolean isFading() {
            return ticksTillRemoval < 0;
        }

        public Outline getOutline() {
            return outline;
        }

    }

}
