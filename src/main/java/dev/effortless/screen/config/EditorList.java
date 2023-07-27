package dev.effortless.screen.config;

import dev.effortless.screen.ScissorsHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class EditorList<T> extends ObjectSelectionList<EditorList<T>.Entry> {

    private static final int ICON_WIDTH = 32;
    private static final int ICON_HEIGHT = 32;

    private static final int ROW_WIDTH = 282;

    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");

    public EditorList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
    }

    @Override
    public void setSelected(EditorList<T>.Entry entry) {
        super.setSelected(entry);
        // TODO: 22/2/23
//        updateButtonValidity();
    }

    @Override
    public int getRowWidth() {
        return ROW_WIDTH;
    }

//    @Override
//    protected boolean isFocused() {
//        return EffortlessRandomizerSettingsScreen.this.getFocused() == this;
//    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 160;
    }

    @Override
    public void render(GuiGraphics gui, int i, int j, float f) {
        if (minecraft.level != null) {
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            ScissorsHandler.scissor(new Rectangle(0, y0, this.width, y1 - y0));
        } else {
            setRenderBackground(true);
            setRenderTopAndBottom(true);
        }
        super.render(gui, i, j, f);
    }

    @Override
    protected void renderBackground(GuiGraphics gui) {
        if (this.minecraft.level != null) {
            gui.fillGradient(0, 0, this.width, this.height, 0xa1101010, 0x8c101010);
        }
    }

    @Override
    protected void renderDecorations(GuiGraphics gui, int i, int j) {
        if (this.minecraft.level != null) {
            ScissorsHandler.removeLastScissor();
        }

    }

    protected Entry createHolder(T item) {
        return new Entry(item);
    }

    protected void renderEntry(GuiGraphics gui, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f, Entry entry) {

    }

    protected Component getNarration(T item) {
        return null;
    }

    // old: add
    public int insertSelected(T item) {
        var index = getSelected() == null ? children().size() : (children().indexOf(getSelected()) + 1);
        var entry = createHolder(item);
        children().add(index, entry);
        setSelected(entry);
        return index;
    }

    // old: edit
    public void replaceSelect(T item) {
        getSelected().setItem(item);
    }

    public void deleteSelected() {
        var selected = getSelected();
        var index = children().indexOf(selected);
        removeEntry(selected);
        if (index >= 0 && index < children().size()) {
            setSelected(children().get(index));
        }
    }

    private void swap(int i, int j) {
        var old = children().get(i);
        children().set(i, children().get(j));
        children().set(j, old);
    }

    public void reset(Collection<T> items) {
        int i = children().indexOf(getSelected());
        clearEntries();

        items.forEach((item) -> {
            addEntry(createHolder(item));
        });

        var list = children();
        if (i >= 0 && i < list.size()) {
            setSelected(list.get(i));
        }
    }

    public List<T> items() {
        return children().stream().map((entry -> entry.item)).toList();
    }

    @Environment(EnvType.CLIENT)
    public class Entry extends ObjectSelectionList.Entry<Entry> {

        private T item;

        public Entry(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        @Override
        public void render(GuiGraphics gui, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            renderEntry(gui, i, j, k, l, m, n, o, bl, f, this);
            if (o > j && o < j + ICON_WIDTH) {
                int v = n - k;
                int w = o - j;
                if (i > 0) {
                    gui.blit(ICON_OVERLAY_LOCATION, k + 8, j, 96.0F, 8 < v && v < 24 && w < 16 ? 32.0F : 0.0F, 32, 32, 256, 256);
                }
                if (i < children().size() - 1) {
                    gui.blit(ICON_OVERLAY_LOCATION, k + 8, j, 64.0F, 8 < v && v < 24 && w > 16 ? 32.0F : 0.0F, 32, 32, 256, 256);
                }
            }

        }

        @Override
        public Component getNarration() {
            return EditorList.this.getNarration(item);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            var f = d - getRowLeft();
            var g = e - getRowTop(children().indexOf(this));
            if (f <= 32.0) {

                int j = children().indexOf(this);
                if (8 < f && f < 24 && g < 16 && j > 0) { // move down
                    swap(j, j - 1);
                    EditorList.this.scrollAmount = getScrollAmount() - itemHeight;
                    return true;
                }

                if (8 < f && f < 24 && g > 16 && j < children().size() - 1) { // move up
                    swap(j, j + 1);
                    EditorList.this.scrollAmount = getScrollAmount() + itemHeight;
                    return true;
                }
            }
            EditorList.this.setSelected(this);
            return false;
        }

    }

}