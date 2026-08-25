/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasIds.*;

/**
 * Sets up the backwards compatibility of worlds that used Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class AurorasDecoDataUpper {
	public static final String OLD_NAMESPACE = "aurorasdeco";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(OLD_NAMESPACE, path);
	}

	public static void init() {
		addBlockItem(BLACKBOARD_ID);
		addBlockItem(WAXED_BLACKBOARD_ID);
		addBlockItem(CHALKBOARD_ID);
		addBlockItem(WAXED_CHALKBOARD_ID);
		addBlockItem(GLASSBOARD_ID);
		addBlockItem(WAXED_GLASSBOARD_ID);

		BuiltInRegistries.ITEM.addAlias(id(PAINTER_PALETTE_ID.getPath()), PAINTER_PALETTE_ID);
	}

	private static void addBlockItem(Identifier newId) {
		final var old = id(newId.getPath());
		BuiltInRegistries.BLOCK.addAlias(old, newId);
		BuiltInRegistries.ITEM.addAlias(old, newId);
	}
}
