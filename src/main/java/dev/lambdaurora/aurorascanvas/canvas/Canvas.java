/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a canvas drawing.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Canvas implements CanvasHandler {
	private final short[] pixels = new short[256];
	private boolean lit;

	public Canvas() {}

	/**
	 * Gets the pixels of this canvas.
	 *
	 * @return the pixels
	 */
	public short[] getPixels() {
		return this.pixels;
	}

	@Override
	public short getPixel(int x, int y) {
		return this.pixels[y * 16 + x];
	}

	public int getColor(int x, int y) {
		int id = this.getPixel(x, y);
		return BlackboardColor.getRenderColor(id);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		if ((color & BlackboardColor.COLOR_MASK) == 0) color = 0; // There's no color, make sure to erase any extra metadata.

		short id = (short) color;
		if (this.pixels[y * 16 + x] != id) {
			this.pixels[y * 16 + x] = id;
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, int color) {
		short id = (short) color;

		x = x - 1;
		y = y - 1;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (x < 16 && x >= 0 && y < 16 && y >= 0)
					this.pixels[y * 16 + x] = id;
				x++;
			}
			x = x - 3;
			y++;
		}
		return true;
	}

	@Override
	public boolean replace(int x, int y, int color) {
		short id = this.getPixel(x, y);
		short repl = (short) color;

		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == id) {
				this.pixels[i] = repl;
			}
		}
		return true;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, DrawModifier modifier) {
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
			for (;;) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y));
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
			for (;;) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y));
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
	public boolean fill(int x, int y, int color) {
		int replacement = (short) color;
		int target = this.getPixel(x, y);
		if (target != replacement) {
			this.flood(x, y, target, replacement);
		}
		return true;
	}

	private void flood(int x, int y, int target, int replacement) {
		short pixel = this.getPixel(x, y);
		if (pixel == target) {
			this.pixels[y * 16 + x] = (short) replacement;
			this.flood((x <= 0 ? x : x - 1), y, target, replacement);
			this.flood((x >= 15 ? x : x + 1), y, target, replacement);
			this.flood(x, (y <= 0 ? y : y - 1), target, replacement);
			this.flood(x, (y >= 15 ? y : y + 1), target, replacement);
		}
	}

	/**
	 * Copies the canvas data to this canvas.
	 *
	 * @param source the canvas to copy
	 */
	public void copy(Canvas source) {
		System.arraycopy(source.pixels, 0, this.pixels, 0, this.pixels.length);
		this.setLit(source.isLit());
	}

	/**
	 * Clears the canvas.
	 */
	public void clear() {
		Arrays.fill(this.pixels, (short) 0);
	}

	/**
	 * Returns whether this canvas is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	public boolean isEmpty() {
		for (short b : this.pixels) {
			if (b != 0)
				return false;
		}
		return true;
	}

	public boolean isLit() {
		return this.lit;
	}

	public void setLit(boolean lit) {
		this.lit = lit;
	}

	/* Serialization */

	public void readNbt(CompoundTag nbt) {
		byte[] pixels = nbt.getByteArray("pixels");

		if (!nbt.contains("version", Tag.TAG_INT)) {
			convert01(pixels);
		} else {
			switch (nbt.getInt("version")) {
				case 1 -> pixels = convert02(pixels);
				default -> {
				}
			}
		}

		int boardIndex = 0;
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == 0) {
				this.pixels[boardIndex] = 0;
			} else {
				this.pixels[boardIndex] = (short) (pixels[i] << 8 | pixels[++i] & 0xff);
			}

			boardIndex++;
			if (boardIndex >= this.pixels.length) break;
		}

		this.lit = nbt.getBoolean("lit");
	}

	public CompoundTag writeNbt(CompoundTag nbt) {
		if (!this.isEmpty()) {
			int length = 0;
			for (short pixel : this.pixels) {
				if (pixel == 0) length++;
				else length += 2;
			}

			var pixels = new byte[length];

			int rawIndex = 0;
			for (short pixel : this.pixels) {
				if (pixel == 0) {
					pixels[rawIndex++] = 0;
				} else {
					pixels[rawIndex] = (byte) (pixel >> 8);
					pixels[rawIndex + 1] = (byte) (pixel & 0xff);
					rawIndex += 2;
				}
			}

			nbt.putInt("version", 2);
			nbt.putByteArray("pixels", pixels);
			nbt.putBoolean("lit", this.isLit());
		} else if (this.isLit()) {
			nbt.putBoolean("lit", true);
		}

		return nbt;
	}

	public static Canvas fromNbt(CompoundTag nbt) {
		var blackboard = new Canvas();
		blackboard.readNbt(nbt);
		return blackboard;
	}

	public static boolean shouldConvert(CompoundTag nbt) {
		return !nbt.contains("version", Tag.TAG_INT);
	}

	/**
	 * Converts the raw pixel data from version 0 to version 1.
	 *
	 * @param pixels the raw pixel data
	 */
	private static void convert01(byte[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] *= 4;
		}
	}

	/**
	 * Converts the raw pixel data from version 1 to version 2.
	 *
	 * @param pixels the raw pixel data
	 * @return the converted raw pixel data
	 */
	private static byte[] convert02(byte[] pixels) {
		var converted = new byte[256 * 2];

		int newIndex = 0;
		for (byte pixel : pixels) {
			if (pixel == 0) {
				converted[newIndex] = 0;
				newIndex++;
			} else {
				converted[newIndex] = (byte) (pixel / 4);
				converted[newIndex + 1] = (byte) ((pixel & 3) << 4);
				newIndex += 2;
			}
		}

		return converted;
	}

	public enum DrawAction {
		DEFAULT(AurorasCanvas.NAMESPACE + ".tool.pixel") {
			@Override
			public @Nullable Item getOffHandTool(FeatureFlagSet enabledFeatures) {
				return null;
			}

			@Override
			public boolean execute(CanvasHandler blackboard, int x, int y, DrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.setPixel(x, y, modifier.apply(colorData));
			}
		},
		BRUSH(AurorasCanvas.NAMESPACE + ".tool.brush") {
			@Override
			public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
				return Items.BRUSH;
			}

			@Override
			public boolean execute(CanvasHandler blackboard, int x, int y, DrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.brush(x, y, modifier.apply(colorData));
			}
		},
		FILL(AurorasCanvas.NAMESPACE + ".tool.fill") {
			@Override
			public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
				return Items.BUCKET;
			}

			@Override
			public boolean execute(CanvasHandler blackboard, int x, int y, DrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.fill(x, y, modifier.apply(colorData));
			}
		},
		REPLACE(AurorasCanvas.NAMESPACE + ".tool.replace") {
			@Override
			public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
				return Items.ENDER_PEARL;
			}

			@Override
			public boolean execute(CanvasHandler blackboard, int x, int y, DrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.replace(x, y, modifier.apply(colorData));
			}
		};

		public static final List<DrawAction> ACTIONS = List.of(values());

		private final String translationKey;

		DrawAction(@NotNull String translationKey) {
			this.translationKey = translationKey;
		}

		public Component getName() {
			return Component.translatable(this.translationKey);
		}

		public abstract @Nullable Item getOffHandTool(FeatureFlagSet enabledFeatures);

		public abstract boolean execute(CanvasHandler blackboard, int x, int y, DrawModifier modifier);
	}
}
