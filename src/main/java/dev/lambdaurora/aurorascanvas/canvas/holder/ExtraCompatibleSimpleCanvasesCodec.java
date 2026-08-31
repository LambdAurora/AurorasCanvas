/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas.holder;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ExtraCompatibleSimpleCanvasesCodec implements Codec<SimpleCanvasHolder> {
	private final Codec<SimpleCanvasHolder> actual;
	private final Codec<SimpleCanvasHolder> fallback;

	public ExtraCompatibleSimpleCanvasesCodec(final Codec<SimpleCanvasHolder> actual, final Codec<SimpleCanvasHolder> fallback) {
		this.actual = actual;
		this.fallback = fallback;
	}

	@Override
	public <T> DataResult<Pair<SimpleCanvasHolder, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getMap(input).flatMap(map -> {
			if (map.get("canvas") != null || map.get("pixels") != null || map.get("lit") != null) {
				return this.actual.decode(ops, input);
			}

			var fallback = this.fallback.decode(ops, input);
			if (fallback.isError()) {
				return this.actual.decode(ops, input);
			} else {
				return fallback;
			}
		});
	}

	@Override
	public <T> DataResult<T> encode(final SimpleCanvasHolder input, final DynamicOps<T> ops, final T prefix) {
		return this.actual.encode(input, ops, prefix);
	}

	@Override
	public boolean equals(final @Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExtraCompatibleSimpleCanvasesCodec other = ((ExtraCompatibleSimpleCanvasesCodec) o);
		return Objects.equals(this.actual, other.actual) && Objects.equals(this.fallback, other.fallback);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.actual, this.fallback);
	}

	@Override
	public String toString() {
		return "ExtraCompatibleCanvasesCodec[" + this.actual + ", " + this.fallback + ']';
	}
}
