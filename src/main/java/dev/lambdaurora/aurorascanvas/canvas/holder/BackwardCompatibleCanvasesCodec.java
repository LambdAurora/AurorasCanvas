package dev.lambdaurora.aurorascanvas.canvas.holder;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasSerialization;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public final class BackwardCompatibleCanvasesCodec<H> implements Codec<H> {
	private final Codec<H> actual;
	private final Function<Canvas, H> fallbackFactory;

	public BackwardCompatibleCanvasesCodec(final Codec<H> actual, Function<Canvas, H> fallbackFactory) {
		this.actual = actual;
		this.fallbackFactory = fallbackFactory;
	}

	@Override
	public <T> DataResult<Pair<H, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getMap(input).flatMap(map -> {
			if (map.get("pixels") == null && map.get("lit") == null) {
				return this.actual.decode(ops, input);
			}

			return CanvasSerialization.CANVAS_CODEC.decode(ops, input).map(pair -> new Pair<>(this.fallbackFactory.apply(pair.getFirst()), pair.getSecond()));
		});
	}

	@Override
	public <T> DataResult<T> encode(final H input, final DynamicOps<T> ops, final T prefix) {
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
		final BackwardCompatibleCanvasesCodec<?> other = ((BackwardCompatibleCanvasesCodec<?>) o);
		return Objects.equals(this.actual, other.actual) && Objects.equals(this.fallbackFactory, other.fallbackFactory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.actual, this.fallbackFactory);
	}

	@Override
	public String toString() {
		return "BackwardCompatibleCanvasesCodec[" + this.actual + ", " + this.fallbackFactory + ']';
	}
}