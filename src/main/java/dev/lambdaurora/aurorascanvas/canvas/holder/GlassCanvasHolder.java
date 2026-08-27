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
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasSerialization;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
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
	public static final StreamCodec<ByteBuf, GlassCanvasHolder> STREAM_CODEC = StreamCodec.composite(
			CanvasSerialization.CANVAS_STREAM_CODEC, GlassCanvasHolder::front,
			CanvasSerialization.CANVAS_STREAM_CODEC, GlassCanvasHolder::back,
			GlassCanvasHolder::new
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
		public StreamCodec<ByteBuf, GlassCanvasHolder> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public DataComponentType<GlassCanvasHolder> componentType() {
			return AurorasCanvasRegistry.GLASS_CANVAS_COMPONENT_TYPE;
		}

		@Override
		public GlassCanvasHolder createDefault() {
			return new GlassCanvasHolder(new Canvas(), new Canvas());
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
	public GlassCanvasHolder copy() {
		var frontClone = new Canvas();
		frontClone.copy(this.front());
		var backClone = new Canvas();
		backClone.copy(this.back());
		return new GlassCanvasHolder(frontClone, backClone);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof GlassCanvasHolder that)) return false;
		return Objects.equals(this.front(), that.back());
	}
}
