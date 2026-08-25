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
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ToolButton extends Button {
	protected final CanvasController controller;
	protected final DrawAction drawAction;

	protected ToolButton(
			CanvasController controller,
			List<ToolButton> buttons,
			int x, int y, int width, int height,
			DrawAction drawAction
	) {
		super(x, y, width, height, drawAction.getName(), button -> {
			controller.currentAction = drawAction;
			buttons.forEach(ToolButton::computeState);
		}, Button.DEFAULT_NARRATION);
		this.controller = controller;
		this.drawAction = drawAction;

		this.computeState();
		this.setTooltip(Tooltip.create(drawAction.getName()));
	}

	private void computeState() {
		this.active = this.controller.getAvailableTools().contains(this.drawAction);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		var client = Minecraft.getInstance();
		graphics.setColor(1.f, 1.f, 1.f, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
		graphics.setColor(1.f, 1.f, 1.f, 1.f);
		int color = (this.active ? 0x00ffffff : 0x00a0a0a0) | Mth.ceil(this.alpha * 255.0F) << 24;

		var item = drawAction.getOffHandTool(client.level.enabledFeatures());
		if (this.drawAction == DrawAction.DEFAULT || this.drawAction == DrawAction.FILL || this.drawAction == DrawAction.REPLACE) {
			int vOffset = 256 - 80;
			graphics.blit(CanvasScreen.TEXTURE, this.getX(), this.getY(), switch (this.drawAction) {
				case FILL -> 20;
				case REPLACE -> 40;
				default -> 0;
			}, vOffset + (this.active ? 0 : 20), 20, 20);
		} else if (item != null) {
			var matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(0.f, 0.f, 232.f);
			graphics.setColor(
					FastColor.ARGB32.red(color) / 255.f,
					FastColor.ARGB32.green(color) / 255.f,
					FastColor.ARGB32.blue(color) / 255.f,
					this.alpha
			);
			graphics.renderItem(new ItemStack(item), this.getX() + 2, this.getY() + 2);
			matrices.popPose();
		} else {
			this.renderString(graphics, client.font, color);
		}

		if (this.controller.currentAction == this.drawAction) {
			graphics.blit(CanvasScreen.TEXTURE,
					this.getX() - 1, this.getY() - 1, 176 + 24, 0, 22, 22, 256, 256
			);
		}
	}

	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}

		return 46 + i * 20;
	}
}
