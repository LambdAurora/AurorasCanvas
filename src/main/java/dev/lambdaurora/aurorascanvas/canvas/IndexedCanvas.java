/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Represents an indexed canvas.
 *
 * @param key the key of the canvas
 * @param canvas the canvas
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public record IndexedCanvas(String key, Canvas canvas) {
	public static final Provider SIMPLE = new Provider("");
	public static final Provider FRONT = new Provider("front", nbt -> {
		if (!nbt.contains("front", Tag.TAG_COMPOUND)) {
			return SIMPLE.reader().fromNbt(nbt);
		}

		return fromNbt("front", nbt);
	});
	public static final Provider BACK = new Provider("back");

	public static final StreamCodec<FriendlyByteBuf, IndexedCanvas> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, IndexedCanvas::key,
			CanvasSerialization.CANVAS_STREAM_CODEC, IndexedCanvas::canvas,
			IndexedCanvas::new
	);

	public static IndexedCanvas fromNbt(String key, CompoundTag nbt) {
		if (key.isEmpty()) {
			return new IndexedCanvas(key, Canvas.fromNbt(nbt));
		} else {
			return new IndexedCanvas(key, Canvas.fromNbt(nbt.getCompound(key)));
		}
	}

	public void writeNbt(CompoundTag nbt) {
		if (this.key.isEmpty()) {
			var encodedNbt = this.canvas.toNbt();

			if (this.canvas.isEmpty() && !this.canvas.isGlowing()) {
				encodedNbt.getAllKeys().forEach(nbt::remove);
			} else {
				nbt.merge(encodedNbt);
			}
		} else {
			if (this.canvas.isEmpty() && !this.canvas.isGlowing()) {
				nbt.remove(this.key);
			} else {
				nbt.put(this.key, this.canvas.toNbt());
			}
		}
	}

	public record Provider(String key, Reader reader) {
		public Provider(String key) {
			this(key, nbt -> fromNbt(key, nbt));
		}
	}

	@FunctionalInterface
	public interface Reader {
		IndexedCanvas fromNbt(CompoundTag nbt);
	}
}
