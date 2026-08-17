/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.block.entity;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CanvasPressBlockEntity extends BasicBlockEntity {
	public CanvasPressBlockEntity(BlockPos pos, BlockState blockState) {
		super(AurorasCanvasRegistry.BLACKBOARD_PRESS_BLOCK_ENTITY, pos, blockState);
	}
}
