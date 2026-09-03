/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.Map;

/// Provides custom block state definitions for use in block model loading.
///
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.2.0
/// @see net.minecraft.client.resources.model.BlockStateDefinitions
/// @see dev.lambdaurora.aurorascanvas.client.mixin.BlockStateDefinitionsMixin
@Environment(EnvType.CLIENT)
public final class AurorasCanvasBlockStateDefinitions {
	public static final StateDefinition<Block, BlockState> CANVAS_PRESS_PRESS_PLATE_FAKE_STATE = createCanvasPressFakeState();
	public static final StateDefinition<Block, BlockState> CANVAS_PRESS_SCREW_FAKE_STATE = createCanvasPressFakeState();
	private static final Identifier CANVAS_PRESS_PRESS_PLATE_ID = AurorasCanvas.id("canvas_press/press_plate");
	private static final Identifier CANVAS_PRESS_SCREW_ID = AurorasCanvas.id("canvas_press/screw");
	public static final Map<Identifier, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = Map.of(
			CANVAS_PRESS_PRESS_PLATE_ID, CANVAS_PRESS_PRESS_PLATE_FAKE_STATE,
			CANVAS_PRESS_SCREW_ID, CANVAS_PRESS_SCREW_FAKE_STATE
	);

	private static StateDefinition<Block, BlockState> createCanvasPressFakeState() {
		var existing = AurorasCanvasRegistry.CANVAS_PRESS.block().value().getStateDefinition();
		var builder = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR);
		existing.getProperties().forEach(builder::add);
		return builder.create(Block::defaultBlockState, BlockState::new);
	}

	private AurorasCanvasBlockStateDefinitions() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
