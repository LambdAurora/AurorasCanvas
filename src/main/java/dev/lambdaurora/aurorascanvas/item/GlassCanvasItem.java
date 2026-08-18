/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Represents a glass canvas item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class GlassCanvasItem extends CanvasItem {
	public GlassCanvasItem(GlassCanvasBlock canvasBlock, Properties settings) {
		super(canvasBlock, settings);
	}

	@Override
	protected boolean clearContents(ItemStack self) {
		var nbt = Utils.getOrCreateBlockEntityNbt(self, AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE);

		if (nbt.contains("pixels")) {
			return super.clearContents(self);
		}

		var front = Canvas.fromNbt(nbt.getCompound("front"));
		var back = Canvas.fromNbt(nbt.getCompound("back"));

		if (front.isEmpty() && back.isEmpty()) {
			return false;
		}

		front.clear();
		back.clear();
		nbt.put("front", front.toNbt());
		nbt.put("back", back.toNbt());

		return true;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		return super.getTooltipImage(stack).or(() -> {
			var nbt = BlockItem.getBlockEntityData(stack);
			if (nbt != null && (nbt.contains("front", Tag.TAG_COMPOUND) || nbt.contains("back", Tag.TAG_COMPOUND))) {
				var frontCanvas = Canvas.fromNbt(nbt.getCompound("front"));
				var backCanvas = Canvas.fromNbt(nbt.getCompound("back"));

				return Optional.of(new CanvasTooltipData(
						BuiltInRegistries.ITEM.getKey(this).getPath().replace("waxed_", ""),
						List.of(frontCanvas, backCanvas), this.locked
				));
			}

			return Optional.empty();
		});
	}
}
