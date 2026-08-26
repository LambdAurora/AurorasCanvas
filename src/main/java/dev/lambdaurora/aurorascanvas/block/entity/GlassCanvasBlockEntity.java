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
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.GlassCanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.GlassCanvasLikeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Unmodifiable;

import java.util.stream.Stream;

/**
 * Represents a glass canvas block entity, stores the pixels of a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class GlassCanvasBlockEntity extends CanvasBlockEntity<GlassCanvasHolder, GlassCanvasLikeHolder<CanvasBlockEntity.SyncedCanvas>> {

	public GlassCanvasBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	protected CanvasHolder.Type<GlassCanvasHolder> canvasType() {
		return GlassCanvasHolder.TYPE;
	}

	@Override
	public @Unmodifiable Stream<PlacedCanvas> canvases() {
		var state = this.getBlockState();
		var facing = state.getValue(GlassCanvasBlock.FACING);
		boolean pane = state.getValue(GlassCanvasBlock.PANE);

		return Stream.of(
				new PlacedCanvas(this.canvases.front().getCanvas(), facing, pane ? .436f : PlacedCanvas.DEFAULT_DEPTH),
				new PlacedCanvas(this.canvases.back().getCanvas(), facing.getOpposite(), pane ? .436f : -.005f)
		);
	}

	@Override
	public SyncedCanvas getSyncedCanvas(Direction facing) {
		var blockFacing = this.getBlockState().getValue(GlassCanvasBlock.FACING);
		return blockFacing.equals(facing) ? this.canvases.front() : this.canvases.back();
	}
}
