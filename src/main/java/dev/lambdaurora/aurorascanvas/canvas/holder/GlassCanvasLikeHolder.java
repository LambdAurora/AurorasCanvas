/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas.holder;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class GlassCanvasLikeHolder<T> implements CanvasLikeHolder<T> {
	private final T front;
	private final T back;

	public GlassCanvasLikeHolder(T front, T back) {
		this.front = front;
		this.back = back;
	}

	public T front() {
		return this.front;
	}

	public T back() {
		return this.back;
	}

	@Override
	public T getDefault() {
		return this.front;
	}

	@Override
	public Stream<T> stream() {
		return Stream.of(this.front, this.back);
	}

	@Override
	public <R> GlassCanvasLikeHolder<R> map(Function<T, R> mapper) {
		return new GlassCanvasLikeHolder<>(mapper.apply(this.front), mapper.apply(this.back));
	}

	@Override
	public GlassCanvasHolder mapToCanvas(Function<T, Canvas> mapper) {
		return new GlassCanvasHolder(mapper.apply(this.front), mapper.apply(this.back));
	}

	@Override
	public <I> void into(CanvasLikeHolder<I> other, BiConsumer<I, T> consumer) {
		if (other instanceof GlassCanvasLikeHolder<I> actualOther) {
			consumer.accept(actualOther.front, this.front);
			consumer.accept(actualOther.back, this.back);
		}
	}
}
