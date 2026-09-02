/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.client.screen.SpriteIds;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTextures;
import dev.lambdaurora.spruceui.border.Border;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceEntryListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ColorSelectorWidget extends SpruceEntryListWidget<ColorSelectorWidget.Entry> {
	private static final Identifier COLOR_SLOTS_SPRITE = AurorasCanvas.id("editor/color_slots");
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");

	private static final int MARGIN = 7;
	private final CanvasController controller;

	public ColorSelectorWidget(CanvasScreen screen, int y0, int x0) {
		super(Position.of(screen, x0, y0 - MARGIN), 40, 126 + 14, 0, Entry.class);
		this.controller = screen.controller;

		this.setBorder(new WidgetBorder());
		this.setBackground((graphics, widget, _, _, _, _) -> {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, COLOR_SLOTS_SPRITE, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
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
	protected void extractWidgetRenderState(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);

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
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SELECT_HIGHLIGHT_SPRITE, entry.getX() - 2, entry.getY() - 2, 22, 22);
				graphics.disableScissor();
				break;
			}
		}
	}

	@Override
	protected void extractScrollbar(
			SpruceGuiGraphics graphics, int mouseX, int mouseY
	) {
		if (this.isScrollbarVisible()) {
			int top = this.getInnerBorderedY();
			int height = this.getInnerBorderedHeight();
			int scrollbarX = this.getScrollbarPositionX();
			int scrollerHeight = (int) ((float) (height * height) / (float) this.getMaxPosition());
			scrollerHeight = Mth.clamp(scrollerHeight, 32, height - 8);
			int scrollbarY = (int) this.getScrollAmount() * (height - scrollerHeight) / this.getMaxScroll() + top;
			if (scrollbarY < top) {
				scrollbarY = top;
			}

			scrollbarY++;
			scrollerHeight -= 2;

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpruceTextures.SCROLLER_BACKGROUND,
					scrollbarX, top, 4, this.getInnerBorderedHeight()
			);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpruceTextures.SCROLLER,
					scrollbarX, scrollbarY, 4, scrollerHeight
			);

			if (this.isOverScrollbar(mouseX, mouseY)) {
				//graphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
			}
		}
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
		protected boolean onMouseRelease(MouseButtonEvent event) {
			this.setFocused(true);
			this.controller.currentModifier = this.modifier;

			return super.onMouseRelease(event);
		}

		@Override
		protected void extractWidgetRenderState(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SLOT_SPRITE, this.getX(), this.getY(), 18, 18);

			graphics.vanilla().item(this.stack, this.getX() + 1, this.getY() + 1);

			if (this.isMouseHovered()) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, this.getX() + 1, this.getY() + 1, 24, 24);
			}
		}
	}

	public static class WidgetBorder implements Border {
		@Override
		public void extractRenderState(SpruceGuiGraphics graphics, SpruceWidget widget, int mouseX, int mouseY, float delta) {
		}

		@Override
		public int getThickness() {
			return 7;
		}
	}
}
