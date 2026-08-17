/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedGlassboardModel extends BakedCanvasModel {
	private final Int2ObjectMap<List<BakedModel>> models;

	public BakedGlassboardModel(BakedModel baseModel, Int2ObjectMap<List<BakedModel>> models) {
		super(baseModel);
		this.models = models;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	private int isBlockSame(BlockAndTintGetter world, BlockPos pos, BlockState state, int mask) {
		BlockState neighborState = world.getBlockState(pos);
		if (neighborState.is(state.getBlock())) {
			if (neighborState.getValue(CanvasBlock.FACING) == state.getValue(CanvasBlock.FACING)) {
				return mask;
			}
		}

		return 0;
	}

	@Override
	public void emitBlockQuads(
			BlockAndTintGetter world, BlockState state, BlockPos pos,
			Supplier<RandomSource> randomSupplier, RenderContext context
	) {
		var facing = state.getValue(CanvasBlock.FACING);
		int mask = 0;
		BlockPos.MutableBlockPos neighborPos = pos.mutable();

		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.LEFT_MASK);
		this.move(neighborPos, facing, Direction.UP);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.LEFT_UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.RIGHT_UP_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.RIGHT_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.RIGHT_DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(world, neighborPos, state, UnbakedGlassboardModel.LEFT_DOWN_MASK);

		final int fixedMask = mask;

		context.pushTransform(quad -> {
			var cullFace = quad.cullFace();
			if (cullFace != null) {
				var adjacentPos = pos.relative(cullFace);
				return !world.getBlockState(adjacentPos).is(AurorasCanvasRegistry.GLASSBOARD_BLOCKS); // Force the culling.
			}

			return true;
		});

		this.models.get(fixedMask).forEach(model -> model.emitBlockQuads(world, state, pos, randomSupplier, context));

		context.popTransform();

		this.emitBlockMesh(world, pos, context);

		context.pushTransform(quad -> {
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

		this.emitBlockMesh(world, pos, context);
		context.popTransform();
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
}
