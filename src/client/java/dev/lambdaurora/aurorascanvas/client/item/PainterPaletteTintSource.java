/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

/// Represents a painter's palette tint source.
///
/// @param slot the slot to tint
/// @param defaultColor the default color of the tint
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.2.0
public record PainterPaletteTintSource(Slot slot, int defaultColor) implements ItemTintSource {
	public static final int DEFAULT_BACKGROUND_COLOR = 0xff967441;
	public static final MapCodec<PainterPaletteTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Slot.CODEC.fieldOf("slot").forGetter(PainterPaletteTintSource::slot),
					ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(PainterPaletteTintSource::defaultColor)
			).apply(instance, PainterPaletteTintSource::new)
	);

	@Override
	public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
		var inventory = itemStack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);

		DrawModifier primaryColor = null;
		DrawModifier previousColor = null;
		DrawModifier nextColor = null;

		if (inventory != null) {
			primaryColor = DrawModifier.fromItem(inventory.getSelectedColor());
			previousColor = inventory.getPreviousColor();
			nextColor = inventory.getNextColor();
		}

		return switch (this.slot) {
			case Slot.CURRENT -> primaryColor == null ? this.defaultColor : primaryColor.getColor();
			case Slot.BRUSH -> primaryColor == null ? this.defaultColor : primaryColor.getColor();
			case Slot.PREVIOUS -> previousColor == null ? this.defaultColor : previousColor.getColor();
			case Slot.NEXT -> nextColor == null ? this.defaultColor : nextColor.getColor();
		};
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return MAP_CODEC;
	}

	public enum Slot {
		PREVIOUS,
		BRUSH,
		CURRENT,
		NEXT;

		private static final Map<String, Slot> BY_ID = Stream.of(values()).map(slot -> Map.entry(slot.id, slot)).collect(Util.toMap());
		public static final Codec<Slot> CODEC = Codec.STRING.flatXmap(
				id -> {
					var value = BY_ID.get(id);
					if (value == null) return DataResult.error(() -> "Unknown slot type \"" + id + "\".");
					else return DataResult.success(value);
				},
				slot -> DataResult.success(slot.id)
		);

		private final String id;

		Slot() {
			this.id = this.name().toLowerCase();
		}
	}
}
