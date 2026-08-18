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
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Unmodifiable;

import java.util.stream.Stream;

/**
 * Represents a canvas block entity, stores the pixels of a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleCanvasBlockEntity extends CanvasBlockEntity {
	private final SyncedCanvas syncedHandler = new SyncedCanvas();

	public SimpleCanvasBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public @Unmodifiable Stream<PlacedCanvas> canvases() {
		return Stream.of(new PlacedCanvas(this.syncedHandler.getCanvas(), this.getBlockState().getValue(CanvasBlock.FACING)));
	}

	@Override
	public SyncedCanvas getSyncedCanvas(Direction facing) {
		return this.syncedHandler.access();
	}

	/* Serialization */

	@Override
	public void loadCanvasNbt(CompoundTag nbt) {
		this.syncedHandler.setCanvas(Canvas.fromNbt(nbt));
	}

	@Override
	public CompoundTag writeCanvasNbt(CompoundTag nbt) {
		this.syncedHandler.getCanvas().writeNbt(nbt);
		return super.writeCanvasNbt(nbt);
	}
}
