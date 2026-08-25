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
 * Represents a source handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CanvasHandler {
	/**
	 * The amount of pixels in a canvas, which is {@value}.
	 */
	int PIXELS_COUNT = 16 * 16;

	/**
	 * Gets the pixel data identifier at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the raw pixel
	 */
	short getRawPixel(int x, int y);

	/**
	 * Gets the pixel at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the pixel
	 */
	default CanvasPixel getPixel(int x, int y) {
		return CanvasPixel.fromRaw(this.getRawPixel(x, y));
	}

	default int getColor(int x, int y) {
		int id = this.getRawPixel(x, y);
		return CanvasColor.getRenderColor(id);
	}

	/**
	 * Sets the pixel data at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the raw pixel
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	boolean setPixel(int x, int y, int color);

	/**
	 * Sets the pixel data at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param pixel the pixel data
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	default boolean setPixel(int x, int y, CanvasPixel pixel) {
		return this.setPixel(x, y, pixel.toRawId());
	}

	boolean drawBrush(int x, int y, DrawModifier modifier);

	boolean replaceColor(int x, int y, int color);

	default boolean replaceColor(int x, int y, CanvasPixel pixel) {
		return this.replaceColor(x, y, pixel.toRawId());
	}

	boolean drawLine(int x1, int y1, int x2, int y2, DrawModifier modifier);

	boolean fillColor(int x, int y, int color);

	default boolean fillColor(int x, int y, CanvasPixel pixel) {
		return this.fillColor(x, y, pixel.toRawId());
	}

	/**
	 * {@return {@code true} if this canvas is glowing, or {@code false} otherwise}
	 */
	boolean isGlowing();

	/**
	 * Sets whether this canvas is glowing or not.
	 *
	 * @param glowing {@code true} if this canvas is glowing, or {@code false} otherwise
	 */
	void setGlowing(boolean glowing);

	/**
	 * Copies the canvas data to this canvas.
	 *
	 * @param source the canvas to copy
	 */
	default void copy(CanvasHandler source) {
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				this.setPixel(x, y, source.getRawPixel(x, y));
			}
		}
		this.setGlowing(source.isGlowing());
	}

	boolean isEmpty();

	void clear();

	/**
	 * Checks that the given pixel array is safe to operate on.
	 *
	 * @param pixels the pixels
	 */
	static void checkPixels(short[] pixels) {
		if (pixels.length != PIXELS_COUNT) {
			throw new IllegalArgumentException("Canvas pixels must have a length of " + PIXELS_COUNT + ".");
		}
	}
}
