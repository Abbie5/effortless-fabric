package dev.effortless.screen.pattern.randomizer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.effortless.render.ScissorsHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EffortlessItemPickerScreen extends Screen {

    private static final int MAX_SEARCH_NAME_LENGTH = 255;
    private static final int ROW_WIDTH = 268;

    protected final Screen parent;
    private final Consumer<ItemStack> applySettings;
    private EditBox searchEditBox;
    private Button addButton;
    private DetailsList entries;

    public EffortlessItemPickerScreen(Screen screen, Consumer<ItemStack> consumer) {
        super(Component.translatable("randomizer.search.title"));
        this.parent = screen;
        this.applySettings = consumer;
    }

    @Override
    public void tick() {
        searchEditBox.tick();
    }

    @Override
    protected void init() {

        if (this.minecraft != null) {
            var player = this.minecraft.player;
            var itemStacks = new ArrayList<ItemStack>();
            if (player != null) {
                CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), player.canUseGameMasterBlocks());
                itemStacks.add(new ItemStack(Items.AIR));
                itemStacks.addAll(CreativeModeTabs.searchTab().getSearchTabDisplayItems());
            } else {
                itemStacks.addAll(BuiltInRegistries.ITEM.stream().map(ItemStack::new).toList());
            }
            this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_NAMES, itemStacks);
            this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_TAGS, itemStacks);
        }

        this.searchEditBox = addRenderableWidget(
                new EditBox(font, width / 2 - (ROW_WIDTH - 2) / 2, 24, ROW_WIDTH - 2, 20, Component.translatable("randomizer.search.text"))
        );
        this.searchEditBox.setMaxLength(MAX_SEARCH_NAME_LENGTH);
        this.searchEditBox.setHint(Component.literal("Search Item"));
        this.searchEditBox.setResponder((text) -> {
            updateSearchResult(text);
            updateButtonValidity();
        });

        this.entries = addWidget(new DetailsList());

        this.addButton = addRenderableWidget(Button.builder(Component.translatable("Add"), (button) -> {
            applySettings.accept(entries.getFocused().itemStack);
            minecraft.setScreen(parent);
        }).bounds(width / 2 - 154, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            minecraft.setScreen(parent);
        }).bounds(width / 2 + 4, height - 28, 150, 20).build());

        this.searchEditBox.setValue("");
    }

    void updateSearchResult(String string) {
        var items = NonNullList.<ItemStack>create();
        var searchTree = (SearchTree<ItemStack>) null;
        if (string.startsWith("#")) {
            string = string.substring(1);
            searchTree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
        } else {
            searchTree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
        }
        items.addAll(searchTree.search(string.toLowerCase(Locale.ROOT)));
        entries.reset(items);
        entries.setScrollAmount(0);
    }

    void updateButtonValidity() {
        addButton.active = hasValidSelection();
    }

    private boolean hasValidSelection() {
        return entries.getSelected() != null;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        entries.render(poseStack, i, j, f);
        searchEditBox.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, title, width / 2, 8, 16777215);
        super.render(poseStack, i, j, f);
    }

    @Environment(EnvType.CLIENT)
    private class DetailsList extends ObjectSelectionList<DetailsList.Entry> {

        public DetailsList() {
            super(
                    EffortlessItemPickerScreen.this.minecraft,
                    EffortlessItemPickerScreen.this.width,
                    EffortlessItemPickerScreen.this.height,
                    50,
                    EffortlessItemPickerScreen.this.height - 36,
                    24
            );
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            updateButtonValidity();
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        @Override
        protected boolean isFocused() {
            return EffortlessItemPickerScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 160;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            if (minecraft.level != null) {
                setRenderBackground(false);
                setRenderTopAndBottom(false);
                ScissorsHandler.scissor(new Rectangle(0, y0, this.width, y1 - y0));
            } else {
                setRenderBackground(true);
                setRenderTopAndBottom(true);
            }
            super.render(poseStack, i, j, f);
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            if (this.minecraft.level != null) {
                this.fillGradient(poseStack, 0, 0, this.width, this.height, 0xa1101010, 0x8c101010);
            }
        }

        @Override
        protected void renderDecorations(PoseStack poseStack, int i, int j) {
            if (this.minecraft.level != null) {
                ScissorsHandler.removeLastScissor();
            }
            var entry = this.getHovered();
            if (entry != null && i < (this.width + this.getRowWidth()) / 2 - 48) {
                renderTooltip(poseStack, entry.itemStack.getHoverName(), i, j);
            }
        }

        public int add(ItemStack itemStack) {
            var index = getSelected() == null ? children().size() : children().indexOf(getSelected());
            var entry = new EffortlessItemPickerScreen.DetailsList.Entry(itemStack);
            children().add(index, entry);
            setSelected(entry);
            return index;
        }


        public void reset(List<ItemStack> itemStacks) {
            int i = children().indexOf(getSelected());
            clearEntries();

            itemStacks.forEach((itemStack) -> {
                addEntry(new Entry(itemStack));
            });

            var list = children();
            if (i >= 0 && i < list.size()) {
                setSelected(list.get(i));
            }
        }

        @Environment(EnvType.CLIENT)
        class Entry extends ObjectSelectionList.Entry<Entry> {

            private final ItemStack itemStack;

            public Entry(ItemStack itemStack) {
                this.itemStack = itemStack;

            }

            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, minecraft.font, getDisplayName(itemStack), k + 24, j + 6, 0xFFFFFFFF);

                blitSlot(poseStack, k, j, itemStack);
            }

            // TODO: 8/2/23
            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", getDisplayName(itemStack));
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    EffortlessItemPickerScreen.DetailsList.this.setSelected(this);
                    return true;
                }
                return false;
            }

            private Component getDisplayName(ItemStack itemStack) {
                return itemStack.getHoverName();
            }

            private void blitSlot(PoseStack poseStack, int i, int j, ItemStack itemStack) {
                this.blitSlotBg(poseStack, i + 1, j + 1);
                if (!itemStack.isEmpty()) {
                    itemRenderer.renderGuiItem(itemStack, i + 2, j + 2);
                }
            }

            private void blitSlotBg(PoseStack poseStack, int i, int j) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(poseStack, i, j, getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }

        }
    }
}
