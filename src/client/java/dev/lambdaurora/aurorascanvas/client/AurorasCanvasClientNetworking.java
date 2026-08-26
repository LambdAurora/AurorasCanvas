/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import dev.lambdaurora.aurorascanvas.client.screen.canvas.CanvasController;
import dev.lambdaurora.aurorascanvas.client.screen.canvas.CanvasScreen;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.network.AurorasCanvasNetworking;
import dev.lambdaurora.aurorascanvas.network.CanvasOpenGuiPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClientNetworking {
	static void handleCanvasOpenGui(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		var payload = new CanvasOpenGuiPayload(buf);

		if (!(payload.canvas().getItem() instanceof CanvasItem<?> item)
				|| !(payload.painterPalette().getItem() instanceof PainterPaletteItem painterPaletteItem)) {
			return;
		}

		var painterPalette = painterPaletteItem.getInventory(payload.painterPalette());

		client.execute(() -> {
			client.setScreen(new CanvasScreen(payload.title(), new CanvasController(
					payload.easelEntityId(),
					client.level,
					item,
					painterPalette,
					item.getCanvases(payload.canvas())
			)));
		});
	}

	static void init() {
		ClientPlayNetworking.registerGlobalReceiver(AurorasCanvasNetworking.OPEN_CANVAS_GUI, AurorasCanvasClientNetworking::handleCanvasOpenGui);
	}

	private AurorasCanvasClientNetworking() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
