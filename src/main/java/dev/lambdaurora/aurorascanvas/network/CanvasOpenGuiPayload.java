/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the canvas open GUI payload.
 *
 * @param easelEntityId the easel entity identifier of the attached canvas
 * @param canvas the canvas item to open the GUI of
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record CanvasOpenGuiPayload(int easelEntityId, ItemStack canvas, ItemStack painterPalette) {
	public CanvasOpenGuiPayload(FriendlyByteBuf buffer) {
		this(buffer.readVarInt(), buffer.readItem(), buffer.readItem());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.easelEntityId);
		buffer.writeItem(this.canvas);
		buffer.writeItem(painterPalette);
	}

	public Component title() {
		return this.canvas.getHoverName();
	}
}
