/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.client.screen.SpriteIds;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.border.Border;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceEntryListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ColorSelectorWidget extends SpruceEntryListWidget<ColorSelectorWidget.Entry> {
	private static final Identifier COLOR_SLOTS_SPRITE = AurorasCanvas.id("editor/color_slots");

	private static final int MARGIN = 7;
	private final CanvasController controller;

	public ColorSelectorWidget(CanvasScreen screen, int y0, int x0) {
		super(Position.of(screen, x0, y0 - MARGIN), 40, 126 + 14, 0, Entry.class);
		this.controller = screen.controller;

		this.setBorder(new WidgetBorder());
		this.setBackground((graphics, widget, vOffset, mouseX, mouseY, delta) -> {
			graphics.blitSprite(COLOR_SLOTS_SPRITE, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
		});

		for (var colorStack : screen.controller.getAvailableColors()) {
			var entry = new Entry(screen.controller, colorStack);

			if (entry.modifier == screen.controller.currentModifier) {
				entry.setFocused(true);
			}

			this.addEntry(entry);
		}

		this.setScrollAmount(0);
	}

	@Override
	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - this.getHeight() + 14);
	}

	@Override
	protected int getScrollbarPositionX() {
		return this.getEndInnerBorderedX() - 4;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);

		int left = this.getInnerBorderedX();
		int right = this.getEndInnerBorderedX();
		int top = this.getInnerBorderedY();
		int bottom = this.getEndInnerBorderedY();
		int height = this.getInnerBorderedHeight();

		for (int i = 0; i < this.getEntriesCount(); i++) {
			var entry = this.getEntry(i);

			if (entry.modifier == this.controller.currentModifier) {
				int realTop = entry.getY() - 2 < top - 2 ? top : (top - 2);
				int realBottom = entry.getY() + entry.getHeight() + 2 > bottom + 2 ? bottom : (bottom + 2);
				graphics.enableScissor(left - 2, realTop, right, realBottom);
				graphics.blitSprite(SpriteIds.SELECT_HIGHLIGHT_SPRITE, entry.getX() - 2, entry.getY() - 2, 22, 22);
				graphics.disableScissor();
				break;
			}
		}
	}

	@Override
	protected void renderScrollbar(
			Tesselator tessellator, BufferBuilder buffer,
			int scrollbarX, int scrollbarEndX,
			int scrollbarY, int scrollbarHeight
	) {
		scrollbarEndX -= 2;
		scrollbarY++;
		scrollbarHeight -= 2;
		int y = this.getInnerBorderedY();
		int endY = this.getEndInnerBorderedY();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.addVertex(scrollbarX, scrollbarY + scrollbarHeight, 0.0f).setColor(128, 128, 128, 255);
		buffer.addVertex(scrollbarEndX, scrollbarY + scrollbarHeight, 0.0f).setColor(128, 128, 128, 255);
		buffer.addVertex(scrollbarEndX, scrollbarY, 0.0f).setColor(128, 128, 128, 255);
		buffer.addVertex(scrollbarX, scrollbarY, 0.0f).setColor(128, 128, 128, 255);
		buffer.addVertex(scrollbarX, scrollbarY + scrollbarHeight - 1, 0.0f).setColor(192, 192, 192, 255);
		buffer.addVertex(scrollbarEndX - 1, scrollbarY + scrollbarHeight - 1, 0.0f).setColor(192, 192, 192, 255);
		buffer.addVertex(scrollbarEndX - 1, scrollbarY, 0.0f).setColor(192, 192, 192, 255);
		buffer.addVertex(scrollbarX, scrollbarY, 0.0f).setColor(192, 192, 192, 255);
		MeshData builtBuffer = buffer.build();
		if (builtBuffer != null) {
			BufferUploader.drawWithShader(builtBuffer);
		}
		tessellator.clear();
	}

	public static class Entry extends SpruceEntryListWidget.Entry {
		private final CanvasController controller;
		private final ItemStack stack;
		private final DrawModifier modifier;

		public Entry(CanvasController controller, ItemStack stack) {
			this.controller = controller;
			this.stack = stack;
			this.modifier = Objects.requireNonNull(DrawModifier.fromItem(stack));

			this.width = 18;
			this.height = 18;
		}

		@Override
		protected boolean onMouseRelease(double mouseX, double mouseY, int button) {
			this.setFocused(true);
			this.controller.currentModifier = this.modifier;

			return super.onMouseRelease(mouseX, mouseY, button);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.blitSprite(SpriteIds.SLOT_SPRITE, this.getX(), this.getY(), 18, 18);

			var matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(0.f, 0.f, 232.f);
			graphics.renderItem(this.stack, this.getX() + 1, this.getY() + 1);

			if (this.isMouseHovered()) {
				AbstractContainerScreen.renderSlotHighlight(graphics, this.getX() + 1, this.getY() + 1, 0);
			}

			matrices.popPose();
		}
	}

	public static class WidgetBorder implements Border {
		@Override
		public void render(GuiGraphics graphics, SpruceWidget widget, int mouseX, int mouseY, float delta) {
		}

		@Override
		public int getThickness() {
			return 7;
		}
	}
}
