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
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.client.screen.PainterPaletteScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ToolButton extends BaseToolButton {
	private static final Sprites PENCIL_SPRITES = new Sprites(
			AurorasCanvas.id("editor/pencil"),
			AurorasCanvas.id("editor/pencil_disabled")
	);
	private static final Sprites FILL_SPRITES = new Sprites(
			AurorasCanvas.id("editor/fill"),
			AurorasCanvas.id("editor/fill_disabled")
	);
	private static final Sprites REPLACE_SPRITE = new Sprites(
			AurorasCanvas.id("editor/replace"),
			AurorasCanvas.id("editor/replace_disabled")
	);

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
		super.renderWidget(graphics, mouseX, mouseY, partialTick);
		int color = (this.active ? 0x00ffffff : 0x00a0a0a0) | Mth.ceil(this.alpha * 255.0F) << 24;

		var item = drawAction.getOffHandTool(client.level.enabledFeatures());
		if (this.drawAction == DrawAction.DEFAULT || this.drawAction == DrawAction.FILL || this.drawAction == DrawAction.REPLACE) {
			graphics.blitSprite((switch (this.drawAction) {
				case FILL -> FILL_SPRITES;
				case REPLACE -> REPLACE_SPRITE;
				default -> PENCIL_SPRITES;
			}).get(this.active), this.getX(), this.getY(), 20, 20);
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
			graphics.blitSprite(PainterPaletteScreen.SELECT_HIGHLIGHT_SPRITE, this.getX() - 1, this.getY() - 1, 22, 22);
		}
	}

	private record Sprites(Identifier active, Identifier disabled) {
		Identifier get(boolean active) {
			return active ? this.active : this.disabled;
		}
	}
}
