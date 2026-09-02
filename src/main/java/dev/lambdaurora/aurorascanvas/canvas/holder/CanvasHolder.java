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
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a holder of canvases.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CanvasHolder<P, H extends CanvasHolder<P, H>> extends CanvasLikeHolder<Canvas> {
	/**
	 * {@return the type of this canvas holder}
	 */
	Type<H> type();

	/**
	 * Clones this canvas holder.
	 *
	 * @return the cloned canvas holder.
	 */
	@Contract(value = "-> new", pure = true)
	H copy();

	Stream<PlacedCanvas> streamPlacedDefault();

	Stream<PlacedCanvas> streamPlaced(P placementData);

	void writeBuffer(FriendlyByteBuf buffer);

	@SuppressWarnings("unchecked")
	default CompoundTag toNbt() {
		return this.type().toNbt((H) this);
	}

	static CanvasHolder<?, ?> fromBuffer(FriendlyByteBuf buffer) {
		var typeName = buffer.readUtf();
		var type = Registry.REGISTRY.get(typeName);

		if (type == null) {
			throw new IllegalArgumentException("Invalid type " + typeName + ".");
		}

		return type.fromBuffer(buffer);
	}

	/**
	 * Represents the type of canvas holder.
	 *
	 * @param <H> the type of the canvas holder
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	interface Type<H extends CanvasHolder<?, H>> {
		String name();

		Codec<H> codec();

		@Contract("-> new")
		H createDefault();

		default H fromNbt(CompoundTag nbt) {
			return this.codec().parse(NbtOps.INSTANCE, nbt).result().orElseGet(this::createDefault);
		}

		default CompoundTag toNbt(H holder) {
			var encoded = this.codec().encodeStart(NbtOps.INSTANCE, holder)
					.getOrThrow(false, message -> {});

			if (!(encoded instanceof CompoundTag encodedNbt))
				throw new IllegalStateException("Canvases codec did not encode into a NBT compound.");

			return encodedNbt;
		}

		H fromBuffer(FriendlyByteBuf buffer);
	}

	final class Registry {
		public static final Map<String, Type<? extends CanvasHolder<?, ?>>> REGISTRY = Map.of(
				"simple", SimpleCanvasHolder.TYPE,
				"glass", GlassCanvasHolder.TYPE
		);

		private Registry() {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
		}
	}
}
