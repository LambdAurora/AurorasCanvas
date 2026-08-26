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

public class SimpleCanvasLikeHolder<T> implements CanvasLikeHolder<T> {
	private final T canvas;

	public SimpleCanvasLikeHolder(T canvas) {this.canvas = canvas;}

	public T canvas() {
		return this.canvas;
	}

	@Override
	public T getDefault() {
		return this.canvas;
	}

	@Override
	public Stream<T> stream() {
		return Stream.of(this.canvas);
	}

	@Override
	public <R> SimpleCanvasLikeHolder<R> map(Function<T, R> mapper) {
		return new SimpleCanvasLikeHolder<>(mapper.apply(this.canvas));
	}

	@Override
	public SimpleCanvasHolder mapToCanvas(Function<T, Canvas> mapper) {
		return new SimpleCanvasHolder(mapper.apply(this.canvas));
	}

	@Override
	public <I> void into(CanvasLikeHolder<I> other, BiConsumer<I, T> consumer) {
		if (other instanceof SimpleCanvasLikeHolder<I> actualOther) {
			consumer.accept(actualOther.canvas, this.canvas);
		}
	}
}
