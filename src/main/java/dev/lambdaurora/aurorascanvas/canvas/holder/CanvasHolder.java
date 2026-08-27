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
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.Map;

/**
 * Represents a holder of canvases.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public interface CanvasHolder<H extends CanvasHolder<H>> extends CanvasLikeHolder<Canvas> {
	/**
	 * {@return the type of this canvas holder}
	 */
	@Contract(pure = true)
	Type<H> type();

	/**
	 * Clones this canvas holder.
	 *
	 * @return the cloned canvas holder.
	 */
	@Contract(value = "-> new", pure = true)
	H copy();

	@SuppressWarnings("unchecked")
	default void setOnStack(ItemStack stack) {
		if (this.stream().allMatch(Canvas::isUnedited)) {
			stack.remove(this.type().componentType());
		} else {
			stack.set(this.type().componentType(), (H) this);
		}
	}

	@SuppressWarnings("unchecked")
	default CompoundTag toNbt() {
		return this.type().toNbt((H) this);
	}

	/**
	 * Represents the type of canvas holder.
	 *
	 * @param <H> the type of the canvas holder
	 * @version 1.1.0
	 * @since 1.0.0
	 */
	interface Type<H extends CanvasHolder<H>> {
		String name();

		Codec<H> codec();

		StreamCodec<ByteBuf, H> streamCodec();

		DataComponentType<H> componentType();

		@Contract("-> new")
		H createDefault();

		default H fromNbt(CompoundTag nbt) {
			return this.codec().parse(NbtOps.INSTANCE, nbt).result().orElseGet(this::createDefault);
		}

		default CompoundTag toNbt(H holder) {
			var encoded = this.codec().encodeStart(NbtOps.INSTANCE, holder).getOrThrow();

			if (!(encoded instanceof CompoundTag encodedNbt))
				throw new IllegalStateException("Canvases codec did not encode into a NBT compound.");

			return encodedNbt;
		}
	}

	final class Registry {
		public static final Map<String, Type<? extends CanvasHolder<?>>> REGISTRY = Map.of(
				"simple", SimpleCanvasHolder.TYPE,
				"glass", GlassCanvasHolder.TYPE
		);

		public static final StreamCodec<ByteBuf, CanvasHolder<?>> STREAM_CODEC = ByteBufCodecs.stringUtf8(32).dispatch(
				canvasHolder -> canvasHolder.type().name(),
				key -> REGISTRY.get(key).streamCodec()
		);

		private Registry() {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
		}
	}
}
