package dev.huskcasaca.effortless.screen.pattern;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.huskcasaca.effortless.building.pattern.Pattern;
import dev.huskcasaca.effortless.building.pattern.PatternSettings;
import dev.huskcasaca.effortless.building.pattern.randomizer.ItemProbability;
import dev.huskcasaca.effortless.screen.config.EditorList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EffortlessPatternSettingsScreen extends Screen {

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
	private final Consumer<PatternSettings> applySettings;
	private PatternSettings lastSettings;
	private DetailsList entries;
	private Button editRandomizerButton;
	private Button deleteRandomizerButton;

	public EffortlessPatternSettingsScreen(Screen screen, Consumer<PatternSettings> consumer, PatternSettings patternSettings) {
		super(Component.translatable("randomizer.config.title"));
		this.parent = screen;
		this.applySettings = consumer;
		this.lastSettings = patternSettings;
	}

	private void updateSettings() {
		lastSettings = new PatternSettings(
				entries.items()
		);
	}

	@Override
	protected void init() {
		this.entries = addRenderableWidget(new DetailsList(minecraft, width, height, 32, height - 60, 24 + 12));
		this.entries.reset(lastSettings.patterns());
		addRenderableWidget(new CenteredStringWidget(width, 49, title, minecraft.font));

		this.editRandomizerButton = addRenderableWidget(Button.builder(Component.translatable("Edit"), (button) -> {
//			if (hasValidSelection()) {
//				minecraft.setScreen(new EffortlessRandomizerEditScreen(this,
//						(randomizer) -> {
//							entries.replaceSelect(randomizer);
//							updateSettings();
//						},
//						entries.getSelected().getItem()));
//			}
		}).bounds(width / 2 - 154, height - 52, 72, 20).build());

		this.deleteRandomizerButton = addRenderableWidget(Button.builder(Component.translatable("Delete"), (button) -> {
			if (hasValidSelection()) {
				entries.deleteSelected();
				updateSettings();
			}
		}).bounds(width / 2 - 76, height - 52, 72, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("Create New Pattern"), (button) -> {
//			minecraft.setScreen(new EffortlessRandomizerEditScreen(this,
//					(randomizer) -> {
//						entries.insertSelected(randomizer);
//						updateSettings();
//					},
//					Randomizer.EMPTY));
		}).bounds(width / 2 + 4, height - 52, 150, 20).build());


		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			updateSettings();
			applySettings.accept(lastSettings);
			minecraft.setScreen(parent);
		}).bounds(width / 2 - 154, height - 28, 150, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			minecraft.setScreen(parent);
		}).bounds(width / 2 + 4, height - 28, 150, 20).build());
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
		updateButtonValidity();
	}

	@Environment(EnvType.CLIENT)
	class DetailsList extends EditorList<Pattern> {
		public DetailsList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
		}

		private static void drawRadialButtonBackgrounds(PoseStack poseStack, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, int selected) {
			var bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			var totalModes = Math.max(3, RADIAL_SIZE);
			var innerGap = Math.PI * 0.04; //gap between buttons in radians at inner edge
			var outerGap = innerGap * ringInnerEdge / ringOuterEdge; //gap between buttons in radians at outer edge
			var rad = 2.0 * Math.PI / totalModes;

			for (int i = 0; i < RADIAL_SIZE; i++) {
				var begRad = (i - 0.5) * rad - Math.PI / 2.0;
				var endRad = (i + 0.5) * rad - Math.PI / 2.0;

				var x1m1 = Math.cos(begRad + innerGap) * ringInnerEdge;
				var x2m1 = Math.cos(endRad - innerGap) * ringInnerEdge;
				var y1m1 = Math.sin(begRad + innerGap) * ringInnerEdge;
				var y2m1 = Math.sin(endRad - innerGap) * ringInnerEdge;

				var x1m2 = Math.cos(begRad + outerGap) * ringOuterEdge;
				var x2m2 = Math.cos(endRad - outerGap) * ringOuterEdge;
				var y1m2 = Math.sin(begRad + outerGap) * ringOuterEdge;
				var y2m2 = Math.sin(endRad - outerGap) * ringOuterEdge;

				var color = RADIAL_COLOR;
				if (selected == i) color = HIGHLIGHT_COLOR;

				bufferBuilder.vertex(middleX + x1m1, middleY + y1m1, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
				bufferBuilder.vertex(middleX + x2m1, middleY + y2m1, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
				bufferBuilder.vertex(middleX + x2m2, middleY + y2m2, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
				bufferBuilder.vertex(middleX + x1m2, middleY + y1m2, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
			}
			Tesselator.getInstance().end();
		}

		@Override
		protected boolean isFocused() {
			return EffortlessPatternSettingsScreen.this.getFocused() == this;
		}

		@Override
		protected void renderEntry(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f, Entry entry) {
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
			drawString(poseStack, minecraft.font, getDisplayName(entry.getItem()), k + 2 + 32 + 1 , j + 2, 0xFFFFFFFF);

//			var slot = new AtomicInteger(0);
//			entry.getItem().holders().forEach((holder) -> {
//				var last = slot.getAndIncrement();
//				if (last >= MAX_SLOT_COUNT) {
//					if (last == MAX_SLOT_COUNT) {
//						blitExtraSlot(poseStack, k + last * SLOT_OFFSET_X + ICON_WIDTH + 1, j + 10 + 1);
//					}
//					return;
//				}
//				blitSlot(poseStack, k + last * SLOT_OFFSET_X + ICON_WIDTH + 1, j + 10 + 1, holder);
//			});
		}

		@Override
		protected void renderDecorations(PoseStack poseStack, int i, int j) {
			super.renderDecorations(poseStack, i, j);
			var entry = this.getHovered();

			if (entry == null || !entry.isMouseOver(i, j)) {
				return;
			}

//			var x0 = (this.width - ROW_WIDTH) / 2 + 2;
//			var x1 = (this.width - ROW_WIDTH) / 2 + ICON_WIDTH + 2;
//
//			if (i >= x0 && i <= x1) {
//				var index = children().indexOf(entry);
//				if (index + 1 < RADIAL_SIZE) {
//					renderTooltip(poseStack, Component.literal("Slot " + (index + 1)), i, j);
//				} else {
//					renderTooltip(poseStack, Component.literal("Not in Slot"), i, j);
//				}
//				return;
//			}
//
//			if (entry.isMouseOver(i, j - 13) && entry.isMouseOver(i, j + 3)) {
//				var index = new AtomicInteger(0);
//				var holders = entry.getItem().holders();
//				var totalCount = holders.stream().mapToInt((holder) -> holder.count()).sum();
//				for (var holder : holders) {
//					var last = index.getAndIncrement();
//					if (last > MAX_SLOT_COUNT) break;
//
//					x0 = (this.width - ROW_WIDTH) / 2 + 3 + last * SLOT_OFFSET_X + ICON_WIDTH + 1;
//					x1 = (this.width - ROW_WIDTH) / 2 + 3 + (last + 1) * SLOT_OFFSET_X + (last == MAX_SLOT_COUNT ? ICON_WIDTH / 2 : ICON_WIDTH);
//					if (i < x0 || i > x1) continue;
//
//					if (last == MAX_SLOT_COUNT) {
//						renderComponentTooltip(poseStack, Collections.singletonList(Component.literal("+" + (holders.size() - MAX_SLOT_COUNT) + " More")), i, j);
//					} else {
//						renderComponentTooltip(poseStack, RandomizerUtils.getRandomizerEntryTooltip(holder, totalCount), i, j);
//					}
//				}
//			}
		}

		@Override
		protected Component getNarration(Pattern pattern) {
			return Component.translatable("narrator.select", getDisplayName(pattern));
		}

		private Component getDisplayName(Pattern pattern) {
			if (pattern.isEmpty()) {
				return Component.literal("" + ChatFormatting.GRAY + ChatFormatting.ITALIC + "Empty Pattern" + ChatFormatting.RESET);
			}
			if (pattern.name().isBlank()) {
				return Component.literal("" + ChatFormatting.GRAY + ChatFormatting.ITALIC + "No Name" + ChatFormatting.RESET);
			}
			return Component.literal(pattern.name());
		}

		private void blitSlot(PoseStack poseStack, int i, int j, ItemProbability holder) {
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
