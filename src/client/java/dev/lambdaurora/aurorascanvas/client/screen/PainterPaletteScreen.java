/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.menu.PainterPaletteMenu;
import dev.lambdaurora.aurorascanvas.menu.slot.CanvasToolSlot;
import dev.lambdaurora.aurorascanvas.menu.slot.ColorSlot;
import dev.lambdaurora.aurorascanvas.menu.slot.LockedSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Represents the painter's palette container screen.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class PainterPaletteScreen extends AbstractContainerScreen<PainterPaletteMenu> {
	private static final Identifier TEXTURE = AurorasCanvas.id("textures/gui/container/painter_palette.png");

	public PainterPaletteScreen(PainterPaletteMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title, 176, 166);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	public int getBackgroundX() {
		return (this.width - this.imageWidth) / 2 - 24;
	}

	public int getBackgroundY() {
		return (this.height - this.imageHeight) / 2;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
				this.getBackgroundX(), this.getBackgroundY(), 0, 0, this.imageWidth + 24, this.imageHeight, 256, 256
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		for (var slot : this.menu.slots) {
			if (slot instanceof CanvasToolSlot) {
				int x = this.getBackgroundX() + slot.x + 24 - 1;
				int y = this.getBackgroundY() + slot.y - 1;

				if (slot.getItem().isEmpty()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.TOOL_SLOT_SPRITE, x, y, 18, 18);
				} else {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SLOT_SPRITE, x, y, 18, 18);
				}
			}
		}

		super.extractRenderState(graphics, mouseX, mouseY, delta);

		for (var slot : this.menu.slots) {
			if (slot instanceof LockedSlot) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.LOCKED_SPRITE, slot.x + 24 + 9, slot.y + 9, 8, 8);
			} else if ((slot instanceof ColorSlot && slot.getContainerSlot() == this.menu.getInventory().getSelectedColorSlot())
					|| (slot instanceof CanvasToolSlot && slot.getContainerSlot() == this.menu.getInventory().getSelectedToolSlot())) {
				this.extractSelectedIndicator(graphics, slot.x + 24, slot.y);
			}
		}
	}

	private void extractSelectedIndicator(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.SELECT_HIGHLIGHT_SPRITE, x - 3, y - 3, 22, 22);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 2) {
			if (this.hasClickedOutside(event.x(), event.y(), this.leftPos, this.topPos)) {
				if (this.menu.clickMenuButton(this.minecraft.player, this.menu.getInventory().getContainerSize())) {
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.menu.getInventory().getContainerSize());
					return true;
				}
			} else {
				int slot = this.getSlotAt(event.x(), event.y());

				if (slot != -1 && this.menu.clickMenuButton(this.minecraft.player, slot)) {
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, slot);
					return true;
				}
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	private int getSlotAt(double x, double y) {
		for (int i = 0; i < this.menu.slots.size(); ++i) {
			Slot slot = this.menu.slots.get(i);
			if (this.isPointOverSlot(slot, x, y) && slot.isActive()) {
				return i;
			}
		}

		return -1;
	}

	private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
		return this.isHovering(slot.x, slot.y, 16, 16, pointX, pointY);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
		return super.hasClickedOutside(mouseX, mouseY, left, top)
				&& (mouseX < this.getBackgroundX() || mouseY < this.getBackgroundY() || mouseY > this.getBackgroundY() + 86 || mouseY > this.getBackgroundX() + 10);
	}
}
