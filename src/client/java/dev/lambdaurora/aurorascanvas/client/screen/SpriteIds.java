/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.resources.Identifier;

/**
 * Provides some common sprites.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.1.0
 */
public final class SpriteIds {
	public static final Identifier SLOT_SPRITE = AurorasCanvas.id("palette/slot");
	public static final Identifier TOOL_SLOT_SPRITE = AurorasCanvas.id("palette/empty_tool_slot");
	public static final Identifier SELECT_HIGHLIGHT_SPRITE = AurorasCanvas.id("palette/select_highlight");
	public static final Identifier LOCKED_SPRITE = Identifier.withDefaultNamespace("container/cartography_table/locked");

	private SpriteIds() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
