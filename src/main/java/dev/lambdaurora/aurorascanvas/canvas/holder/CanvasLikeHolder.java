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

/**
 * Represents a holder of canvases-like objects.
 *
 * @param <T> the canvas-like object type
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CanvasLikeHolder<T> {
	/**
	 * {@return the default canvas}
	 */
	T getDefault();

	/**
	 * {@return a stream of held canvases}
	 */
	Stream<T> stream();

	<R> CanvasLikeHolder<R> map(Function<T, R> mapper);

	CanvasHolder<?> mapToCanvas(Function<T, Canvas> mapper);

	<I> void into(CanvasLikeHolder<I> other, BiConsumer<I, T> consumer);
}
