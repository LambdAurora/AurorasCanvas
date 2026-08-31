/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat;

import dev.lambdaurora.aurorascanvas.util.FabricRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasIds.*;

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

	public static void init() {
		addBlockItem(BLACKBOARD_ID);
		addBlockItem(WAXED_BLACKBOARD_ID);
		addBlockItem(CHALKBOARD_ID);
		addBlockItem(WAXED_CHALKBOARD_ID);
		addBlockItem(GLASSBOARD_ID);
		addBlockItem(WAXED_GLASSBOARD_ID);

		((FabricRegistry) BuiltInRegistries.ITEM).aurorascanvas$addAlias(id(PAINTER_PALETTE_ID.getPath()), PAINTER_PALETTE_ID);
	}

	private static void addBlockItem(Identifier newId) {
		final var old = id(newId.getPath());
		((FabricRegistry) BuiltInRegistries.BLOCK).aurorascanvas$addAlias(old, newId);
		((FabricRegistry) BuiltInRegistries.ITEM).aurorascanvas$addAlias(old, newId);
	}
}
