/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.network;

import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Represents the canvas edit submit payload.
 *
 * @param easelEntityId the easel entity identifier of the attached canvas
 * @param canvas the edited canvas data
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record CanvasEditSubmitPayload(int easelEntityId, IndexedCanvas canvas) {
	public CanvasEditSubmitPayload(FriendlyByteBuf buffer) {
		this(buffer.readVarInt(), IndexedCanvas.fromBuffer(buffer));
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.easelEntityId);
		this.canvas.writeBuffer(buffer);
	}
}
