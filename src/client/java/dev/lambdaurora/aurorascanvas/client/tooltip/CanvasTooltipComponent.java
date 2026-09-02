/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.tooltip;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasTexture;
import dev.lambdaurora.aurorascanvas.client.screen.SpriteIds;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents the blackboard tooltip component. Displays the blackboard's contents.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class CanvasTooltipComponent implements ClientTooltipComponent {
	private static final Identifier GLOWING_SPRITE = AurorasCanvas.id("glowing");

	private final Identifier background;
	private final List<Entry> canvases;
	private final boolean locked;

	public CanvasTooltipComponent(String background, @Unmodifiable List<Canvas> canvases, boolean locked) {
		this.background = AurorasCanvas.id("textures/block/canvas/" + background + ".png");
		this.canvases = canvases.stream().map(canvas -> new Entry(CanvasTexture.fromCanvas(canvas), canvas.isGlowing())).toList();
		this.locked = locked;
	}

	public CanvasTooltipComponent(CanvasTooltipData data) {
		this(data.background(), data.canvases(), data.locked());
	}

	@Override
	public int getHeight(final Font font) {
		return 128 + 2;
	}

	@Override
	public int getWidth(Font font) {
		return 128;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, x, y, 128, 128, 0, 0, 16, 16, 16, 16);

		var entry = this.selectEntry();
		entry.texture.extract(graphics, x, y, 128, 128);

		if (this.locked) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SpriteIds.LOCKED_SPRITE, x + 112, y + 112, 16, 16);
		}

		if (entry.glowing) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GLOWING_SPRITE, x, y, 128, 128);
		}
	}

	private Entry selectEntry() {
		if (this.canvases.size() == 1) {
			return this.canvases.get(0);
		}

		int period = 5;
		int fullCycle = this.canvases.size() * period;

		int index = Math.toIntExact((System.currentTimeMillis() / 1000) % fullCycle / period);
		return this.canvases.get(index);
	}

	private record Entry(CanvasTexture texture, boolean glowing) {}
}
