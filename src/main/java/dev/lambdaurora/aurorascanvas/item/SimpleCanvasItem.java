/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;

/**
 * Represents a canvases item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleCanvasItem extends CanvasItem<SimpleCanvasHolder> {
	public SimpleCanvasItem(CanvasBlock canvasBlock, Properties settings) {
		super(canvasBlock, settings);
	}

	@Override
	public CanvasHolder.Type<SimpleCanvasHolder> canvasType() {
		return SimpleCanvasHolder.TYPE;
	}
}
