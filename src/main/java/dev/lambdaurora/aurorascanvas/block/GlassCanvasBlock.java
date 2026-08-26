/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.block.entity.GlassCanvasBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a glass canvas.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class GlassCanvasBlock extends CanvasBlock {
	public static final MapCodec<GlassCanvasBlock> CODEC = makeCodec(GlassCanvasBlock::new);

	public static final BooleanProperty PANE = BooleanProperty.create("pane");
	private static final Map<Direction, VoxelShape> SHAPES;

	public GlassCanvasBlock(Properties settings, boolean locked) {
		super(settings, locked);

		this.registerDefaultState(this.defaultBlockState()
				.setValue(PANE, false)
		);
	}

	@Override
	protected MapCodec<? extends CanvasBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(PANE);
	}

	/* Shapes */

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!state.getValue(PANE)) {
			return super.getShape(state, world, pos, context);
		} else {
			return SHAPES.get(state.getValue(FACING));
		}
	}

	/* Placement */

	@Override
	public boolean isPlacingPreferred(BlockState state, LevelReader world, BlockPos pos) {
		var neighbor = world.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
		return !(neighbor.getBlock() instanceof IronBarsBlock) && neighbor.isSolid();
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var world = ctx.getLevel();
		var pos = ctx.getClickedPos();
		var fluidState = world.getFluidState(pos);
		var state = this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
		var directions = ctx.getNearestLookingDirections();

		Direction firstDirection = Direction.NORTH;
		for (var direction : directions) {
			var adjacentState = world.getBlockState(pos.relative(direction));
			if (adjacentState.getBlock() instanceof GlassCanvasBlock) {
				return state.setValue(FACING, adjacentState.getValue(FACING)).setValue(PANE, adjacentState.getValue(PANE));
			} else if (adjacentState.getBlock() instanceof CanvasBlock) {
				return state.setValue(FACING, adjacentState.getValue(FACING));
			}

			if (direction.getAxis().isHorizontal()) {
				firstDirection = direction;

				var opposite = direction.getOpposite();
				state = state.setValue(FACING, opposite);

				if (this.isPlacingPreferred(state, world, pos)) {
					return state;
				}
			}
		}

		return state.setValue(FACING, firstDirection).setValue(PANE, ctx.getClickedFace().getAxis().isVertical());
	}

	/* Interaction */

	@Override
	protected boolean isUseFaceValid(BlockState state, Direction direction) {
		var facing = state.getValue(FACING);
		return direction.equals(facing) || direction.getOpposite().equals(facing);
	}

	/* Block Entity Stuff */

	@Override
	protected BlockEntityType<? extends GlassCanvasBlockEntity> getBlockEntityType() {
		return AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE;
	}

	static {
		var builder = ImmutableMap.<Direction, VoxelShape>builder();

		var xAxis = box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0);
		var zAxis = box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0);
		builder.put(Direction.NORTH, zAxis);
		builder.put(Direction.EAST, xAxis);
		builder.put(Direction.SOUTH, zAxis);
		builder.put(Direction.WEST, xAxis);

		SHAPES = new EnumMap<>(builder.build());
	}
}
