/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.Arrays;

/**
 * Represents a canvas drawing.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Canvas implements CanvasHandler {
	private final short[] pixels;
	private boolean glowing;

	public Canvas() {
		this(new short[PIXELS_COUNT]);
	}

	public Canvas(short[] pixels) {
		this(pixels, false);
	}

	public Canvas(short[] pixels, boolean glowing) {
		CanvasHandler.checkPixels(pixels);
		this.pixels = pixels;
		this.glowing = glowing;
	}

	/**
	 * Gets the pixels of this canvas.
	 *
	 * @return the pixels
	 */
	public short[] getPixels() {
		return this.pixels;
	}

	@Override
	public short getRawPixel(int x, int y) {
		return this.pixels[y * 16 + x];
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		if ((color & CanvasPixel.COLOR_MASK) == 0) color = 0; // There's no color, make sure to erase any extra metadata.

		short id = (short) color;
		if (this.pixels[y * 16 + x] != id) {
			this.pixels[y * 16 + x] = id;
			return true;
		}
		return false;
	}

	@Override
	public boolean drawBrush(int x, int y, DrawModifier modifier) {
		x = x - 1;
		y = y - 1;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (x < 16 && x >= 0 && y < 16 && y >= 0) {
					this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y)).toRawId();
				}
				x++;
			}
			x = x - 3;
			y++;
		}
		return true;
	}

	@Override
	public boolean replaceColor(int x, int y, int color) {
		if ((color & CanvasPixel.COLOR_MASK) == 0) color = 0; // There's no color, make sure to erase any extra metadata.

		short id = this.getRawPixel(x, y);
		short repl = (short) color;

		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == id) {
				this.pixels[i] = repl;
			}
		}
		return true;
	}

	@Override
	public boolean drawLine(int x1, int y1, int x2, int y2, DrawModifier modifier) {
		int d = 0;

		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);

		int dx2 = 2 * dx;
		int dy2 = 2 * dy;

		int ix = x1 < x2 ? 1 : -1; // increment direction
		int iy = y1 < y2 ? 1 : -1;

		int x = x1;
		int y = y1;

		if (dx >= dy) {
			for (; ; ) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y)).toRawId();
				if (x == x2)
					break;
				x += ix;
				d += dy2;
				if (d > dx) {
					y += iy;
					d -= dx2;
				}
			}
		} else {
			for (; ; ) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y)).toRawId();
				if (y == y2)
					break;
				y += iy;
				d += dx2;
				if (d > dy) {
					x += ix;
					d -= dy2;
				}
			}
		}
		return true;
	}

	@Override
	public boolean fillColor(int x, int y, int color) {
		if ((color & CanvasPixel.COLOR_MASK) == 0) color = 0; // There's no color, make sure to erase any extra metadata.

		int replacement = (short) color;
		int target = this.getRawPixel(x, y);
		if (target != replacement) {
			this.flood(x, y, target, replacement);
		}
		return true;
	}

	private void flood(int x, int y, int target, int replacement) {
		short pixel = this.getRawPixel(x, y);
		if (pixel == target) {
			this.pixels[y * 16 + x] = (short) replacement;
			this.flood((x <= 0 ? x : x - 1), y, target, replacement);
			this.flood((x >= 15 ? x : x + 1), y, target, replacement);
			this.flood(x, (y <= 0 ? y : y - 1), target, replacement);
			this.flood(x, (y >= 15 ? y : y + 1), target, replacement);
		}
	}

	@Override
	public void copy(CanvasHandler source) {
		if (source instanceof Canvas sourceCanvas) {
			System.arraycopy(sourceCanvas.pixels, 0, this.pixels, 0, this.pixels.length);
		} else {
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					this.setPixel(x, y, source.getRawPixel(x, y));
				}
			}
		}

		this.setGlowing(source.isGlowing());
	}

	/**
	 * Clears the canvas.
	 */
	@Override
	public void clear() {
		Arrays.fill(this.pixels, (short) 0);
	}

	/**
	 * Returns whether this canvas is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	@Override
	public boolean isEmpty() {
		for (short b : this.pixels) {
			if (b != 0)
				return false;
		}
		return true;
	}

	@Override
	public boolean isGlowing() {
		return this.glowing;
	}

	@Override
	public void setGlowing(boolean glowing) {
		this.glowing = glowing;
	}

	/* Serialization */

	public CompoundTag toNbt() {
		var encoded = CanvasSerialization.CANVAS_CODEC.encodeStart(NbtOps.INSTANCE, this)
				.getOrThrow();

		if (!(encoded instanceof CompoundTag encodedNbt))
			throw new IllegalStateException("Canvas codec did not encode into a NBT compound.");

		return encodedNbt;
	}

	public CompoundTag writeNbt(CompoundTag nbt) {
		var encodedNbt = this.toNbt();

		nbt.merge(encodedNbt);

		return nbt;
	}

	public static Canvas fromNbt(CompoundTag nbt) {
		return CanvasSerialization.CANVAS_CODEC.parse(NbtOps.INSTANCE, nbt)
				.result().orElseGet(Canvas::new);
	}
}
