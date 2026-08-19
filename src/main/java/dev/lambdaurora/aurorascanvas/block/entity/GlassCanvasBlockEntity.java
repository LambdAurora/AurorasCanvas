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
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
public class GlassCanvasBlockEntity extends CanvasBlockEntity {
	private final SyncedCanvas front = new SyncedCanvas();
	private final SyncedCanvas back = new SyncedCanvas();

	public GlassCanvasBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public @Unmodifiable Stream<PlacedCanvas> canvases() {
		var state = this.getBlockState();
		var facing = state.getValue(GlassCanvasBlock.FACING);
		boolean pane = state.getValue(GlassCanvasBlock.PANE);

		return Stream.of(
				new PlacedCanvas(this.front.getCanvas(), facing, pane ? .436f : PlacedCanvas.DEFAULT_DEPTH),
				new PlacedCanvas(this.back.getCanvas(), facing.getOpposite(), pane ? .436f : -.005f)
		);
	}

	@Override
	public SyncedCanvas getSyncedCanvas(Direction facing) {
		var blockFacing = this.getBlockState().getValue(GlassCanvasBlock.FACING);

		return (blockFacing.equals(facing) ? this.front : this.back).access();
	}

	/* Serialization */

	@Override
	public void loadCanvasNbt(CompoundTag nbt) {
		if (nbt.contains("pixels")) {
			this.front.setCanvas(Canvas.fromNbt(nbt));
			this.back.getCanvas().clear();
		} else {
			this.front.setCanvas(Canvas.fromNbt(nbt.getCompound("front")));
			this.back.setCanvas(Canvas.fromNbt(nbt.getCompound("back")));
		}
	}

	public CompoundTag writeCanvasNbt(CompoundTag nbt) {
		nbt.put("front", this.front.getCanvas().writeNbt(new CompoundTag()));
		nbt.put("back", this.back.getCanvas().writeNbt(new CompoundTag()));
		return super.writeCanvasNbt(nbt);
	}
}
