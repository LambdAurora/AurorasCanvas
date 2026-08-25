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

/**
 * Represents the painter palette scroll payload.
 *
 * @param scrollDelta the scroll delta
 * @param toolModifier {@code true} if the tool modifier is active, or {@code false} otherwise
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record PainterPaletteScrollPayload(double scrollDelta, boolean toolModifier) {
	public PainterPaletteScrollPayload(FriendlyByteBuf buffer) {
		this(buffer.readDouble(), buffer.readBoolean());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeDouble(this.scrollDelta);
		buffer.writeBoolean(this.toolModifier);
	}
}
