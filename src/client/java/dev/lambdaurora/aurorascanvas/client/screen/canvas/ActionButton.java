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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class ActionButton extends BaseToolButton {
	static final Identifier UNDO_SPRITE = AurorasCanvas.id("editor/undo");
	static final Identifier DISABLED_UNDO_SPRITE = AurorasCanvas.id("editor/undo_disabled");
	static final Identifier REDO_SPRITE = AurorasCanvas.id("editor/redo");
	static final Identifier DISABLED_REDO_SPRITE = AurorasCanvas.id("editor/redo_disabled");
	static final Identifier CLEAR_SPRITE = AurorasCanvas.id("editor/clear");
	static final Identifier DISABLED_CLEAR_SPRITE = AurorasCanvas.id("editor/clear_disabled");

	protected final CanvasController controller;
	private final Identifier sprite;
	private final Identifier disabledSprite;

	protected ActionButton(
			CanvasController controller,
			Component name,
			int x, int y,
			Identifier sprite, Identifier disabledSprite,
			Consumer<TrackedCanvasHandler> action,
			Predicate<TrackedCanvasHandler> predicate
	) {
		super(x, y, 20, 20, name, button -> {
			action.accept(controller.canvas.getDefault());
		}, Button.DEFAULT_NARRATION);
		this.controller = controller;
		this.sprite = sprite;
		this.disabledSprite = disabledSprite;
		this.controller.canvas.getDefault().history().addListener(history -> this.active = predicate.test(this.controller.canvas.getDefault()));

		this.setTooltip(Tooltip.create(name));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(graphics, mouseX, mouseY, partialTick);

		graphics.blitSprite(this.active ? this.sprite : this.disabledSprite, this.getX(), this.getY(), 20, 20);
	}
}
