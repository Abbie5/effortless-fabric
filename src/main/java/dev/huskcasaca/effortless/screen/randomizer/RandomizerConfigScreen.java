package dev.huskcasaca.effortless.screen.randomizer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.huskcasaca.effortless.randomizer.Randomizer;
import dev.huskcasaca.effortless.randomizer.RandomizerSettings;
import dev.huskcasaca.effortless.render.ScissorsHandler;
import dev.huskcasaca.effortless.utils.RandomizerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class RandomizerConfigScreen extends Screen {
	private static final int SLOT_TEX_SIZE = 128;
	private static final int SLOT_BG_SIZE = 18;
	private static final int SLOT_STAT_HEIGHT = 20;
	private static final int SLOT_BG_X = 1;
	private static final int SLOT_BG_Y = 1;
	private static final int SLOT_FG_X = 2;
	private static final int SLOT_FG_Y = 2;

	private static final int SLOT_OFFSET_X = 20;
	private static final int SLOT_OFFSET_Y = 20;

	private static final int ICON_WIDTH = 32;
	private static final int ICON_HEIGHT = 32;

	private static final int ROW_WIDTH = 282;

	private static final int MAX_SLOT_COUNT = (ROW_WIDTH - ICON_WIDTH) / SLOT_OFFSET_X;

	private static final Color RADIAL_COLOR = new Color(0.44f, 0.44f, 0.44f, 1f);
	private static final Color HIGHLIGHT_COLOR = new Color(0.84f, 0.84f, 0.84f, 1f);
	private static final int RADIAL_SIZE = 12;
	private static final double RING_INNER_EDGE = 6;
	private static final double RING_OUTER_EDGE = 15;

	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");

	protected final Screen parent;
	private final Consumer<RandomizerSettings> applySettings;
	private RandomizerSettings lastSettings;
	private DetailsList entries;
	private Button editRandomizerButton;
	private Button deleteRandomizerButton;

	public RandomizerConfigScreen(Screen screen, Consumer<RandomizerSettings> consumer, RandomizerSettings randomizerSettings) {
		super(Component.translatable("randomizer.config.title"));
		this.parent = screen;
		this.applySettings = consumer;
		this.lastSettings = randomizerSettings;
	}

	private void updateSettings() {
		lastSettings = new RandomizerSettings(
				entries.items()
		);
	}

	@Override
	protected void init() {
		this.entries = addRenderableWidget(new DetailsList());
		this.entries.reset(lastSettings.randomizers());

		addRenderableWidget(new CenteredStringWidget(width, 49, title, minecraft.font));

		this.editRandomizerButton = addRenderableWidget(Button.builder(Component.translatable("Edit"), (button) -> {
			if (hasValidSelection()) {
				minecraft.setScreen(new RandomizerEditScreen(this,
						(randomizer) -> {
							entries.getSelected().setRandomizer(randomizer);
							updateSettings();
							updateButtonValidity();
						},
						entries.getSelected().randomizer));
			}
		}).bounds(width / 2 - 154, height - 52, 72, 20).build());

		this.deleteRandomizerButton = addRenderableWidget(Button.builder(Component.translatable("Delete"), (button) -> {
			if (hasValidSelection()) {
				entries.delete();
				updateSettings();
				updateButtonValidity();
			}
		}).bounds(width / 2 - 76, height - 52, 72, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("Create New Randomizer"), (button) -> {
			minecraft.setScreen(new RandomizerEditScreen(this,
					(randomizer) -> {
						entries.add(randomizer);
						updateSettings();
						updateButtonValidity();
					},
					Randomizer.EMPTY));
			updateButtonValidity();
		}).bounds(width / 2 + 4, height - 52, 150, 20).build());


		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
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
		editRandomizerButton.active = hasValidSelection();
		deleteRandomizerButton.active = hasValidSelection();
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
					RandomizerConfigScreen.this.minecraft,
					RandomizerConfigScreen.this.width,
					RandomizerConfigScreen.this.height,
					32,
					RandomizerConfigScreen.this.height - 60,
					24 + 12
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
			return RandomizerConfigScreen.this.getFocused() == this;
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

			if (entry == null || !entry.isMouseOver(i, j)) {
				return;
			}

			var x0 = (this.width - ROW_WIDTH) / 2 + 2;
			var x1 = (this.width - ROW_WIDTH) / 2 + ICON_WIDTH + 2;

			if (i >= x0 && i <= x1) {
				var index = children().indexOf(entry);
				if (index + 1 < RADIAL_SIZE) {
					renderTooltip(poseStack, Component.literal("Slot " + (index + 1)), i, j);
				} else {
					renderTooltip(poseStack, Component.literal("Not in Slot"), i, j);
				}
				return;
			}

			if (entry.isMouseOver(i, j - 13) && entry.isMouseOver(i, j + 3)) {
				var index = new AtomicInteger(0);
				var holders = entry.randomizer.holders();
				var totalCount = holders.stream().mapToInt((holder) -> holder.count()).sum();
				for (var holder : holders) {
					var last = index.getAndIncrement();
					if (last > MAX_SLOT_COUNT) break;

					x0 = (this.width - ROW_WIDTH) / 2 + 3 + last * SLOT_OFFSET_X + ICON_WIDTH + 1;
					x1 = (this.width - ROW_WIDTH) / 2 + 3 + (last + 1) * SLOT_OFFSET_X + (last == MAX_SLOT_COUNT ? ICON_WIDTH / 2 : ICON_WIDTH);
					if (i < x0 || i > x1) continue;

					if (last == MAX_SLOT_COUNT) {
						renderComponentTooltip(poseStack, Collections.singletonList(Component.literal("+" + (holders.size() - MAX_SLOT_COUNT) + " More")), i, j);
					} else {
						renderComponentTooltip(poseStack, RandomizerUtils.getRandomizerEntryTooltip(holder, totalCount), i, j);
					}
				}
			}
		}

		public int add(Randomizer randomizer) {
			var index = getSelected() == null ? children().size() : (children().indexOf(getSelected()) + 1);
			var entry = new RandomizerConfigScreen.DetailsList.Entry(randomizer);
			children().add(index, entry);
			setSelected(entry);
			return index;
		}

		public void edit(Randomizer randomizer) {
			getSelected().setRandomizer(randomizer);
		}

		public void delete() {
			var selected = entries.getSelected();
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

		public void reset(List<Randomizer> randomizers) {
			int i = children().indexOf(getSelected());
			clearEntries();

			randomizers.forEach((randomizer) -> {
				addEntry(new RandomizerConfigScreen.DetailsList.Entry(randomizer));
			});

			var list = children();
			if (i >= 0 && i < list.size()) {
				setSelected(list.get(i));
			}
		}

		public List<Randomizer> items() {
			return children().stream().map((entry -> entry.randomizer)).toList();
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<Entry> {

			private Randomizer randomizer;

			public Entry(Randomizer randomizer) {
				this.randomizer = randomizer;
			}

			private static void drawRadialButtonBackgrounds(PoseStack poseStack, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, int selected) {
				var buffer = Tesselator.getInstance().getBuilder();
				buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				final var quarterCircle = Math.PI / 2.0;
				final int totalModes = Math.max(3, RADIAL_SIZE);
				final double fragment = Math.PI * 0.04; //gap between buttons in radians at inner edge
				final double fragment2 = fragment * ringInnerEdge / ringOuterEdge; //gap between buttons in radians at outer edge
				final double radiansPerObject = 2.0 * Math.PI / totalModes;

				for (int i = 0; i < RADIAL_SIZE; i++) {
					final double beginRadians = (i - 0.5) * radiansPerObject - quarterCircle;
					final double endRadians = (i + 0.5) * radiansPerObject - quarterCircle;

					final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
					final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
					final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
					final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

					final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
					final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
					final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
					final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

					var color = RADIAL_COLOR;
					if (selected == i) color = HIGHLIGHT_COLOR;


					buffer.vertex(middleX + x1m1, middleY + y1m1, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
					buffer.vertex(middleX + x2m1, middleY + y2m1, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
					buffer.vertex(middleX + x2m2, middleY + y2m2, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
					buffer.vertex(middleX + x1m2, middleY + y1m2, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
				}
				Tesselator.getInstance().end();
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.fill(poseStack, k, j, k + ICON_WIDTH, j + ICON_WIDTH, 0x9f6c6c6c);

				drawRadialButtonBackgrounds(poseStack,  k + (ICON_WIDTH >> 1), j + (ICON_HEIGHT >> 1), RING_INNER_EDGE, RING_OUTER_EDGE, i + 1);

				if (o > j && o < j + ICON_WIDTH) {
					RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					int v = n - k;
					int w = o - j;

					if (i > 0) {
						GuiComponent.blit(poseStack, k + 8, j, 96.0F, 8 < v && v < 24 && w < 16 ? 32.0F : 0.0F, 32, 32, 256, 256);
					}
					if (i < children().size() - 1) {
						GuiComponent.blit(poseStack, k + 8, j, 64.0F, 8 < v && v < 24 && w > 16 ? 32.0F : 0.0F, 32, 32, 256, 256);
					}
				}
				drawString(poseStack, minecraft.font, getDisplayName(randomizer), k + 2 + 32 + 1 , j + 2, 0xFFFFFFFF);

				var slot = new AtomicInteger(0);
				randomizer.holders().forEach((holder) -> {
					var last = slot.getAndIncrement();
					if (last >= MAX_SLOT_COUNT) {
						if (last == MAX_SLOT_COUNT) {
							blitExtraSlot(poseStack, k + last * SLOT_OFFSET_X + ICON_WIDTH + 1, j + 10 + 1);
						}
						return;
					}
					blitSlot(poseStack, k + last * SLOT_OFFSET_X + ICON_WIDTH + 1, j + 10 + 1, holder);
				});
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", getDisplayName(randomizer));
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				var f = d - getRowLeft();
				var g = e - getRowTop(children().indexOf(this));
				var height = itemHeight + 4;
				if (f <= 32.0) {

					int j = children().indexOf(this);
					if (8 < f && f < 24 && g < 16 && j > 0) { // move down
						swap(j, j - 1);
						DetailsList.this.scrollAmount = getScrollAmount() - height;
						return true;
					}

					if (8 < f && f < 24 && g > 16 && j < children().size() - 1) { // move up
						swap(j, j + 1);
						DetailsList.this.scrollAmount = getScrollAmount() + height;
						return true;
					}
				}
				RandomizerConfigScreen.DetailsList.this.setSelected(this);
				return false;
			}

			public void setRandomizer(Randomizer randomizer) {
				this.randomizer = randomizer;
			}

			private Component getDisplayName(Randomizer randomizer) {
				if (randomizer.isEmpty()) {
					return Component.literal("" + ChatFormatting.GRAY + ChatFormatting.ITALIC + "Empty Randomizer" + ChatFormatting.RESET);
				}
				if (randomizer.name().isBlank()) {
					return Component.literal("" + ChatFormatting.GRAY + ChatFormatting.ITALIC + "No Name" + ChatFormatting.RESET);
				}
				return Component.literal(randomizer.name());
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

			private void blitExtraSlot(PoseStack poseStack, int i, int j) {
				blitSlotBgExtra(poseStack, i + 1, j + 1);
			}

			private void blitSlotBgExtra(PoseStack poseStack, int i, int j) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
				GuiComponent.blit(poseStack, i, j, getBlitOffset(), 0.0F, 0.0F, 5, 18, 128, 128);
			}

		}
	}
}
