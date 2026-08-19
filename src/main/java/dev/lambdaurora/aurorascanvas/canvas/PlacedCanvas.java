/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import net.minecraft.core.Direction;

/**
 * Represents a canvas that is placed down in the world.
 *
 * @param canvas the canvas
 * @param facing the facing direction of the canvas
 * @param depth the depth of the canvas
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record PlacedCanvas(Canvas canvas, Direction facing, float depth) implements CanvasHandler {
	public static final float DEFAULT_DEPTH = 0.928f;

	public PlacedCanvas(Canvas canvas, Direction facing) {
		this(canvas, facing, DEFAULT_DEPTH);
	}

	@Override
	public short getRawPixel(int x, int y) {
		return this.canvas.getRawPixel(x, y);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		return this.canvas.setPixel(x, y, color);
	}

	@Override
	public boolean drawBrush(int x, int y, int color) {
		return this.canvas.drawBrush(x, y, color);
	}

	@Override
	public boolean replaceColor(int x, int y, int color) {
		return this.canvas.replaceColor(x, y, color);
	}

	@Override
	public boolean drawLine(int x1, int y1, int x2, int y2, DrawModifier modifier) {
		return this.canvas.drawLine(x1, y1, x2, y2, modifier);
	}

	@Override
	public boolean fillColor(int x, int y, int color) {
		return this.canvas.fillColor(x, y, color);
	}

	@Override
	public boolean isGlowing() {
		return this.canvas.isGlowing();
	}

	@Override
	public void setGlowing(boolean glowing) {
		this.canvas.setGlowing(glowing);
	}

	@Override
	public void copy(Canvas source) {
		this.canvas.copy(source);
	}

	@Override
	public boolean isEmpty() {
		return this.canvas.isEmpty();
	}

	@Override
	public void clear() {
		this.canvas.clear();
	}
}
