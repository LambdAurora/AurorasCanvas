/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

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
public final class AurorasCanvasPackets {
	private AurorasCanvasPackets() {
		throw new UnsupportedOperationException("AurorasCanvasPackets only contains static definitons.");
	}

	public static final Identifier PAINTER_PALETTE_SCROLL = AurorasCanvas.id("painter_palette/scroll");

	public static void handlePainterPaletteScroll(
			MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
			FriendlyByteBuf buf, PacketSender responseSender
	) {
		double scrollDelta = buf.readDouble();
		boolean toolModifier = buf.readBoolean();

		server.execute(() -> {
			var mainHandStack = player.getMainHandItem();

			if (mainHandStack.getItem() instanceof PainterPaletteItem paletteItem) {
				paletteItem.onScroll(player, mainHandStack, scrollDelta, toolModifier);
			}
		});
	}
}
