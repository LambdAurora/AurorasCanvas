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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Represents the painter palette scroll payload.
 *
 * @param scrollDelta the scroll delta
 * @param toolModifier {@code true} if the tool modifier is active, or {@code false} otherwise
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public record PainterPaletteScrollPayload(double scrollDelta, boolean toolModifier) implements CustomPacketPayload {
	public static Type<PainterPaletteScrollPayload> TYPE = new Type<>(AurorasCanvas.id("painter_palette/scroll"));
	public static final StreamCodec<FriendlyByteBuf, PainterPaletteScrollPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, PainterPaletteScrollPayload::scrollDelta,
			ByteBufCodecs.BOOL, PainterPaletteScrollPayload::toolModifier,
			PainterPaletteScrollPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	static {
		PayloadTypeRegistry.playC2S().register(TYPE, STREAM_CODEC);
	}
}
