/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat;

import net.minecraft.resources.Identifier;

/**
 * Sets up the backwards compatibility of worlds that used Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoDataUpper {
	public static final String OLD_NAMESPACE = "aurorasdeco";

	public static Identifier id(String path) {
		return new Identifier(OLD_NAMESPACE, path);
	}
}
