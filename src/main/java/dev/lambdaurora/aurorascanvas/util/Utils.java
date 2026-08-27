/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.util;

public final class Utils {
	private Utils() {
		throw new UnsupportedOperationException("Utils only contains static definitions.");
	}

	public static double posMod(double n, double d) {
		double v = n % d;
		if (v < 0) v = d + v;
		return v;
	}
}
