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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Represents the painter's palette container screen.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class PainterPaletteScreen extends AbstractContainerScreen<PainterPaletteMenu> {
	private static final Identifier TEXTURE = AurorasCanvas.id("textures/gui/container/painter_palette.png");
	private static final Identifier LOCK_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/cartography_table.png");

	public PainterPaletteScreen(PainterPaletteMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		this.imageHeight += 2;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	public int getBackgroundX() {
		return (this.width - this.imageWidth) / 2 - 24;
	}

	public int getBackgroundY() {
		return (this.height - this.imageHeight) / 2;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		graphics.setColor(1.f, 1.f, 1.f, 1.f);
		graphics.blit(TEXTURE,
				this.getBackgroundX(), this.getBackgroundY(), 0, 0, this.imageWidth + 24, this.imageHeight
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics, mouseX, mouseY, delta);

		for (var slot : this.menu.slots) {
			if (slot instanceof CanvasToolSlot) {
				int x = this.getBackgroundX() + slot.x + 24 - 1;
				int y = this.getBackgroundY() + slot.y - 1;

				if (slot.getItem().isEmpty()) {
					graphics.blit(TEXTURE, x, y, this.imageWidth + 26, 24, 18, 18, 256, 256);
				} else {
					graphics.blit(TEXTURE, x, y, this.imageWidth + 26, 42, 18, 18, 256, 256);
				}
			}
		}

		super.render(graphics, mouseX, mouseY, delta);

		var matrices = graphics.pose();
		matrices.pushPose();
		matrices.translate(this.getBackgroundX(), this.getBackgroundY(), 275);
		for (var slot : this.menu.slots) {
			if (slot instanceof LockedSlot) {
				matrices.pushPose();
				matrices.translate(slot.x + 24 + 4, slot.y + 4, 0);
				matrices.scale(.75f, .75f, 1);
				graphics.blit(LOCK_TEXTURE, 0, 0, 46, 212, 16, 16, 256, 256);
				matrices.popPose();
			} else if ((slot instanceof ColorSlot && slot.getContainerSlot() == this.menu.getInventory().getSelectedColorSlot())
					|| (slot instanceof CanvasToolSlot && slot.getContainerSlot() == this.menu.getInventory().getSelectedToolSlot())) {
				matrices.pushPose();
				matrices.translate(slot.x + 24, slot.y, 0);
				this.drawSelectedIndicator(graphics);
				matrices.popPose();
			}
		}
		matrices.popPose();

		this.renderTooltip(graphics, mouseX, mouseY);
	}

	private void drawSelectedIndicator(GuiGraphics graphics) {
		graphics.blit(TEXTURE, -3, -3, this.imageWidth + 24, 0, 22, 22, 256, 256);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 2) {
			if (this.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, button)) {
				if (this.menu.clickMenuButton(this.minecraft.player, this.menu.getInventory().getContainerSize())) {
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.menu.getInventory().getContainerSize());
					return true;
				}
			} else {
				int slot = this.getSlotAt(mouseX, mouseY);

				if (slot != -1 && this.menu.clickMenuButton(this.minecraft.player, slot)) {
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, slot);
					return true;
				}
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
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
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
		return super.hasClickedOutside(mouseX, mouseY, left, top, button)
				&& (mouseX < this.getBackgroundX() || mouseY < this.getBackgroundY() || mouseY > this.getBackgroundY() + 86 || mouseY > this.getBackgroundX() + 10);
	}
}
