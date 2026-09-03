/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.tooltip;

import dev.lambdaurora.aurorascanvas.canvas.CanvasColor;
import dev.lambdaurora.aurorascanvas.client.screen.SpriteIds;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the painter's palette tooltip component.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class PainterPaletteTooltipComponent implements ClientTooltipComponent {
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");

	private final PainterPaletteInventory inventory;
	private final Component selectedToolText;

	public PainterPaletteTooltipComponent(PainterPaletteInventory inventory) {
		this.inventory = inventory;
		var enabledFeatures = Minecraft.getInstance().level.enabledFeatures();
		this.selectedToolText = PainterPaletteItem.getSelectedToolMessage(inventory, enabledFeatures).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public int getHeight(final Font font) {
		int height = 12;
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return height;

		return height + 24;
	}

	@Override
	public int getWidth(Font font) {
		int width = font.width(this.selectedToolText);

		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return width;

		return Math.max(width, 18 * 5 + 2);
	}

	@Override
	public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
		graphics.text(font, this.selectedToolText, x, y, 0xffffffff, true);
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return;

		var matrices = graphics.pose();
		byte previousColorIndex = this.inventory.findFirstPreviousColor();
		ItemStack previousColorStack = this.inventory.getPreviousColorStack();
		byte nextColorIndex = this.inventory.findFirstNextColor();
		ItemStack nextColorStack = this.inventory.getNextColorStack();

		y += 12;

		this.extractSlot(graphics, previousColorStack, -1, x, y);

		this.extractSlot(graphics, primaryColorStack, 0, x += 20, y);

		this.extractSlot(graphics, nextColorStack, 1, x + 20, y);

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SELECT_HIGHLIGHT_SPRITE, x - 2, y - 2, 24, 24);
	}

	private void extractSlot(
			GuiGraphicsExtractor graphics, ItemStack stack,
			int index, int x, int y
	) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SLOT_SPRITE, x + 1, y + 1, 18, 18);

		if (!stack.isEmpty()) {
			graphics.item(stack, x + 2, y + 2, index);
			this.drawColorOverlay(graphics, stack, x, y);
		}
	}

	private void drawColorOverlay(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
		var color = CanvasColor.fromItem(stack.getItem());
		if (color != null) {
			graphics.fill(x + 14, y + 14, x + 18, y + 18, color.getColor());
		}
	}
}
