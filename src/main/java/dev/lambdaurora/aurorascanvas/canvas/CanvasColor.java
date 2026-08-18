/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import dev.lambdaurora.aurorascanvas.compat.AurorasDecoDataUpper;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Represents a canvas color.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CanvasColor extends DrawModifier {
	private static final Byte2ObjectMap<CanvasColor> COLORS = new Byte2ObjectOpenHashMap<>();

	/**
	 * Represents the absence of color.
	 */
	public static final CanvasColor EMPTY = new CanvasColor(0, 0x00000000, Items.PAPER);
	public static final CanvasColor WHITE = fromDye(1, Items.WHITE_DYE);
	public static final CanvasColor ORANGE = fromDye(2, Items.ORANGE_DYE);
	public static final CanvasColor MAGENTA = fromDye(3, Items.MAGENTA_DYE);
	public static final CanvasColor LIGHT_BLUE = fromDye(4, Items.LIGHT_BLUE_DYE);
	public static final CanvasColor YELLOW = fromDye(5, Items.YELLOW_DYE);
	public static final CanvasColor LIME = fromDye(6, Items.LIME_DYE);
	public static final CanvasColor PINK = fromDye(7, Items.PINK_DYE);
	public static final CanvasColor GRAY = fromDye(8, Items.GRAY_DYE);
	public static final CanvasColor LIGHT_GRAY = fromDye(9, Items.LIGHT_GRAY_DYE);
	public static final CanvasColor CYAN = fromDye(10, Items.CYAN_DYE);
	public static final CanvasColor PURPLE = fromDye(11, Items.PURPLE_DYE);
	public static final CanvasColor BLUE = fromDye(12, Items.BLUE_DYE);
	public static final CanvasColor BROWN = fromDye(13, Items.BROWN_DYE);
	public static final CanvasColor GREEN = fromDye(14, Items.GREEN_DYE);
	public static final CanvasColor RED = fromDye(15, Items.RED_DYE);
	public static final CanvasColor BLACK = fromDye(16, Items.BLACK_DYE);
	public static final CanvasColor SWEET_BERRIES = new CanvasColor(17, 0xffbb0000, Items.SWEET_BERRIES);
	public static final CanvasColor GLOW_BERRIES = new CanvasColor(18, 0xffff9737, Items.GLOW_BERRIES);
	public static final CanvasColor BLUEBERRIES = new CanvasColor(19, 0xff006ac6, new CompatMatcher(new Identifier("ecotones", "blueberries")));
	public static final CanvasColor LAVENDER = new CanvasColor(20, 0xffb886db, new CompatMatcher(AurorasDecoDataUpper.id("lavender")));

	private final byte id;
	private final Predicate<Item> itemMatcher;

	private CanvasColor(int id, int color, Predicate<Item> item) {
		super("", color);
		this.id = (byte) id;
		this.itemMatcher = item;

		if (COLORS.containsKey((byte) id)) {
			throw new IllegalStateException("Cannot register color twice for the same identifier.");
		}
		COLORS.put((byte) id, this);
	}

	private CanvasColor(int id, int color, Item item) {
		this(id, color, other -> other == item);
	}

	/**
	 * {@return the color instance from its identifier}
	 *
	 * @param color the color identifier
	 */
	public static CanvasColor byId(byte color) {
		return COLORS.getOrDefault(color, EMPTY);
	}

	/**
	 * Extracts the color instance out of the given raw color format.
	 *
	 * @param color the raw color format
	 * @return the extracted color instance
	 */
	public static CanvasColor fromRaw(int color) {
		return byId((byte) ((color & CanvasPixel.COLOR_MASK) >> 8));
	}

	public static @Nullable CanvasColor fromItem(Item item) {
		for (var color : COLORS.values()) {
			if (color.matchItem(item)) {
				return color;
			}
		}

		return null;
	}

	public static @UnmodifiableView Collection<CanvasColor> getColors() {
		return Collections.unmodifiableCollection(COLORS.values());
	}

	/**
	 * {@return the identifier of the color}
	 */
	public byte getId() {
		return this.id;
	}

	/**
	 * {@return the render color in the ABGR format}
	 *
	 * @param shade the shade
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 */
	public int getRenderColor(int shade, boolean saturated) {
		if (this.getId() == 0)
			return this.getColor();

		int factor = switch (shade) {
			case 1 -> 220;
			case 2 -> 180;
			case 3 -> 135;
			case 4 -> 285;
			case 5 -> 320;
			default -> 255;
		};

		int color = saturated ? this.getSaturated() : this.getColor();
		int red = Mth.clamp((color >> 16 & 255) * factor / 255, 0, 255);
		int green = Mth.clamp((color >> 8 & 255) * factor / 255, 0, 255);
		int blue = Mth.clamp((color & 255) * factor / 255, 0, 255);
		return 0xff000000 | blue << 16 | green << 8 | red;
	}

	/**
	 * {@return the render color in the ABGR format}
	 *
	 * @param color the raw color format
	 */
	public static int getRenderColor(int color) {
		return fromRaw(color).getRenderColor(CanvasPixel.getShadeFromRaw(color), CanvasPixel.getSaturationFromRaw(color));
	}

	public static int increaseDarkness(int shade) {
		return switch (shade) {
			case 0, 1, 2 -> shade + 1;
			case 4 -> 0;
			case 5 -> shade - 1;
			default -> shade;
		};
	}

	public static int decreaseDarkness(int shade) {
		return switch (shade) {
			case 1, 2, 3 -> shade - 1;
			case 0 -> 4;
			case 4 -> shade + 1;
			default -> shade;
		};
	}

	int getSaturated() {
		final int value = 1;

		int color = this.getColor();
		int red = color >> 16 & 255;
		int green = color >> 8 & 255;
		int blue = color & 255;

		float gray = 0.2989f * red + 0.5870f * green + 0.1140f * blue;

		red = Mth.clamp((int) (-gray * value + red * (1 + value)), 0, 255);
		green = Mth.clamp((int) (-gray * value + green * (1 + value)), 0, 255);
		blue = Mth.clamp((int) (-gray * value + blue * (1 + value)), 0, 255);

		return 0xff000000 | red << 16 | green << 8 | blue;
	}

	private static CanvasColor fromDye(int id, Item item) {
		if (!(item instanceof DyeItem dyeItem)) {
			throw new IllegalArgumentException("Item must be a DyeItem.");
		}

		var color = dyeItem.getDyeColor();

		if (COLORS.containsKey((byte) id)) {
			return COLORS.get((byte) id);
		}

		int red = (int) (color.getTextureDiffuseColors()[0] * 255.f);
		int green = (int) (color.getTextureDiffuseColors()[1] * 255.f);
		int blue = (int) (color.getTextureDiffuseColors()[2] * 255.f);
		return new CanvasColor((byte) id, 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
	}

	@Override
	public boolean matchItem(Item item) {
		return this.itemMatcher.test(item);
	}

	@Override
	public CanvasPixel apply(CanvasPixel pixel) {
		return pixel.withColor(this);
	}

	private record CompatMatcher(Identifier id) implements Predicate<Item> {
		@SuppressWarnings("deprecation")
		@Override
		public boolean test(Item item) {
			return item.builtInRegistryHolder().is(id);
		}
	}
}
