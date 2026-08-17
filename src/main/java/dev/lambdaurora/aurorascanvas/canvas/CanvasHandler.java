/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

/**
 * Represents a canvas handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CanvasHandler {
	short getPixel(int x, int y);

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the raw color
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	boolean setPixel(int x, int y, int color);

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the color
	 * @param shade the shade of the color
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 * @see #setPixel(int, int, int)
	 */
	default boolean setPixel(int x, int y, CanvasColor color, int shade, boolean saturated) {
		return this.setPixel(x, y, color.toRawId(shade, saturated));
	}

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the color
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	default boolean setPixel(int x, int y, CanvasColor color) {
		return this.setPixel(x, y, color, 0, false);
	}

	/**
	 * Sets whether the given pixel is saturated or not.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 * @see #setPixel(int, int, int)
	 * @see #setPixel(int, int, CanvasColor, int, boolean)
	 */
	default boolean setSaturated(int x, int y, boolean saturated) {
		int color = this.getPixel(x, y);
		if (CanvasColor.getSaturationFromRaw(color) == saturated) return false;

		color &= ~CanvasColor.SATURATION_MASK;
		if (saturated)
			color |= CanvasColor.SATURATION_MASK;

		this.setPixel(x, y, color);

		return true;
	}

	boolean brush(int x, int y, int color);

	default boolean brush(int x, int y, CanvasColor color, int shade) {
		return this.brush(x, y, color.toRawId(shade, CanvasColor.getSaturationFromRaw(this.getPixel(x, y))));
	}

	boolean replace(int x, int y, int color);

	default boolean replace(int x, int y, CanvasColor color, int shade) {
		return this.replace(x, y, color.getRenderColor(shade, false));
	}

	boolean line(int x1, int y1, int x2, int y2, DrawModifier modifier);

	boolean fill(int x, int y, int color);

	default boolean fill(int x, int y, CanvasColor color, int shade) {
		return this.fill(x, y, color.toRawId(shade, CanvasColor.getSaturationFromRaw(this.getPixel(x, y))));
	}
}
