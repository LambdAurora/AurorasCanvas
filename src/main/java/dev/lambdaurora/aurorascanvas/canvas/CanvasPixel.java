/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Range;

/**
 * Represents a canvas pixel.
 *
 * @param color the color of the pixel
 * @param saturated {@code true} if the pixel is saturated, or {@code false} otherwise
 * @param shade the shade of the pixel
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record CanvasPixel(CanvasColor color, boolean saturated, @Range(from = 0, to = 7) int shade) {
	/**
	 * The color identifier mask ({@value}) for the raw color format.
	 */
	public static final int COLOR_MASK /**/ = 0b1111111100000000;
	/**
	 * The saturation mask ({@value}) for the raw color format.
	 */
	public static final int SATURATION_MASK = 0b0000000010000000;
	/**
	 * The shade mask ({@value}) for the raw color format.
	 */
	public static final int SHADE_MASK /**/ = 0b0000000001110000;

	public CanvasPixel(CanvasColor color) {
		this(color, false, 0);
	}

	/**
	 * Extracts the pixel instance out of the given raw pixel format.
	 *
	 * @param color the raw pixel format
	 * @return the extracted pixel instance
	 */
	public static CanvasPixel fromRaw(int color) {
		return new CanvasPixel(
				CanvasColor.fromRaw(color),
				getSaturationFromRaw(color),
				getShadeFromRaw(color)
		);
	}

	public CanvasPixel withColor(CanvasColor color) {
		if (this.color == color) return this;
		return new CanvasPixel(color, this.saturated, this.shade);
	}

	public CanvasPixel withSaturation(boolean saturation) {
		if (this.color == CanvasColor.EMPTY || this.saturated == saturation) return this;
		return new CanvasPixel(this.color, saturation, this.shade);
	}

	public CanvasPixel withShade(@Range(from = 0, to = 7) int shade) {
		if (this.color == CanvasColor.EMPTY || this.shade == shade) return this;
		return new CanvasPixel(this.color, this.saturated, shade);
	}

	public static boolean getSaturationFromRaw(int color) {
		return (color & SATURATION_MASK) != 0;
	}

	public static int getShadeFromRaw(int color) {
		return (color & SHADE_MASK) >> 4;
	}

	/**
	 * Returns the raw pixel format of this pixel.
	 *
	 * @return the raw pixel format
	 */
	public short toRawId() {
		if (this.color == CanvasColor.EMPTY) return 0;

		short id = (short) (this.color.getId() << 8);
		id |= (short) (Mth.clamp(this.shade, 0, 7) << 4);
		if (this.saturated) id |= SATURATION_MASK;
		return id;
	}

	/**
	 * {@return the render color in the ABGR format}
	 */
	public int getRenderColor() {
		if (this.color.getId() == 0)
			return this.color.getColor();

		int factor = switch (this.shade) {
			case 1 -> 220;
			case 2 -> 180;
			case 3 -> 135;
			case 4 -> 285;
			case 5 -> 320;
			default -> 255;
		};

		int color = this.saturated ? this.color.getSaturated() : this.color.getColor();
		int red = Mth.clamp((color >> 16 & 255) * factor / 255, 0, 255);
		int green = Mth.clamp((color >> 8 & 255) * factor / 255, 0, 255);
		int blue = Mth.clamp((color & 255) * factor / 255, 0, 255);
		return 0xff000000 | blue << 16 | green << 8 | red;
	}
}
