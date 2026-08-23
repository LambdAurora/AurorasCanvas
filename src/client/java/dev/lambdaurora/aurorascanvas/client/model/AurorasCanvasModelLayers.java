/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.client.model.geom.ModelLayerLocation;

/**
 * Contains the model layers of Aurora's Canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasCanvasModelLayers {
	public static final ModelLayerLocation EASEL = new ModelLayerLocation(AurorasCanvas.id("easel"), "main");

	private AurorasCanvasModelLayers() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions");
	}
}
