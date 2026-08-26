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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasSerialization;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public final class SimpleCanvasHolder extends SimpleCanvasLikeHolder<Canvas> implements CanvasHolder<SimpleCanvasHolder> {
	public static final Codec<SimpleCanvasHolder> CODEC = new BackwardCompatibleCanvasesCodec<>(
			RecordCodecBuilder.create(instance -> instance.group(
					CanvasSerialization.CANVAS_CODEC.optionalFieldOf("canvas")
							.xmap(canvas -> canvas.orElseGet(Canvas::new), canvas -> canvas.isUnedited() ? Optional.empty() : Optional.of(canvas))
							.forGetter(SimpleCanvasHolder::canvas)
			).apply(instance, SimpleCanvasHolder::new)),
			SimpleCanvasHolder::new
	);

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
		public SimpleCanvasHolder createDefault() {
			return new SimpleCanvasHolder(new Canvas());
		}

		@Override
		public SimpleCanvasHolder fromBuffer(FriendlyByteBuf buffer) {
			return new SimpleCanvasHolder(CanvasSerialization.fromByteBuf(buffer));
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
	public void writeBuffer(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.type().name());
		CanvasSerialization.writeCanvasToBuffer(buffer, this.canvas());
	}
}
