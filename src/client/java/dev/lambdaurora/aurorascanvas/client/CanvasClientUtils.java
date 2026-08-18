/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CanvasClientUtils {
	/**
	 * Exports the given canvas to the given image.
	 * <p>
	 * The image must be 16x16 pixels.
	 *
	 * @param canvas the canvas to export
	 * @param image the image to export to
	 * @see #exportToImage(CanvasHandler)
	 */
	public static void exportToImage(CanvasHandler canvas, NativeImage image) {
		if (image.getWidth() != 16 || image.getHeight() != 16) {
			throw new IllegalArgumentException("Image must be 16x16.");
		}

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				image.setPixelRGBA(x, y, canvas.getColor(x, y));
			}
		}
	}

	/**
	 * Exports the given canvas to an image.
	 *
	 * @param canvas the canvas to export
	 * @return the exported image
	 * @see #exportToImage(CanvasHandler, NativeImage)
	 */
	public static NativeImage exportToImage(CanvasHandler canvas) {
		var image = new NativeImage(16, 16, false);
		exportToImage(canvas, image);
		return image;
	}

	public static void importFromImage(Canvas canvas, NativeImage image) {
		if (image.getWidth() != 16 || image.getHeight() != 16) {
			throw new IllegalArgumentException("Image must be 16x16.");
		}
	}

	private CanvasClientUtils() {
		throw new UnsupportedOperationException("CanvasClientUtils only contains static definitions.");
	}
}
