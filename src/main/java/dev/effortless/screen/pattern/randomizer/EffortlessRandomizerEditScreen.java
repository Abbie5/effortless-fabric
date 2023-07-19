package dev.effortless.screen.pattern.randomizer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.effortless.building.pattern.randomizer.ItemChance;
import dev.effortless.building.pattern.randomizer.Randomizer;
import dev.effortless.screen.ScissorsHandler;
import dev.effortless.screen.config.EditorList;
import dev.effortless.screen.widget.NumberField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EffortlessRandomizerEditScreen extends Screen {

    private static final int MAX_RANDOMIZER_SIZE = Inventory.INVENTORY_SIZE;
    private static final int MAX_RANDOMIZER_NAME_LENGTH = 255;
    private static final int MIN_ITEM_COUNT = 0;
    private static final int MAX_ITEM_COUNT = 64;
    private static final int ROW_WIDTH = 268;

    protected final Screen parent;
    private final Consumer<Randomizer> applySettings;
    private final Randomizer defaultSettings;
    private Randomizer lastSettings;
    private Button deleteButton;
    private Button addButton;
    private Button saveButton;
    private EditBox nameEditBox;
    private DetailsList entries;

    public EffortlessRandomizerEditScreen(Screen screen, Consumer<Randomizer> consumer, Randomizer randomizer) {
        super(Component.translatable("randomizer.edit.title"));
        this.parent = screen;
        this.applySettings = consumer;
        this.defaultSettings = randomizer;
        this.lastSettings = randomizer;
    }

    public static List<Component> getRandomizerEntryTooltip(ItemChance chance, int totalCount) {
        var components = new ItemStack(chance.content(), 1).getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED.asCreative());
        var percentage = String.format("%.2f%%", 100.0 * chance.chance() / totalCount);
        components.add(
                Component.empty()
        );
        components.add(
                Component.literal(ChatFormatting.GRAY + "Total Probability: " + ChatFormatting.GOLD + percentage + ChatFormatting.DARK_GRAY + " (" + chance.chance() + "/" + totalCount + ")" + ChatFormatting.RESET)
        );
        return components;
    }

    private void updateSettings() {
        lastSettings = Randomizer.create(
                nameEditBox.getValue(),
                entries.items()
        );
    }

    @Override
    public void tick() {
        nameEditBox.tick();
        entries.tick();
        updateButtonValidity();
    }

    @Override
    protected void init() {
        this.entries = addRenderableWidget(new DetailsList(minecraft, width, height, 50, height - 60, 24));
        this.entries.reset(lastSettings.chances());

        this.nameEditBox = addRenderableWidget(
                new EditBox(font, width / 2 - (ROW_WIDTH - 2) / 2, 24, ROW_WIDTH - 2, 20, null)
        );
        this.nameEditBox.setMaxLength(MAX_RANDOMIZER_NAME_LENGTH);
        this.nameEditBox.setHint(Component.literal("Randomizer Name"));
        this.nameEditBox.setValue(lastSettings.name());

        addRenderableWidget(new CenteredStringWidget(width, 26, title, minecraft.font));

        this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("Delete Item"), (button) -> {
            entries.deleteSelected();
            updateSettings();
        }).bounds(width / 2 - 154, height - 52, 150, 20).build());
        this.addButton = addRenderableWidget(Button.builder(Component.translatable("Add New Item"), (button) -> {
            minecraft.setScreen(new EffortlessItemPickerScreen(this,
                    (itemStack) -> {
                        entries.insertSelected(ItemChance.of(itemStack.getItem(), 1));
                        updateSettings();
                    }));
            updateSettings();
        }).bounds(width / 2 + 4, height - 52, 150, 20).build());

        this.saveButton = addRenderableWidget(Button.builder(Component.translatable("Save"), (button) -> {
            updateSettings();
            applySettings.accept(lastSettings);
            minecraft.setScreen(parent);
        }).bounds(width / 2 - 154, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            minecraft.setScreen(parent);
        }).bounds(width / 2 + 4, height - 28, 150, 20).build());
    }

    void updateButtonValidity() {
        deleteButton.active = hasValidSelection();
        addButton.active = entries.children().size() <= MAX_RANDOMIZER_SIZE;
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
        super.render(poseStack, i, j, f);
    }

    @Environment(EnvType.CLIENT)
    private class DetailsList extends EditorList<ItemChance> {

        public DetailsList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        @Override
        protected boolean isFocused() {
            return EffortlessRandomizerEditScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 160;
        }

        @Override
        protected void renderDecorations(PoseStack poseStack, int i, int j) {
            if (this.minecraft.level != null) {
                ScissorsHandler.removeLastScissor();
            }
            var entry = this.getHovered();
            if (entry != null && i < (this.width + this.getRowWidth()) / 2 - 48) {
                renderComponentTooltip(poseStack, getRandomizerEntryTooltip(entry.getItem(), totalCount()), i, j);
            }
        }

        @Override
        protected EditorList<ItemChance>.Entry createHolder(ItemChance item) {
            return new Entry(item);
        }

        public int totalCount() {
            return items().stream().mapToInt(ItemChance::chance).sum();
        }

        public void tick() {
            children().forEach((entry) -> ((Entry) entry).tick());
        }

        @Environment(EnvType.CLIENT)
        class Entry extends EditorList<ItemChance>.Entry {

            private final NumberField numberField;
            private ItemChance chance;

            public Entry(ItemChance chance) {
                super(chance);
                this.chance = chance;
                this.numberField = new NumberField(0, 0, 42, 18);
                this.numberField.getTextField().setFilter((string) -> {
                    if (string.isEmpty()) {
                        return true;
                    }
                    try {
                        var result = Integer.parseInt(string);
                        if (result < MIN_ITEM_COUNT || result > MAX_ITEM_COUNT) {
                            numberField.getTextField().setValue(String.valueOf(Mth.clamp(result, MIN_ITEM_COUNT, MAX_ITEM_COUNT)));
                            return false;
                        }
                        if (!String.valueOf(result).equals(string)) {
                            numberField.getTextField().setValue(String.valueOf(result));
                            return false;
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
                this.numberField.getTextField().setValue(String.valueOf(this.chance.chance()));
                this.numberField.getTextField().setResponder((string) -> {
                    var count = 0;
                    try {
                        count = Integer.parseInt(string);
                    } catch (NumberFormatException ignored) {
                    }
                    this.chance = updateChance(chance, count);
                });
            }

            private static ItemChance updateChance(ItemChance chance, int newChance) {
                return ItemChance.of(chance.content(), newChance);
            }

            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, minecraft.font, getDisplayName(chance), k + 24, j + 6, 0xFFFFFFFF);
                var percentage = String.format("%.2f%%", 100.0 * chance.chance() / totalCount());
                GuiComponent.drawString(poseStack, minecraft.font, percentage, k + ROW_WIDTH - 50 - minecraft.font.width(percentage), j + 6, 0xFFFFFFFF);

                numberField.setX(k + getRowWidth() - 46);
                numberField.setY(j + 1);
                numberField.render(poseStack, n, o, f);

                if (DetailsList.this.getSelected() != this) {
                    numberField.getTextField().setFocus(false);
                    numberField.setFocused(null);
                }

                blitSlot(poseStack, k, j, chance);
            }

            // TODO: 8/2/23
            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", getDisplayName(chance));
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                var r = numberField.mouseClicked(d, e, i);
                if (!numberField.getTextField().isFocused()) {
                    if (numberField.getTextField().getValue().isEmpty()) {
                        numberField.getTextField().setValue("0");
                    }
                }
                if (i == 0) {
                    EffortlessRandomizerEditScreen.DetailsList.this.setSelected(this);
                    return true;
                }
                return r;
            }

            @Override
            public boolean keyPressed(int i, int j, int k) {
                return numberField.keyPressed(i, j, k) || super.keyPressed(i, j, k);
            }

            @Override
            public boolean keyReleased(int i, int j, int k) {
                return numberField.keyReleased(i, j, k) || super.keyReleased(i, j, k);
            }

            @Override
            public boolean charTyped(char c, int i) {
                return numberField.charTyped(c, i) || super.charTyped(c, i);
            }

            public void tick() {
                numberField.tick();
            }

            private Component getDisplayName(ItemChance chance) {
                return new ItemStack(chance.content(), 1).getHoverName();
            }

            private void blitSlot(PoseStack poseStack, int i, int j, ItemChance chance) {
                blitSlotBg(poseStack, i + 1, j + 1);
                blitSlotItem(poseStack, i + 2, j + 2, new ItemStack(chance.content(), 1), Integer.toString(chance.chance()));
            }

            private void blitSlotItem(PoseStack poseStack, int i, int j, ItemStack itemStack, String string2) {
                itemRenderer.renderGuiItem(itemStack, i, j);
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.0F, itemRenderer.blitOffset + 200.0F);
                var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                font.drawInBatch(string2, (float) (i + 19 - 2 - font.width(string2)), (float) (j + 6 + 3), 16777215, true, poseStack.last().pose(), bufferSource, false, 0, 15728880);
                bufferSource.endBatch();
                poseStack.popPose();
            }

            private void blitSlotBg(PoseStack poseStack, int i, int j) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(poseStack, i, j, getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }

        }
    }
}
