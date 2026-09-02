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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
class BaseToolButton extends Button {
	private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted"));

	protected BaseToolButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
		super(x, y, width, height, message, onPress, createNarration);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractBackground(graphics);
	}

	protected void extractBackground(GuiGraphicsExtractor graphics) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}
}
