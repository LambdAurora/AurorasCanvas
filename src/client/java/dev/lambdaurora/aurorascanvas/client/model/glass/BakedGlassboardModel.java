/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model.glass;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.client.model.BakedCanvasModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class BakedGlassboardModel extends BakedCanvasModel {
	private final Int2ObjectMap<FabricBlockStateModel> models;

	public BakedGlassboardModel(BlockStateModel baseModel, Int2ObjectMap<FabricBlockStateModel> models) {
		super(baseModel);
		this.models = models;
	}

	private int isBlockSame(BlockAndTintGetter world, BlockPos pos, BlockState state, int mask) {
		BlockState neighborState = world.getBlockState(pos);
		if (neighborState.is(state.getBlock())) {
			if (neighborState.getValue(GlassCanvasBlock.FACING) == state.getValue(GlassCanvasBlock.FACING)
					&& neighborState.getValue(GlassCanvasBlock.PANE) == state.getValue(GlassCanvasBlock.PANE)
			) {
				return mask;
			}
		}

		return 0;
	}

	@Override
	public void emitQuads(
			QuadEmitter emitter, BlockAndTintGetter world, BlockPos pos, BlockState state,
			RandomSource random, Predicate<@Nullable Direction> cullTest
	) {
		var facing = state.getValue(GlassCanvasBlock.FACING);
		boolean pane = state.getValue(GlassCanvasBlock.PANE);
		int mask = 0;
		BlockPos.MutableBlockPos neighborPos = pos.mutable();

		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.LEFT_MASK);
		this.move(neighborPos, facing, Direction.UP);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.LEFT_UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.RIGHT_UP_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.RIGHT_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.RIGHT_DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, GlassboardModel.LEFT_DOWN_MASK);

		final int fixedMask = mask;

		emitter.pushTransform(quad -> {
			var cullFace = quad.cullFace();
			if (cullFace != null) {
				var adjacentPos = pos.relative(cullFace);
				var adjacentState = world.getBlockState(adjacentPos);

				if (!adjacentState.is(AurorasCanvasRegistry.GLASSBOARD_BLOCKS)) {
					return !(pane && adjacentState.is(Blocks.GLASS_PANE));
				}

				return adjacentState.getBlock() instanceof GlassCanvasBlock && pane != adjacentState.getValue(GlassCanvasBlock.PANE); // Force the culling.
			}

			return true;
		});

		this.models.get(fixedMask).emitQuads(emitter, world, pos, state, random, cullTest);

		emitter.popTransform();

		this.emitBlockMesh(world, pos, emitter);

		emitter.pushTransform(quad -> {
			quad.nominalFace(quad.lightFace().getClockWise());
			Direction direction = quad.lightFace();
			var quadPos = new Vector3f();

			float leftValue;
			float rightValue;

			if (direction.getAxis() == Direction.Axis.Z) {
				quad.copyPos(0, quadPos);
				leftValue = quadPos.x();
				quad.copyPos(2, quadPos);
				rightValue = quadPos.x();
			} else if (direction.getAxis() == Direction.Axis.X) {
				quad.copyPos(0, quadPos);
				leftValue = quadPos.z();
				quad.copyPos(2, quadPos);
				rightValue = quadPos.z();
			} else {
				leftValue = rightValue = 0;
			}

			for (int i = 0; i < 4; i++) {
				quad.copyPos(i, quadPos);

				if (direction.getAxis() == Direction.Axis.Z) {
					quad.pos(i, i < 2 ? rightValue : leftValue, quadPos.y(), quadPos.z());
				} else if (direction.getAxis() == Direction.Axis.X) {
					quad.pos(i, quadPos.x(), quadPos.y(), i < 2 ? rightValue : leftValue);
				}
			}
			return true;
		});

		this.emitBlockMesh(world, pos, emitter);
		emitter.popTransform();
	}

	private void move(BlockPos.MutableBlockPos pos, Direction facing, Direction direction) {
		if (facing.getAxis().isHorizontal()) {
			if (direction.getAxis().isHorizontal()) {
				pos.move(direction == Direction.WEST ? facing.getClockWise() : facing.getCounterClockWise());
			} else {
				pos.move(direction);
			}
		} else {
			pos.move(switch (direction) {
				case UP -> Direction.NORTH;
				case DOWN -> Direction.SOUTH;
				default -> direction;
			});
		}
	}

	public record Part(List<BlockStateModelPart> contents) implements FabricBlockStateModel {
		@Override
		public void emitQuads(
				QuadEmitter emitter, BlockAndTintGetter world, BlockPos pos, BlockState state,
				RandomSource random, Predicate<@Nullable Direction> cullTest
		) {
			this.contents.forEach(model -> model.emitQuads(emitter, cullTest));
		}
	}
}
