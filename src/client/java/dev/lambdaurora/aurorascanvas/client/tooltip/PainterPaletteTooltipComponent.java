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
import dev.lambdaurora.aurorascanvas.client.screen.PainterPaletteScreen;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Represents the painter's palette tooltip component.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class PainterPaletteTooltipComponent implements ClientTooltipComponent {
	private final PainterPaletteInventory inventory;
	private final Component selectedToolText;

	public PainterPaletteTooltipComponent(PainterPaletteInventory inventory) {
		this.inventory = inventory;
		var enabledFeatures = Minecraft.getInstance().level.enabledFeatures();
		this.selectedToolText = PainterPaletteItem.getSelectedToolMessage(inventory, enabledFeatures).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public int getHeight() {
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
	public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource immediate) {
		font.drawInBatch(
				this.selectedToolText, x, y, 0xffffffff, true, matrix4f, immediate, Font.DisplayMode.NORMAL,
				0, LightTexture.FULL_BRIGHT
		);
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return;

		var matrices = graphics.pose();
		byte previousColorIndex = this.inventory.findFirstPreviousColor();
		ItemStack previousColorStack = this.inventory.getPreviousColorStack();
		byte nextColorIndex = this.inventory.findFirstNextColor();
		ItemStack nextColorStack = this.inventory.getNextColorStack();

		matrices.pushPose();
		y += 12;

		matrices.translate(x, y, 0);
		this.drawSlot(graphics, previousColorStack, previousColorIndex, true, false);

		matrices.translate(18, 0, 0);
		this.drawSlot(graphics, primaryColorStack, inventory.getSelectedColorSlot(), false, false);
		AbstractContainerScreen.renderSlotHighlight(graphics, 2, 2, 0);

		matrices.translate(18, 0, 0);
		this.drawSlot(graphics, nextColorStack, nextColorIndex, false, true);

		matrices.popPose();
	}

	private void drawSlot(
			GuiGraphics graphics, ItemStack stack,
			int index, boolean start, boolean end
	) {
		graphics.blitSprite(PainterPaletteScreen.SLOT_SPRITE, 1, 1, 0, 18, 18);

		if (!stack.isEmpty()) {
			graphics.renderItem(stack, 2, 2, index);
			this.drawColorOverlay(graphics, stack);
		}
	}

	private void drawColorOverlay(GuiGraphics graphics, ItemStack stack) {
		var color = CanvasColor.fromItem(stack.getItem());
		if (color != null) {
			var matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(14, 14, 210);
			graphics.fill(0, 0, 4, 4, color.getColor());
			matrices.popPose();
		}
	}
}
