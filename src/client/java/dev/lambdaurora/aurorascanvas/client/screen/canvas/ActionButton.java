/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class ActionButton extends BaseToolButton {
	protected final CanvasController controller;
	private final int uOffset;

	protected ActionButton(
			CanvasController controller,
			Component name,
			int x, int y, int uOffset,
			Consumer<TrackedCanvasHandler> action,
			Predicate<TrackedCanvasHandler> predicate
	) {
		super(x, y, 20, 20, name, button -> {
			action.accept(controller.canvas);
		}, Button.DEFAULT_NARRATION);
		this.controller = controller;
		this.uOffset = uOffset;
		this.controller.canvas.history().addListener(history -> this.active = predicate.test(this.controller.canvas));

		this.setTooltip(Tooltip.create(name));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(graphics, mouseX, mouseY, partialTick);

		int vOffset = 256 - 40;
		graphics.blit(CanvasScreen.TEXTURE, this.getX(), this.getY(), this.uOffset, vOffset + (this.active ? 0 : 20), 20, 20);

		graphics.setColor(1.f, 1.f, 1.f, 1.f);
	}
}
