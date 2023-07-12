package dev.effortless.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.effortless.Effortless;
import dev.effortless.building.operation.ItemStackType;
import dev.effortless.config.ConfigManager;
import dev.effortless.screen.mode.EffortlessModeRadialScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ContainerOverlay {

    private static final ContainerOverlay INSTANCE = new ContainerOverlay(Minecraft.getInstance());
    private static int lastTipsHeight = 0;
    private final Map<Integer, Map<Object, Entry>> prioritiedMap = new HashMap<>();
    private final Minecraft minecraft;

    public ContainerOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public static ContainerOverlay getInstance() {
        return INSTANCE;
    }

    public static int rightTipsHeight() {
        if (getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            return lastTipsHeight;
        } else {
            return 0;
        }
    }

    private static Direction.AxisDirection getAxisDirection() {
        return ConfigManager.getGlobalPreviewConfig().getBuildInfoPosition().getAxis();
    }

    public void showMessages(Object id, List<Component> components, int priority) {
        showEntry(id, new ComponentsEntry(components), priority);
    }

    public void showTitledItems(Object id, Component title, Map<ItemStackType, List<ItemStack>> items, int priority) {
        showEntry(id, new TitledItemsEntry(title, items), priority);
    }

    public void showItems(Object id, Map<ItemStackType, List<ItemStack>> items, int priority) {
        showEntry(id, new ItemsEntry(items), priority);
    }

    private void showEntry(Object id, Entry entry, int priority) {
        prioritiedMap.compute(priority, (k, v) -> {
            if (v == null) {
                v = new LinkedHashMap<>();
            }
            v.put(id, entry);
            return v;
        });
    }

    public void renderGuiOverlay(PoseStack poseStack) {
        lastTipsHeight = 0;
        var contentSide = ConfigManager.getGlobalPreviewConfig().getBuildInfoPosition().getAxis();
        if (contentSide == null) {
            return;
        }
        if (contentSide == Direction.AxisDirection.POSITIVE
                && (minecraft.options.showAutosaveIndicator().get()
                && (minecraft.gui.autosaveIndicatorValue > 0.0F
                || minecraft.gui.lastAutosaveIndicatorValue > 0.0F))
                && Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), minecraft.gui.lastAutosaveIndicatorValue, minecraft.gui.autosaveIndicatorValue), 0.0F, 1.0F)) > 8 || EffortlessModeRadialScreen.getInstance().isVisible()) {
            return;
        }
        poseStack.pushPose();
        var startX = contentSide == Direction.AxisDirection.POSITIVE ?  minecraft.getWindow().getGuiScaledWidth() : 0f;
        var startY = minecraft.getWindow().getGuiScaledHeight() * 1f;
        poseStack.translate(startX, startY, 0);
        poseStack.translate(-1f * contentSide.getStep(), 0, 0);
        poseStack.translate(0, -8, 0);
        startY -= 8;

        for (var map : prioritiedMap.values()) {
            var iterator = new LinkedList<>(map.values()).descendingIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (!entry.shouldRender()) {
                    continue;
                }
                poseStack.pushPose();
                entry.fill(poseStack, entry.getTotalWidth(), 0, 0, -entry.getTotalHeight(), minecraft.options.getBackgroundColor(0.8f * entry.getAlpha()));
                poseStack.translate(entry.getPaddingX(), -entry.getPaddingY(), 0);
                entry.render(poseStack, (int) startX + entry.getPaddingX(), (int) startY - entry.getPaddingY(), contentSide);
                poseStack.popPose();
                poseStack.translate(0, -entry.getTotalHeight() - 1, 0);
                startY -= entry.getTotalHeight() + 1;
            }
        }

        poseStack.popPose();
    }

    public void tick() {
        var iterator = prioritiedMap.values().iterator();
        while (iterator.hasNext()) {
            var map = iterator.next();
            var iterator1 = map.values().iterator();
            while (iterator1.hasNext()) {
                var entry = iterator1.next();
                entry.tick();
                if (!entry.isAlive()) {
                    iterator1.remove();
                }
            }
            if (map.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public enum Position {

        DISABLED("disabled"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        Position(String name) {
            this.name = name;
        }

        public String getNameKey() {
            // TODO: 15/9/22 use ResourceLocation
            return Effortless.MOD_ID + ".position." + name;
        }

        public Direction.AxisDirection getAxis() {
            return switch (this) {
                case LEFT -> Direction.AxisDirection.NEGATIVE;
                case RIGHT -> Direction.AxisDirection.POSITIVE;
                default -> null;
            };
        }
    }

    private static abstract class Entry extends GuiComponent {

        private static final int FADE_TICKS = 10;

        private int ticksTillRemoval = 100;

        public abstract void render(PoseStack poseStack, int i, int j, Direction.AxisDirection contentSide);

        public abstract int getWidth();

        public abstract int getHeight();

        public abstract int getPaddingX();

        public abstract int getPaddingY();

        public int getTotalWidth() {
            return getWidth() + getPaddingX() * 2;
        }

        public int getTotalHeight() {
            return getHeight() + getPaddingY() * 2;
        }

        public abstract boolean shouldRender();

        public void tick() {
            ticksTillRemoval--;
        }

        public boolean isAlive() {
            return ticksTillRemoval >= -FADE_TICKS;
        }

        public boolean isFading() {
            return ticksTillRemoval < 0;
        }

        public float getAlpha() {
            return isFading() ? (float) (ticksTillRemoval + FADE_TICKS) / FADE_TICKS : 1;
        }

    }

    private static class ComponentsEntry extends Entry {

        private final List<Component> components;

        public ComponentsEntry(List<Component> components) {
            var reverse = new ArrayList<>(components);
            Collections.reverse(reverse);
            this.components = reverse;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, Direction.AxisDirection contentSide) {
            var minecraft = Minecraft.getInstance();
            poseStack.translate(0, 1, 0);
            var font = minecraft.font;
            var textY = 0;
            for (var text : components) {
                textY -= 10;
                var positionX = contentSide == Direction.AxisDirection.POSITIVE ? getWidth() - font.width(text) : 0;
                font.drawShadow(poseStack, text, positionX, textY, 0xffffffff);
            }
        }

        @Override
        public int getWidth() {
            var minecraft = Minecraft.getInstance();
            var font = minecraft.font;
            return components.stream().mapToInt(font::width).max().orElse(0);
        }

        @Override
        public int getHeight() {
            return components.size() * 10;
        }

        @Override
        public int getPaddingX() {
            return 10;
        }

        @Override
        public int getPaddingY() {
            return 2;
        }

        @Override
        public boolean shouldRender() {
            return !components.isEmpty();
        }
    }

    private static class TitledItemsEntry extends ItemsEntry {
        private final Component header;

        public TitledItemsEntry(Component header, Map<ItemStackType, List<ItemStack>> result) {
            super(result);
            this.header = header;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, Direction.AxisDirection contentSide) {
            super.render(poseStack, i, j, contentSide);
            var minecraft = Minecraft.getInstance();
            var font = minecraft.font;
            var positionX = contentSide == Direction.AxisDirection.POSITIVE ? getWidth() - font.width(header) : 0;
            font.drawShadow(poseStack, header, positionX, -getHeight() + 2, 0xffffffff);
        }

        @Override
        public int getHeight() {
            return super.getHeight() + 10;
        }

        @Override
        public int getWidth() {
            return Math.max(super.getWidth(), Minecraft.getInstance().font.width(header));
        }
    }

    private static class ItemsEntry extends Entry {

        private static final int MAX_COLUMN = 9;
        private static final int ITEM_SPACING_X = 18;
        private static final int ITEM_SPACING_Y = 18;
        private final Map<ItemStackType, List<ItemStack>> result;
        private final List<ItemStack> items;

        public ItemsEntry(Map<ItemStackType, List<ItemStack>> result) {
            this.result = result;
            this.items = result.values().stream().flatMap(Collection::stream).toList();
        }

        private void renderGuiItem(int i, int j, ItemStack itemStack, BakedModel bakedModel, boolean red) {
            Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            PoseStack poseStack = RenderSystem.getModelViewStack();

            poseStack.pushPose();
            poseStack.translate((float) i, (float)j, 100.0F);
            poseStack.translate(8.0F, 8.0F, 0.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            poseStack.scale(16.0F, 16.0F, 16.0F);

            RenderSystem.applyModelViewMatrix();
            var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            var light = !bakedModel.usesBlockLight();
            if (light) {
                Lighting.setupForFlatItems();
            }
            Minecraft.getInstance().getItemRenderer().render(itemStack, ItemTransforms.TransformType.GUI, false, new PoseStack(), bufferSource, 15728880, red ? OverlayTexture.RED_OVERLAY_V : OverlayTexture.NO_OVERLAY, bakedModel);
            bufferSource.endBatch();
            RenderSystem.enableDepthTest();
            if (light) {
                Lighting.setupFor3DItems();
            }
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        private void renderGuiItemDecorations(int i, int j, Font font, String string, int color) {
            var poseStack = new PoseStack();
            poseStack.translate(0.0F, 0.0F, 200.0F);
            var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            font.drawInBatch(string, (float)(i + 19 - 2 - font.width(string)), (float)(j + 6 + 3), color, true, poseStack.last().pose(), bufferSource, false, 0, 15728880);
            bufferSource.endBatch();
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, Direction.AxisDirection contentSide) {
            var minecraft = Minecraft.getInstance();

            var itemCol = 0;
            var itemRow = 0;

            for (var entry : result.entrySet()) {
                for (var itemStack : entry.getValue()) {
                    var i1 = i - itemCol * ITEM_SPACING_X * contentSide.getStep();
                    var j1 = j + itemRow * ITEM_SPACING_Y - ITEM_SPACING_Y * Mth.ceil(1f * items.size() / MAX_COLUMN);

                    renderGuiItem(i1, j1, itemStack, minecraft.getItemRenderer().getModel(itemStack, null, null, 0), entry.getKey().getColor() == ChatFormatting.RED.getColor());

                    renderGuiItemDecorations(i1, j1, minecraft.font, Integer.toString(itemStack.getCount()), entry.getKey().getColor());

                    if (itemCol < MAX_COLUMN - 1) {
                        itemCol += 1;
                    } else {
                        itemCol = 0;
                        itemRow += 1;
                    }
                }
            }
        }

        @Override
        public int getWidth() {
            return Math.min(MAX_COLUMN, items.size()) * ITEM_SPACING_X - 4;
        }

        @Override
        public int getHeight() {
            return Mth.ceil(1f * items.size() / MAX_COLUMN) * ITEM_SPACING_Y + 2;
        }

        @Override
        public int getPaddingX() {
            return 10;
        }

        @Override
        public int getPaddingY() {
            return 2;
        }

        @Override
        public boolean shouldRender() {
            return !items.isEmpty();
        }
    }

}
