/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a canvas color.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardColor extends DrawModifier {
	private static final Byte2ObjectMap<BlackboardColor> COLORS = new Byte2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Item, BlackboardColor> ITEM_TO_COLOR = new Object2ObjectOpenHashMap<>();

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

	/**
	 * Represents the absence of color.
	 */
	public static final BlackboardColor EMPTY = new BlackboardColor((byte) 0, 0x00000000, Items.PAPER);
	public static final byte FREE_COLOR_SPACE = (byte) (DyeColor.values().length + 1);
	public static final BlackboardColor SWEET_BERRIES = new BlackboardColor(FREE_COLOR_SPACE, 0xffbb0000, Items.SWEET_BERRIES);
	//public static final BlackboardColor GLOW_BERRIES = new BlackboardColor(FREE_COLOR_SPACE + 1, 0xffff9737, Items.GLOW_BERRIES);
	//public static final BlackboardColor LAVENDER = new BlackboardColor(FREE_COLOR_SPACE + 3, 0xffb886db, AurorasDecoPlants.LAVENDER.item());

	public static final int BLUEBERRIES_COLOR = 0xff006ac6;

	private final byte id;
	private final Item item;

	private BlackboardColor(byte id, int color, Item item) {
		super("", color);
		this.id = (byte) id;
		this.item = item;

		COLORS.put(id, this);
		ITEM_TO_COLOR.put(item, this);
	}

	/**
	 * {@return the color instance from its identifier}
	 *
	 * @param color the color identifier
	 */
	public static BlackboardColor byId(byte color) {
		return COLORS.getOrDefault(color, EMPTY);
	}

	/**
	 * Extracts the color instance out of the given raw color format.
	 *
	 * @param color the raw color format
	 * @return the extracted color instance
	 */
	public static BlackboardColor fromRaw(int color) {
		return byId((byte) ((color & COLOR_MASK) >> 8));
	}

	public static @Nullable BlackboardColor fromItem(Item item) {
		return ITEM_TO_COLOR.get(item);
	}

	public static @UnmodifiableView Collection<BlackboardColor> getColors() {
		return Collections.unmodifiableCollection(COLORS.values());
	}

	/**
	 * {@return the identifier of the color}
	 */
	public byte getId() {
		return this.id;
	}

	/**
	 * Returns the raw color format with shading and saturation of this color.
	 *
	 * @param shade the shade
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return the raw color format
	 */
	public short toRawId(int shade, boolean saturated) {
		if (this == EMPTY) return 0;

		short id = (short) (this.getId() << 8);
		id |= Mth.clamp(shade, 0, 7) << 4;
		if (saturated) id |= SATURATION_MASK;
		return id;
	}

	public Item getItem() {
		return this.item;
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
		return fromRaw(color).getRenderColor(getShadeFromRaw(color), getSaturationFromRaw(color));
	}

	public static int getShadeFromRaw(int color) {
		return (color & SHADE_MASK) >> 4;
	}

	public static boolean getSaturationFromRaw(int color) {
		return (color & SATURATION_MASK) != 0;
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

	private int getSaturated() {
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

	private static BlackboardColor fromDye(DyeItem dyeItem) {
		var color = dyeItem.getDyeColor();

		if (COLORS.containsKey(color.getId() + 1)) {
			return COLORS.get(color.getId() + 1);
		}

		int red = (int) (color.getTextureDiffuseColors()[0] * 255.f);
		int green = (int) (color.getTextureDiffuseColors()[1] * 255.f);
		int blue = (int) (color.getTextureDiffuseColors()[2] * 255.f);
		return new BlackboardColor((byte) (color.getId() + 1), 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
	}

	public static void tryRegisterColorFromItem(Identifier id, Item item) {
		if (item instanceof DyeItem dyeItem) {
			fromDye(dyeItem);
		} else if (id.getNamespace().equals("ecotones") && id.getPath().equals("blueberries")) {
			new BlackboardColor((byte) (FREE_COLOR_SPACE + 2), BLUEBERRIES_COLOR, item);
		}
	}

	@Override
	public boolean matchItem(Item item) {
		return this.item == item;
	}

	@Override
	public short apply(short colorData) {
		return this.toRawId(0, false);
	}
}
