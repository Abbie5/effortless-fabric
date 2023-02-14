package dev.huskcasaca.effortless.screen.randomizer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.huskcasaca.effortless.randomizer.Randomizer;
import dev.huskcasaca.effortless.render.ScissorsHandler;
import dev.huskcasaca.effortless.screen.widget.NumberField;
import dev.huskcasaca.effortless.utils.RandomizerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
public class EffortlessRandomizerEditScreen extends Screen {

	private static final int MAX_RANDOMIZER_SIZE = Inventory.INVENTORY_SIZE;
	private static final int MAX_RANDOMIZER_NAME_LENGTH = 255;
	private static final int MIN_ITEM_COUNT = 0;
	private static final int MAX_ITEM_COUNT = 64;
	private static final int ROW_WIDTH = 268;

	protected final Screen parent;
	private final Consumer<Randomizer> applySettings;
	private Randomizer defaultSettings;
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

	private void updateSettings() {
		lastSettings = new Randomizer(
				nameEditBox.getValue(),
				entries.items()
		);
	}

	@Override
	public void tick() {
		nameEditBox.tick();
		entries.tick();
	}

	@Override
	protected void init() {

		this.entries = addRenderableWidget(new DetailsList());
		this.entries.reset(lastSettings.holders());

		this.nameEditBox = addRenderableWidget(
				new EditBox(font, width / 2 - (ROW_WIDTH - 2) / 2, 24, ROW_WIDTH - 2, 20, null)
		);
		this.nameEditBox.setMaxLength(MAX_RANDOMIZER_NAME_LENGTH);
		this.nameEditBox.setHint(Component.literal("Randomizer Name"));
		this.nameEditBox.setValue(lastSettings.name());

		addRenderableWidget(new CenteredStringWidget(width, 26, title, minecraft.font));

		this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("Delete Item"), (button) -> {
			entries.delete();
			updateSettings();
			updateButtonValidity();
		}).bounds(width / 2 - 154, height - 52, 150, 20).build());
		this.addButton = addRenderableWidget(Button.builder(Component.translatable("Add New Item"), (button) -> {
			minecraft.setScreen(new EffortlessItemPickerScreen(this,
					(itemStack) -> {
						entries.add(new Randomizer.Holder(itemStack.getItem(), 1));
						updateSettings();
						updateButtonValidity();
					}));
			updateButtonValidity();


			updateSettings();
			updateButtonValidity();
		}).bounds(width / 2 + 4, height - 52, 150, 20).build());


		this.saveButton = addRenderableWidget(Button.builder(Component.translatable("Save"), (button) -> {
			updateSettings();
			applySettings.accept(lastSettings);
			minecraft.setScreen(parent);
		}).bounds(width / 2 - 154, height - 28, 150, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			minecraft.setScreen(parent);
		}).bounds(width / 2 + 4, height - 28, 150, 20).build());
		updateButtonValidity();
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
	private class DetailsList extends ObjectSelectionList<DetailsList.Entry> {

		public DetailsList() {
			super(
					EffortlessRandomizerEditScreen.this.minecraft,
					EffortlessRandomizerEditScreen.this.width,
					EffortlessRandomizerEditScreen.this.height,
					50,
					EffortlessRandomizerEditScreen.this.height - 60,
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
			return EffortlessRandomizerEditScreen.this.getFocused() == this;
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
				renderComponentTooltip(poseStack, RandomizerUtils.getRandomizerEntryTooltip(entry.holder, totalCount()), i, j);
			}
		}

		public int add(Randomizer.Holder holder) {
			var index = getSelected() == null ? children().size() : (children().indexOf(getSelected()) + 1);
			var entry = new EffortlessRandomizerEditScreen.DetailsList.Entry(holder);
			children().add(index, entry);
			setSelected(entry);
			return index;
		}

		public void delete() {
			var selected = entries.getSelected();
			var index = children().indexOf(selected);
			removeEntry(selected);
			if (index >= 0 && index < children().size()) {
				setSelected(children().get(index));
			}
		}

		public void reset(List<Randomizer.Holder> itemStacks) {
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

		public List<Randomizer.Holder> items() {
			return children().stream().map((it) -> it.holder).toList();
		}

		public int totalCount() {
			return items().stream().mapToInt(Randomizer.Holder::count).sum();
		}

		public void tick() {
			children().forEach((entry) -> entry.numberField.tick());
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<Entry> {

			private final NumberField numberField;
			private Randomizer.Holder holder;

			public Entry(Randomizer.Holder holder) {
				this.holder = holder;
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
				this.numberField.getTextField().setValue(String.valueOf(this.holder.count()));
				this.numberField.getTextField().setResponder((string) -> {
					var count = 0;
					try {
						count = Integer.parseInt(string);
					} catch (NumberFormatException ignored) { }
					this.holder = this.holder.withCount(count);
				});

			}

			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.drawString(poseStack, minecraft.font, getDisplayName(holder), k + 24 , j + 6, 0xFFFFFFFF);
				var percentage = String.format("%.2f%%", 100.0 * holder.count() / totalCount());
				GuiComponent.drawString(poseStack, minecraft.font, percentage, k + ROW_WIDTH - 50 - minecraft.font.width(percentage), j + 6, 0xFFFFFFFF);


				numberField.setX(k + getRowWidth() - 46);
				numberField.setY(j + 1);
				numberField.render(poseStack, n, o, f);

//				if (isMouseOver(n, o)) {
//					setTooltipForNextRenderPass(itemStack.getHoverName());
//				}

				if (EffortlessRandomizerEditScreen.DetailsList.this.getSelected() != this) {
					numberField.getTextField().setFocus(false);
					numberField.setFocused(null);
				}

				blitSlot(poseStack, k, j, holder);
			}

			// TODO: 8/2/23
			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", getDisplayName(holder));
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

			private Component getDisplayName(Randomizer.Holder holder) {
				return holder.singleItemStack().getHoverName();
			}

			private void blitSlot(PoseStack poseStack, int i, int j, Randomizer.Holder holder) {
				blitSlotBg(poseStack, i + 1, j + 1);
				blitSlotItem(poseStack, i + 2, j + 2, holder.singleItemStack(), Integer.toString(holder.count()));
			}

			private void blitSlotItem(PoseStack poseStack, int i, int j, ItemStack itemStack, String string2) {
				itemRenderer.renderGuiItem(itemStack, i, j);
				poseStack.pushPose();
				poseStack.translate(0.0F, 0.0F, itemRenderer.blitOffset + 200.0F);
				var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				font.drawInBatch(string2, (float)(i + 19 - 2 - font.width(string2)), (float)(j + 6 + 3), 16777215, true, poseStack.last().pose(), bufferSource, false, 0, 15728880);
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
