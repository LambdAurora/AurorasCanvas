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

public final class GlassCanvasHolder extends GlassCanvasLikeHolder<Canvas> implements CanvasHolder<GlassCanvasHolder> {
	public static final Codec<GlassCanvasHolder> CODEC = new BackwardCompatibleCanvasesCodec<>(
			RecordCodecBuilder.create(instance -> instance.group(
					CanvasSerialization.CANVAS_CODEC.optionalFieldOf("front")
							.xmap(canvas -> canvas.orElseGet(Canvas::new), canvas -> canvas.isUnedited() ? Optional.empty() : Optional.of(canvas))
							.forGetter(GlassCanvasHolder::front),
					CanvasSerialization.CANVAS_CODEC.optionalFieldOf("back")
							.xmap(canvas -> canvas.orElseGet(Canvas::new), canvas -> canvas.isUnedited() ? Optional.empty() : Optional.of(canvas))
							.forGetter(GlassCanvasHolder::back)
			).apply(instance, GlassCanvasHolder::new)),
			canvas -> new GlassCanvasHolder(canvas, new Canvas())
	);

	public static final Type<GlassCanvasHolder> TYPE = new Type<>() {
		@Override
		public String name() {
			return "glass";
		}

		@Override
		public Codec<GlassCanvasHolder> codec() {
			return CODEC;
		}

		@Override
		public GlassCanvasHolder createDefault() {
			return new GlassCanvasHolder(new Canvas(), new Canvas());
		}

		@Override
		public GlassCanvasHolder fromBuffer(FriendlyByteBuf buffer) {
			var front = CanvasSerialization.fromByteBuf(buffer);
			var back = CanvasSerialization.fromByteBuf(buffer);
			return new GlassCanvasHolder(front, back);
		}
	};

	public GlassCanvasHolder(Canvas front, Canvas back) {
		super(front, back);
	}

	@Override
	public Type<GlassCanvasHolder> type() {
		return TYPE;
	}

	@Override
	public void writeBuffer(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.type().name());
		CanvasSerialization.writeCanvasToBuffer(buffer, this.front());
		CanvasSerialization.writeCanvasToBuffer(buffer, this.back());
	}
}
