/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Provides serialization of canvases.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class CanvasSerialization {
	public static int MAX_PIXELS_BYTES = 2 * CanvasHandler.PIXELS_COUNT;
	private static final int CURRENT_VERSION = 2;

	private static final Codec<RawCanvas> RAW_CANVAS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.INT.fieldOf("version").forGetter(RawCanvas::version),
					Utils.BYTE_ARRAY_CODEC.optionalFieldOf("pixels", new byte[0]).forGetter(RawCanvas::pixels),
					Codec.BOOL.optionalFieldOf("lit", false).forGetter(RawCanvas::glowing)
			).apply(instance, RawCanvas::new)
	);
	public static final Codec<Canvas> CANVAS_CODEC = RAW_CANVAS_CODEC.xmap(
			raw -> {
				byte[] data = switch (raw.version) {
					case 0 -> convertV0(raw.pixels);
					case 1 -> convertV1(raw.pixels);
					default -> raw.pixels;
				};

				var pixels = new short[CanvasHandler.PIXELS_COUNT];

				readPixels(data, pixels);

				return new Canvas(pixels, raw.glowing);
			},
			canvas -> new RawCanvas(CURRENT_VERSION, serializePixels(canvas.getPixels()), canvas.isGlowing())
	);

	public static final StreamCodec<ByteBuf, Canvas> CANVAS_STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.byteArray(MAX_PIXELS_BYTES), canvas -> serializePixels(canvas.getPixels()),
			ByteBufCodecs.BOOL, Canvas::isGlowing,
			(data, glowing) -> {
				var canvas = new Canvas();
				readPixels(data, canvas.getPixels());
				canvas.setGlowing(glowing);
				return canvas;
			}
	);

	public static Canvas fromByteBuf(FriendlyByteBuf buffer) {
		var canvas = new Canvas();
		byte[] data = buffer.readByteArray(MAX_PIXELS_BYTES);
		readPixels(data, canvas.getPixels());
		canvas.setGlowing(buffer.readBoolean());
		return canvas;
	}

	public static void writeCanvasToBuffer(FriendlyByteBuf buffer, Canvas canvas) {
		var data = serializePixels(canvas.getPixels());
		buffer.writeByteArray(data);
		buffer.writeBoolean(canvas.isGlowing());
	}

	/**
	 * Serializes the given canvas pixels to raw data.
	 *
	 * @param pixels the pixels to serialize
	 * @return the serialized pixels
	 */
	private static byte[] serializePixels(short[] pixels) {
		boolean isWorthIt = false;

		int length = 0;
		for (short pixel : pixels) {
			if (pixel == 0) length++;
			else {
				isWorthIt = true;
				length += 2;
			}
		}

		if (!isWorthIt) {
			return new byte[0];
		}

		var data = new byte[length];

		int rawIndex = 0;
		for (short pixel : pixels) {
			if (pixel == 0) {
				data[rawIndex++] = 0;
			} else {
				data[rawIndex] = (byte) (pixel >> 8);
				data[rawIndex + 1] = (byte) (pixel & 0xff);
				rawIndex += 2;
			}
		}

		return data;
	}

	/**
	 * Deserializes the given pixels.
	 *
	 * @param data the pixels serialized data
	 * @return the deserialized pixels
	 */
	private static short[] deserializePixels(byte[] data) {
		var pixels = new short[CanvasHandler.PIXELS_COUNT];
		readPixels(data, pixels);
		return pixels;
	}

	private static void readPixels(byte[] data, short[] pixels) {
		CanvasHandler.checkPixels(pixels);

		int boardIndex = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] == 0) {
				pixels[boardIndex] = 0;
			} else {
				pixels[boardIndex] = (short) (data[i] << 8 | data[++i] & 0xff);
			}

			boardIndex++;
			if (boardIndex >= pixels.length) break;
		}
	}

	/**
	 * Converts the raw pixel data from version 0 to version 1.
	 *
	 * @param pixels the raw pixel data
	 * @return the converted raw pixel data
	 */
	private static byte[] convertV0(byte[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] *= 4;
		}

		return convertV1(pixels);
	}

	/**
	 * Converts the raw pixel data from version 1 to version 2.
	 *
	 * @param pixels the raw pixel data
	 * @return the converted raw pixel data
	 */
	private static byte[] convertV1(byte[] pixels) {
		var converted = new byte[256 * 2];

		int newIndex = 0;
		for (byte pixel : pixels) {
			if (pixel == 0) {
				converted[newIndex] = 0;
				newIndex++;
			} else {
				converted[newIndex] = (byte) ((pixel & 0b11111100) >> 2);
				converted[newIndex + 1] = (byte) ((pixel & 0b11) << 4);
				newIndex += 2;
			}
		}

		return converted;
	}

	private record RawCanvas(int version, byte[] pixels, boolean glowing) {
	}
}
