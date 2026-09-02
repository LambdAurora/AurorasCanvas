/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas.holder;

import com.mojang.serialization.Codec;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasSerialization;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a canvas holder that is holding only one canvas.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class SimpleCanvasHolder extends SimpleCanvasLikeHolder<Canvas> implements CanvasHolder<Direction, SimpleCanvasHolder> {
	public static final Codec<SimpleCanvasHolder> CODEC = CanvasSerialization.CANVAS_CODEC.xmap(SimpleCanvasHolder::new, SimpleCanvasHolder::canvas);
	public static final StreamCodec<ByteBuf, SimpleCanvasHolder> STREAM_CODEC = CanvasSerialization.CANVAS_STREAM_CODEC.map(SimpleCanvasHolder::new, SimpleCanvasHolder::canvas);

	public static final Type<SimpleCanvasHolder> TYPE = new Type<>() {
		@Override
		public String name() {
			return "simple";
		}

		@Override
		public Codec<SimpleCanvasHolder> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<ByteBuf, SimpleCanvasHolder> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public DataComponentType<SimpleCanvasHolder> componentType() {
			return AurorasCanvasRegistry.CANVAS_COMPONENT_TYPE;
		}

		@Override
		public SimpleCanvasHolder createDefault() {
			return new SimpleCanvasHolder(new Canvas());
		}
	};

	public SimpleCanvasHolder(Canvas canvas) {
		super(canvas);
	}

	@Override
	public Type<SimpleCanvasHolder> type() {
		return TYPE;
	}

	@Override
	public SimpleCanvasHolder copy() {
		var clone = new Canvas();
		clone.copy(this.canvas());
		return new SimpleCanvasHolder(clone);
	}

	@Override
	public Stream<PlacedCanvas> streamPlacedDefault() {
		return this.streamPlaced(Direction.NORTH);
	}

	@Override
	public Stream<PlacedCanvas> streamPlaced(Direction facing) {
		return Stream.of(new PlacedCanvas(this.canvas(), facing));
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SimpleCanvasHolder that)) return false;
		return Objects.equals(this.canvas(), that.canvas());
	}
}
