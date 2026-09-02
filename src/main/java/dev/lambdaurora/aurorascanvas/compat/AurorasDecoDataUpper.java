/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasSerialization;
import dev.lambdaurora.aurorascanvas.canvas.holder.GlassCanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;
import dev.lambdaurora.aurorascanvas.util.FabricRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasIds.*;

/**
 * Sets up the backwards compatibility of worlds that used Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoDataUpper {
	private static final Logger LOGGER = LoggerFactory.getLogger(AurorasDecoDataUpper.class);
	public static final String OLD_NAMESPACE = "aurorasdeco";
	public static final Identifier OLD_BLACKBOARD_ID = id(BLACKBOARD_ID.getPath());
	public static final Identifier OLD_WAXED_BLACKBOARD_ID = id(WAXED_BLACKBOARD_ID.getPath());
	public static final Identifier OLD_CHALKBOARD_ID = id(CHALKBOARD_ID.getPath());
	public static final Identifier OLD_WAXED_CHALKBOARD_ID = id(WAXED_CHALKBOARD_ID.getPath());
	public static final Identifier OLD_GLASSBOARD_ID = id(GLASSBOARD_ID.getPath());
	public static final Identifier OLD_WAXED_GLASSBOARD_ID = id(WAXED_GLASSBOARD_ID.getPath());

	public static final Set<String> OLD_BLACKBOARD_IDS = Stream.of(
					OLD_BLACKBOARD_ID,
					OLD_WAXED_BLACKBOARD_ID,
					OLD_CHALKBOARD_ID,
					OLD_WAXED_CHALKBOARD_ID
			)
			.map(Identifier::toString)
			.collect(Collectors.toSet());

	public static final Set<String> OLD_GLASSBOARD_IDS = Stream.of(
					OLD_GLASSBOARD_ID,
					OLD_WAXED_GLASSBOARD_ID
			)
			.map(Identifier::toString)
			.collect(Collectors.toSet());

	public static Identifier id(String path) {
		return new Identifier(OLD_NAMESPACE, path);
	}

	public static void init() {
		addBlockItem(BLACKBOARD_ID);
		addBlockItem(WAXED_BLACKBOARD_ID);
		addBlockItem(CHALKBOARD_ID);
		addBlockItem(WAXED_CHALKBOARD_ID);
		addBlockItem(GLASSBOARD_ID);
		addBlockItem(WAXED_GLASSBOARD_ID);

		((FabricRegistry) BuiltInRegistries.ITEM).aurorascanvas$addAlias(id(PAINTER_PALETTE_ID.getPath()), PAINTER_PALETTE_ID);
	}

	private static void addBlockItem(Identifier newId) {
		final var old = id(newId.getPath());
		((FabricRegistry) BuiltInRegistries.BLOCK).aurorascanvas$addAlias(old, newId);
		((FabricRegistry) BuiltInRegistries.ITEM).aurorascanvas$addAlias(old, newId);
	}

	/**
	 * Fixes the given NBT compound to fit this mod's canvas result format.
	 *
	 * @param nbt the NBT compound to fix
	 * @param source the source of what contains the result that needs to be fixed
	 */
	public static void fixNbt(CompoundTag nbt, FixSource source) {
		if (!nbt.contains("version", CompoundTag.TAG_INT)) {
			nbt.putInt("version", 0);
		}

		var result = CanvasSerialization.CANVAS_CODEC.parse(NbtOps.INSTANCE, nbt)
				.map(SimpleCanvasHolder::new)
				.flatMap(canvas -> SimpleCanvasHolder.CODEC.encodeStart(NbtOps.INSTANCE, canvas))
				.get();

		result.ifLeft(data -> nbt.put("canvas", data));
		result.ifRight(data -> LOGGER.info("Failed to datafix canvas {}: {}", source, data));

		cleanUpAfterFix(nbt);
	}

	/**
	 * Fixes the given NBT compound to fit this mod's glass canvas result format.
	 *
	 * @param nbt the NBT compound to fix
	 * @param source the source of what contains the result that needs to be fixed
	 */
	public static void fixGlassNbt(CompoundTag nbt, FixSource source) {
		if (!nbt.contains("version", CompoundTag.TAG_INT)) {
			nbt.putInt("version", 0);
		}

		var result = CanvasSerialization.CANVAS_CODEC.parse(NbtOps.INSTANCE, nbt)
				.map(canvas -> new GlassCanvasHolder(canvas, new Canvas()))
				.flatMap(canvas -> GlassCanvasHolder.CODEC.encodeStart(NbtOps.INSTANCE, canvas))
				.get();

		result.ifLeft(data -> nbt.put("canvas", data));
		result.ifRight(data -> LOGGER.info("Failed to datafix glass canvas {}: {}", source, data));

		cleanUpAfterFix(nbt);
	}

	private static void cleanUpAfterFix(CompoundTag nbt) {
		nbt.remove("version");
		nbt.remove("pixels");
		nbt.remove("lit");
	}

	public interface FixSource {
		record BlockEntity(String id, BlockPos pos) implements FixSource {}

		record ItemStack(String id) implements FixSource {}
	}
}
