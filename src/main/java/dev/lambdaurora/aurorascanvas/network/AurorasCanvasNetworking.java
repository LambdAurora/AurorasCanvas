/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.network;

import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Contains the different packet definitions used in Aurora's Canvas.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class AurorasCanvasNetworking {
	public static void handleCanvasSubmitEdit(
			CanvasEditSubmitPayload payload, ServerPlayNetworking.Context context
	) {
		var player = context.player();

		context.server().execute(() -> {
			var entity = player.level().getEntity(payload.easelEntityId());

			if (entity instanceof EaselEntity easel) {
				easel.submit(player, payload.canvas());
			}
		});
	}

	public static void handlePainterPaletteScroll(
			PainterPaletteScrollPayload payload, ServerPlayNetworking.Context context
	) {
		var player = context.player();

		context.server().execute(() -> {
			var mainHandStack = player.getMainHandItem();

			if (mainHandStack.getItem() instanceof PainterPaletteItem paletteItem) {
				paletteItem.onScroll(player, mainHandStack, payload.scrollDelta(), payload.toolModifier());
			}
		});
	}

	private AurorasCanvasNetworking() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
