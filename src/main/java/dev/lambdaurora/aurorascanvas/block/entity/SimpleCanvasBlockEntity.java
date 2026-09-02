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
import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasLikeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a canvas block entity, stores the pixels of a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleCanvasBlockEntity extends CanvasBlockEntity<Direction, SimpleCanvasHolder, SimpleCanvasLikeHolder<CanvasBlockEntity.SyncedCanvas>> {
	public SimpleCanvasBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	protected CanvasHolder.Type<SimpleCanvasHolder> canvasType() {
		return SimpleCanvasHolder.TYPE;
	}

	@Override
	protected Direction getPlacementData() {
		return this.getBlockState().getValue(CanvasBlock.FACING);
	}

	@Override
	public SyncedCanvas getSyncedCanvas(Direction facing) {
		return this.canvases.getDefault();
	}
}
