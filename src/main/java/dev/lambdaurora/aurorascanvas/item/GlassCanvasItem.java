/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
	public List<IndexedCanvas> getCanvases(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && (
				nbt.contains("front", Tag.TAG_COMPOUND)
						|| nbt.contains("back", Tag.TAG_COMPOUND)
						|| nbt.contains("pixels", Tag.TAG_COMPOUND)
		)) {
			var frontCanvas = IndexedCanvas.FRONT.reader().fromNbt(nbt);
			var backCanvas = IndexedCanvas.BACK.reader().fromNbt(nbt);

			return List.of(frontCanvas, backCanvas);
		}

		return List.of();
	}
}
