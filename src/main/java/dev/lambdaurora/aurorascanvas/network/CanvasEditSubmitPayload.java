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
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Represents the canvas edit submit payload.
 *
 * @param easelEntityId the easel entity identifier of the attached canvas
 * @param canvas the edited canvas data
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public record CanvasEditSubmitPayload(int easelEntityId, IndexedCanvas canvas) implements CustomPacketPayload {
	public static Type<CanvasEditSubmitPayload> TYPE = new Type<>(AurorasCanvas.id("canvas/open_gui"));
	public static final StreamCodec<FriendlyByteBuf, CanvasEditSubmitPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, CanvasEditSubmitPayload::easelEntityId,
			IndexedCanvas.STREAM_CODEC, CanvasEditSubmitPayload::canvas,
			CanvasEditSubmitPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	static {
		PayloadTypeRegistry.playC2S().register(TYPE, STREAM_CODEC);
	}
}
