/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TrackedCanvasHandler implements CanvasHandler {
	private final CanvasHistory history;

	TrackedCanvasHandler(CanvasHandler parent) {
		this.history = new CanvasHistory(parent);
	}

	public CanvasHistory history() {
		return this.history;
	}

	@Override
	public short getRawPixel(int x, int y) {
		return this.history.effectiveCanvas().getRawPixel(x, y);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		return this.history.push(canvas -> canvas.setPixel(x, y, color));
	}

	@Override
	public boolean drawBrush(int x, int y, DrawModifier modifier) {
		return this.history.push(canvas -> canvas.drawBrush(x, y, modifier));
	}

	@Override
	public boolean replaceColor(int x, int y, int color) {
		return this.history.push(canvas -> canvas.replaceColor(x, y, color));
	}

	@Override
	public boolean drawLine(int x1, int y1, int x2, int y2, DrawModifier modifier) {
		return this.history.push(canvas -> canvas.drawLine(x1, y1, x2, y2, modifier));
	}

	@Override
	public boolean fillColor(int x, int y, int color) {
		return this.history.push(canvas -> canvas.fillColor(x, y, color));
	}

	@Override
	public boolean isGlowing() {
		return this.history.effectiveCanvas().isGlowing();
	}

	@Override
	public void setGlowing(boolean glowing) {
	}

	@Override
	public void copy(CanvasHandler source) {
	}

	@Override
	public boolean isEmpty() {
		return this.history.effectiveCanvas().isEmpty();
	}

	@Override
	public void clear() {
		this.history.push(CanvasHandler::clear);
	}
}
