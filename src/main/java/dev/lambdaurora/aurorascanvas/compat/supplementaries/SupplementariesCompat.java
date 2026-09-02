/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat.supplementaries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.AurorasCanvasIds;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasColor;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import dev.lambdaurora.aurorascanvas.canvas.CanvasPixel;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;
import dev.lambdaurora.aurorascanvas.util.FabricRegistry;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.LongStream;

/**
 * Represents utilities related to Supplementaries' integration.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SupplementariesCompat {
	public static final String NAMESPACE = "supplementaries";

	public static final boolean SHOULD_DATAFIX = !FabricLoader.getInstance().isModLoaded(NAMESPACE);

	private static final Codec<byte[]> MATRIX_CODEC_OR_LEGACY = Utils.codecWithAlternative(
			Codec.LONG_STREAM.xmap(LongStream::toArray, Arrays::stream)
					.xmap(SupplementariesCompat::unpackPixels, SupplementariesCompat::packPixels),
			byteMatrix(16).xmap(bytes -> {
				var out = new byte[CanvasHandler.PIXELS_COUNT];
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						out[y * 16 + x] = bytes[x][y];
					}
				}
				return out;
			}, bytes -> {
				throw new UnsupportedOperationException("Cannot convert from Aurora's Canvas format to Supplementaries'.");
			})
	);
	private static final Codec<short[]> TO_AURORAS_CANVAS_FORMAT_CODEC = MATRIX_CODEC_OR_LEGACY.xmap(supplementaries -> {
		var pixels = new short[CanvasHandler.PIXELS_COUNT];
		for (int i = 0; i < supplementaries.length; i++) {
			var color = switch (supplementaries[i]) {
				case 0 -> CanvasColor.BLACK;
				case 1 -> CanvasColor.WHITE;
				case 15 -> CanvasColor.ORANGE;
				default -> CanvasColor.byId((byte) (supplementaries[i] + 1));
			};
			pixels[i] = new CanvasPixel(color).toRawId();
		}
		return pixels;
	}, pixels -> {
		throw new UnsupportedOperationException("Cannot convert from Aurora's Canvas format to Supplementaries'.");
	});
	private static final Codec<Raw> RAW_BLACKBOARD_CODEC = Utils.codecWithAlternative(
			// 1.21 format.
			RecordCodecBuilder.create(instance -> instance.group(
							TO_AURORAS_CANVAS_FORMAT_CODEC.fieldOf("values").forGetter(Raw::pixels),
							Codec.BOOL.fieldOf("glow").forGetter(Raw::glow),
							Codec.BOOL.fieldOf("waxed").forGetter(Raw::waxed)
					).apply(instance, Raw::new)
			),
			// 1.20 format.
			RecordCodecBuilder.create(instance -> instance.group(
							TO_AURORAS_CANVAS_FORMAT_CODEC.fieldOf("Pixels").forGetter(Raw::pixels),
							Codec.BOOL.optionalFieldOf("glow", false).forGetter(Raw::glow),
							Codec.BOOL.optionalFieldOf("Waxed", false).forGetter(Raw::waxed)
					).apply(instance, Raw::new)
			)
	);
	public static final Codec<Data> BLACKBOARD_CODEC = RAW_BLACKBOARD_CODEC.flatXmap(
			raw -> DataResult.success(new Data(new Canvas(raw.pixels, raw.glow), raw.waxed)),
			data -> DataResult.error(() -> "Cannot convert from Aurora's Canvas format to Supplementaries'.")
	);

	public static Optional<Data> fromNbt(CompoundTag nbt) {
		return BLACKBOARD_CODEC.parse(NbtOps.INSTANCE, nbt).result();
	}

	public static Optional<SimpleCanvasHolder> canvasHolderFromNbt(CompoundTag nbt) {
		return fromNbt(nbt).map(data -> new SimpleCanvasHolder(data.canvas));
	}

	public static long[] packPixels(byte[] pixels) {
		long[] packed = new long[pixels.length];
		for (int x = 0; x < 16; x++) {
			long l = 0;
			for (int y = 0; y < 16; y++) {
				l = l | (((long) (pixels[y * 16 + x] & 15)) << y * 4);
			}
			packed[x] = l;
		}
		return packed;
	}

	public static byte[] unpackPixels(long[] packed) {
		var bytes = new byte[CanvasHandler.PIXELS_COUNT];
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				bytes[y * 16 + x] = (byte) ((packed[x] >> y * 4) & 15);
			}
		}
		return bytes;
	}

	private static Codec<byte[][]> byteMatrix(int size) {
		return Codec.BYTE_BUFFER.xmap(buffer -> {
			byte[][] matrix = new byte[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					matrix[i][j] = buffer.get();
				}
			}
			return matrix;
		}, bytes -> {
			ByteBuffer buffer = ByteBuffer.allocate(size * size * 4);
			for (byte[] row : bytes) {
				buffer.put(row);
			}
			return buffer;
		});
	}

	public static void fixNbt(CompoundTag nbt) {
		canvasHolderFromNbt(nbt).ifPresent(canvases -> {
			// Clean up leftovers.
			nbt.remove("values");
			nbt.remove("Pixels");
			nbt.remove("glow");
			nbt.remove("waxed");
			nbt.remove("Waxed");
			// Put the new NBT.
			nbt.put("canvas", canvases.toNbt());
		});
	}

	public static void init() {
		if (SHOULD_DATAFIX) {
			((FabricRegistry) BuiltInRegistries.BLOCK).aurorascanvas$addAlias(new Identifier(NAMESPACE, "blackboard"), AurorasCanvasIds.BLACKBOARD_ID);
		}
	}

	public record Raw(short[] pixels, boolean glow, boolean waxed) {
	}

	public record Data(Canvas canvas, boolean waxed) {
	}
}
