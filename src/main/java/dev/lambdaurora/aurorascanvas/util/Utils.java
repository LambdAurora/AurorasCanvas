/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.util;

import com.mojang.serialization.Codec;

import java.nio.ByteBuffer;

public final class Utils {
	public static final Codec<byte[]> BYTE_ARRAY_CODEC = Codec.BYTE_BUFFER.xmap(buffer -> {
		if (buffer.hasArray()) {
			return buffer.array();
		}
		var bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		return bytes;
	}, ByteBuffer::wrap);

	private Utils() {
		throw new UnsupportedOperationException("Utils only contains static definitions.");
	}

	public static double posMod(double n, double d) {
		double v = n % d;
		if (v < 0) v = d + v;
		return v;
	}
}
