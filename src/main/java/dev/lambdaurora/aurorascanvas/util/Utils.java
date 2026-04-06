/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class Utils {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());

	private Utils() {
		throw new UnsupportedOperationException("Utils only contains static definitions.");
	}
}
