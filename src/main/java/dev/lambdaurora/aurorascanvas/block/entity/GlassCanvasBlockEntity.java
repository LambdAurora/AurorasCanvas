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
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a glass canvas block entity, stores the pixels of a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class GlassCanvasBlockEntity extends CanvasBlockEntity {

	public GlassCanvasBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	protected @Unmodifiable List<IndexedCanvas.Provider> canvasProviders() {
		return List.of(
				IndexedCanvas.FRONT,
				IndexedCanvas.BACK
		);
	}

	@Override
	public @Unmodifiable Stream<PlacedCanvas> canvases() {
		var state = this.getBlockState();
		var facing = state.getValue(GlassCanvasBlock.FACING);
		boolean pane = state.getValue(GlassCanvasBlock.PANE);

		return Stream.of(
				new PlacedCanvas(this.canvases.get(IndexedCanvas.FRONT.key()).getCanvas(), facing, pane ? .436f : PlacedCanvas.DEFAULT_DEPTH),
				new PlacedCanvas(this.canvases.get(IndexedCanvas.BACK.key()).getCanvas(), facing.getOpposite(), pane ? .436f : -.005f)
		);
	}

	@Override
	public SyncedCanvas getSyncedCanvas(Direction facing) {
		var blockFacing = this.getBlockState().getValue(GlassCanvasBlock.FACING);
		var key = (blockFacing.equals(facing) ? IndexedCanvas.FRONT : IndexedCanvas.BACK).key();

		return this.canvases.get(key).access();
	}
}
