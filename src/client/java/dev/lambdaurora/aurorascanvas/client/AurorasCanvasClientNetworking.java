/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.client.screen.canvas.CanvasController;
import dev.lambdaurora.aurorascanvas.client.screen.canvas.CanvasScreen;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.network.CanvasOpenGuiPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClientNetworking {
	static void handleCanvasOpenGui(CanvasOpenGuiPayload payload, ClientPlayNetworking.Context context) {
		if (!(payload.canvas().getItem() instanceof CanvasItem item)) {
			return;
		}

		var painterPalette = payload.painterPalette().get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
		if (painterPalette == null) return;

		var client = context.client();
		var player = context.player();

		client.execute(() -> {
			client.setScreen(new CanvasScreen(payload.title(), new CanvasController(
					payload.easelEntityId(),
					player.level(),
					item,
					painterPalette,
					item.getCanvases(payload.canvas(), true).getFirst()
			)));
		});
	}

	static void init() {
		ClientPlayNetworking.registerGlobalReceiver(CanvasOpenGuiPayload.TYPE, AurorasCanvasClientNetworking::handleCanvasOpenGui);
	}

	private AurorasCanvasClientNetworking() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
