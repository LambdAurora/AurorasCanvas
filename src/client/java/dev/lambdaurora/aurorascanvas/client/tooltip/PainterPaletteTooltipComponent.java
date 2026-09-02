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

		matrices.pushMatrix();
		y += 12;

		matrices.translate(x, y);
		this.extractSlot(graphics, previousColorStack, previousColorIndex, true, false);

		matrices.translate(18, 0);
		this.extractSlot(graphics, primaryColorStack, inventory.getSelectedColorSlot(), false, false);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, 2, 2, 24, 24);

		matrices.translate(18, 0);
		this.extractSlot(graphics, nextColorStack, nextColorIndex, false, true);

		matrices.popMatrix();
	}

	private void extractSlot(
			GuiGraphicsExtractor graphics, ItemStack stack,
			int index, boolean start, boolean end
	) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SLOT_SPRITE, 1, 1, 0, 18, 18);

		if (!stack.isEmpty()) {
			graphics.item(stack, 2, 2, index);
			this.drawColorOverlay(graphics, stack);
		}
	}

	private void drawColorOverlay(GuiGraphicsExtractor graphics, ItemStack stack) {
		var color = CanvasColor.fromItem(stack.getItem());
		if (color != null) {
			graphics.fill(14, 14, 4, 4, color.getColor());
		}
	}
}
