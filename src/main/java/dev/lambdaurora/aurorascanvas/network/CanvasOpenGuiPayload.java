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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the canvas open GUI payload.
 *
 * @param easelEntityId the easel entity identifier of the attached canvas
 * @param canvas the canvas item to open the GUI of
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public record CanvasOpenGuiPayload(int easelEntityId, ItemStack canvas, ItemStack painterPalette) implements CustomPacketPayload {
	public static Type<CanvasOpenGuiPayload> TYPE = new Type<>(AurorasCanvas.id("canvas/open_gui"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CanvasOpenGuiPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, CanvasOpenGuiPayload::easelEntityId,
			ItemStack.STREAM_CODEC, CanvasOpenGuiPayload::canvas,
			ItemStack.STREAM_CODEC, CanvasOpenGuiPayload::painterPalette,
			CanvasOpenGuiPayload::new
	);

	public Component title() {
		return this.canvas.getHoverName();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	static {
		PayloadTypeRegistry.playS2C().register(TYPE, STREAM_CODEC);
	}
}
