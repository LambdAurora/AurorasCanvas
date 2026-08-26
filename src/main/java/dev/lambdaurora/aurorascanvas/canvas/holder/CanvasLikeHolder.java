package dev.lambdaurora.aurorascanvas.canvas.holder;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface CanvasLikeHolder<T> {
	T getDefault();

	Stream<T> stream();

	<R> CanvasLikeHolder<R> map(Function<T, R> mapper);

	CanvasHolder mapToCanvas(Function<T, Canvas> mapper);

	<I> void into(CanvasLikeHolder<I> other, BiConsumer<I, T> consumer);
}
