/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

import net.minecraft.resources.Identifier;

import static dev.lambdaurora.aurorascanvas.AurorasCanvas.id;

public final class AurorasCanvasIds {
	public static Identifier CANVAS_ID = id("canvas");
	public static Identifier WHITEBOARD_ID = id("whiteboard");
	public static Identifier WAXED_WHITEBOARD_ID = id("waxed_whiteboard");
	public static Identifier BLACKBOARD_ID = id("blackboard");
	public static Identifier WAXED_BLACKBOARD_ID = id("waxed_blackboard");
	public static Identifier CHALKBOARD_ID = id("chalkboard");
	public static Identifier WAXED_CHALKBOARD_ID = id("waxed_chalkboard");
	public static Identifier GLASSBOARD_ID = id("glassboard");
	public static Identifier WAXED_GLASSBOARD_ID = id("waxed_glassboard");
	public static Identifier CANVAS_PRESS_ID = id("canvas_press");

	private AurorasCanvasIds() {
		throw new UnsupportedOperationException("AurorasCanvasIds only contains static definitions.");
	}
}
