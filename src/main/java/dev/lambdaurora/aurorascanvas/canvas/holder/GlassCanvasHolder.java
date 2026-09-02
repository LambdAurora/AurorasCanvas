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
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a glass canvas holder that is holding a front and back canvas.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class GlassCanvasHolder extends GlassCanvasLikeHolder<Canvas> implements CanvasHolder<GlassCanvasHolder.PlacementData, GlassCanvasHolder> {
	public static final Codec<GlassCanvasHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					CanvasSerialization.CANVAS_CODEC.optionalFieldOf("front")
							.xmap(canvas -> canvas.orElseGet(Canvas::new), canvas -> canvas.isUnedited() ? Optional.empty() : Optional.of(canvas))
							.forGetter(GlassCanvasHolder::front),
					CanvasSerialization.CANVAS_CODEC.optionalFieldOf("back")
							.xmap(canvas -> canvas.orElseGet(Canvas::new), canvas -> canvas.isUnedited() ? Optional.empty() : Optional.of(canvas))
							.forGetter(GlassCanvasHolder::back)
			).apply(instance, GlassCanvasHolder::new)
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
	public Stream<PlacedCanvas> streamPlacedDefault() {
		return this.streamPlaced(PlacementData.DEFAULT);
	}

	@Override
	public Stream<PlacedCanvas> streamPlaced(PlacementData placementData) {
		return Stream.of(
				new PlacedCanvas(this.front(), placementData.facing, placementData.pane ? .436f : PlacedCanvas.DEFAULT_DEPTH),
				new PlacedCanvas(this.back(), placementData.facing.getOpposite(), placementData.pane ? .436f : -.005f)
		);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof GlassCanvasHolder that)) return false;
		return Objects.equals(this.front(), that.back());
	}

	public record PlacementData(Direction facing, boolean pane) {
		public static final PlacementData DEFAULT = new PlacementData(Direction.NORTH, false);
	}
}
