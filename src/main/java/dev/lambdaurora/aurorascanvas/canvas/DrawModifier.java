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
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a draw modifier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class DrawModifier {
	private static final List<DrawModifier> MODIFIERS = new ArrayList<>();

	public static final DrawModifier SHADE_INCREASE = new DrawModifier(AurorasCanvas.NAMESPACE + ".modifier.darken", 0xff444444) {
		@Override
		@SuppressWarnings("deprecation")
		public boolean matchItem(Item item) {
			return item.builtInRegistryHolder().is(ItemTags.COALS);
		}

		@Override
		public CanvasPixel apply(CanvasPixel pixel) {
			if (pixel.color() == CanvasColor.EMPTY) return pixel;

			int newShade = CanvasColor.increaseDarkness(pixel.shade());
			return pixel.withShade(newShade);
		}
	};

	public static final DrawModifier SHADE_DECREASE = new DrawModifier(AurorasCanvas.NAMESPACE + ".modifier.lighten", 0xffeeeeee) {
		@Override
		public boolean matchItem(Item item) {
			return item == Items.BONE_MEAL;
		}

		@Override
		public CanvasPixel apply(CanvasPixel pixel) {
			if (pixel.color() == CanvasColor.EMPTY) return pixel;

			int newShade = CanvasColor.decreaseDarkness(pixel.shade());
			return pixel.withShade(newShade);
		}
	};

	public static final DrawModifier SATURATION = new DrawModifier(AurorasCanvas.NAMESPACE + ".modifier.saturation", 0xffffbc5e) {
		@Override
		public boolean matchItem(Item item) {
			return item == Items.GLOWSTONE_DUST;
		}

		@Override
		public CanvasPixel apply(CanvasPixel pixel) {
			return pixel.withSaturation(!pixel.saturated());
		}
	};

	private final String translationKey;
	private final int color;

	protected DrawModifier(String translationKey, int color) {
		this.translationKey = translationKey;
		this.color = color;
		MODIFIERS.add(this);
	}

	public Component getName() {
		return Component.translatable(this.translationKey);
	}

	/**
	 * {@return the color in the ARGB format}
	 */
	public int getColor() {
		return this.color;
	}

	public abstract boolean matchItem(Item item);

	public abstract CanvasPixel apply(CanvasPixel pixel);

	public static @Nullable DrawModifier fromItem(Item item) {
		for (var modifier : MODIFIERS) {
			if (modifier.matchItem(item))
				return modifier;
		}

		return null;
	}

	public static @Nullable DrawModifier fromItem(ItemStack item) {
		return fromItem(item.getItem());
	}
}
