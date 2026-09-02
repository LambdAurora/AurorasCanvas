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
import dev.yumi.mc.core.api.YumiMods;
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
 * @version 1.1.0
 * @since 1.0.0
 */
public final class SupplementariesCompat {
	public static final String NAMESPACE = "supplementaries";
	public static final Identifier BLACKBOARD_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "blackboard");

	public static final boolean SHOULD_DATAFIX = !YumiMods.get().isModLoaded(NAMESPACE);

	private static final Codec<byte[]> MATRIX_CODEC_OR_LEGACY = Codec.withAlternative(
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
			}),
			Codec.LONG_STREAM.xmap(LongStream::toArray, Arrays::stream)
					.xmap(SupplementariesCompat::unpackPixels, SupplementariesCompat::packPixels)
	);
	private static final Codec<short[]> TO_AURORAS_CANVAS_FORMAT_CODEC = MATRIX_CODEC_OR_LEGACY.xmap(supplementaries -> {
		var pixels = new short[CanvasHandler.PIXELS_COUNT];
		for (int i = 0; i < supplementaries.length; i++) {
			pixels[i] = fromSupplementariesColor(supplementaries[i]);
		}
		return pixels;
	}, pixels -> {
		throw new UnsupportedOperationException("Cannot convert from Aurora's Canvas format to Supplementaries'.");
	});
	private static final Codec<Raw> RAW_BLACKBOARD_CODEC = Codec.withAlternative(
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
	public static final Codec<SimpleCanvasHolder> CODEC = BLACKBOARD_CODEC.flatXmap(
			data -> DataResult.success(new SimpleCanvasHolder(data.canvas)),
			simpleCanvasHolder -> DataResult.error(() -> "Cannot convert from Aurora's Canvas format to Supplementaries'.")
	);

	public static Optional<SimpleCanvasHolder> canvasHolderFromNbt(CompoundTag nbt) {
		return CODEC.parse(NbtOps.INSTANCE, nbt).result();
	}

	private static short fromSupplementariesColor(byte supplementaries) {
		var color = switch (supplementaries) {
			case 0 -> CanvasColor.BLACK;
			case 1 -> CanvasColor.WHITE;
			case 15 -> CanvasColor.ORANGE;
			default -> CanvasColor.byId((byte) (supplementaries + 1));
		};
		return new CanvasPixel(color).toRawId();
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
			BuiltInRegistries.BLOCK.addAlias(BLACKBOARD_ID, AurorasCanvasIds.BLACKBOARD_ID);
			BuiltInRegistries.ITEM.addAlias(BLACKBOARD_ID, AurorasCanvasIds.BLACKBOARD_ID);
			BuiltInRegistries.DATA_COMPONENT_TYPE.addAlias(BLACKBOARD_ID, AurorasCanvasIds.CANVAS_ID);
		}/* else {
			CanvasCloneRecipe.INPUT_GETTER_EVENT.register(stack -> {
				if (stack.has(ModComponents.BLACKBOARD.get())) {
					var data = stack.get(ModComponents.BLACKBOARD.get());
					var canvas = new Canvas();

					for (int y = 0; y < 16; y++) {
						for (int x = 0; x < 16; x++) {
							canvas.setPixel(x, y, fromSupplementariesColor(data.getPixel(x, y)));
						}
					}

					return Optional.of(new SimpleCanvasHolder(canvas));
				}

				return Optional.empty();
			});
		}*/
	}

	public record Raw(short[] pixels, boolean glow, boolean waxed) {
	}

	public record Data(Canvas canvas, boolean waxed) {
	}
}
