/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.network;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Contains the different packet definitions used in Aurora's Canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasCanvasNetworking {
	public static final Identifier OPEN_CANVAS_GUI = AurorasCanvas.id("canvas/open_gui");
	public static final Identifier CANVAS_SUBMIT_EDIT = AurorasCanvas.id("canvas/submit_edit");
	public static final Identifier PAINTER_PALETTE_SCROLL = AurorasCanvas.id("painter_palette/scroll");

	public static void handleCanvasSubmitEdit(
			MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
			FriendlyByteBuf buf, PacketSender responseSender
	) {
		var payload = new CanvasEditSubmitPayload(buf);

		server.execute(() -> {
			var entity = player.level().getEntity(payload.easelEntityId());

			if (entity instanceof EaselEntity easel) {
				easel.submit(player, payload.canvas());
			}
		});
	}

	public static void handlePainterPaletteScroll(
			MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
			FriendlyByteBuf buf, PacketSender responseSender
	) {
		var payload = new PainterPaletteScrollPayload(buf);

		server.execute(() -> {
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
