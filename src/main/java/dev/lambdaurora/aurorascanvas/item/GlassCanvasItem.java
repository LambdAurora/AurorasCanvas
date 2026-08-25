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
import org.jetbrains.annotations.Unmodifiable;

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
	public @Unmodifiable List<IndexedCanvas.Provider> canvasProviders() {
		return List.of(IndexedCanvas.FRONT, IndexedCanvas.BACK);
	}
}
