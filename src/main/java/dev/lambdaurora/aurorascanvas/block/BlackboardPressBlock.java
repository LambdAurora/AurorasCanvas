package dev.lambdaurora.aurorascanvas.block;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.entity.BlackboardPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Represents a blackboard press block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardPressBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape SHAPE = box(0, 0, 0, 1, 9 / 16., 1);
	private static final VoxelShape BASE_SHAPE = Shapes.or(
			box(0, 0, 0, 1, 2 / 16., 1),
			box(1 / 16., 2 / 16., 1 / 16., 15 / 16., 3 / 16., 15 / 16.)
	);
	private static final VoxelShape X_SHAPE = Shapes.or(
			BASE_SHAPE,
			box(4 / 16., 2 / 16., 0, 12 / 16., 7 / 16., 1 / 16.),
			box(4 / 16., 2 / 16., 15 / 16., 12 / 16., 7 / 16., 1),
			box(4 / 16., 7 / 16., 0, 12 / 16., 9 / 16., 1)
	);
	private static final VoxelShape Z_SHAPE = Shapes.or(
			BASE_SHAPE,
			box(0, 2 / 16., 4 / 16., 1 / 16., 7 / 16., 12 / 16.),
			box(15 / 16., 2 / 16., 4 / 16., 1, 7 / 16., 12 / 16.),
			box(0, 7 / 16., 4 / 16., 1, 9 / 16., 12 / 16.)
	);

	public BlackboardPressBlock(Properties properties) {
		super(properties);

		this.registerDefaultState(
				this.defaultBlockState()
						.setValue(FACING, Direction.NORTH)
						.setValue(WATERLOGGED, false)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING).getAxis()) {
			case X -> X_SHAPE;
			case Z -> Z_SHAPE;
			default -> SHAPE; // Why..?
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/* Placement */

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var state = super.getStateForPlacement(ctx);
		if (state != null)
			return state.setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
		return null;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/* Block Entity Stuff */

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AurorasCanvasRegistry.BLACKBOARD_PRESS_BLOCK_ENTITY.create(pos, state);
	}

	public @Nullable BlackboardPressBlockEntity getBlackboardPressEntity(BlockGetter world, BlockPos pos) {
		var entity = world.getBlockEntity(pos);
		if (entity instanceof BlackboardPressBlockEntity blackboardPress)
			return blackboardPress;
		return null;
	}

	/* Fluid */

	/* Entity Stuff */

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
		return switch (type) {
			case LAND, AIR -> false;
			case WATER -> world.getFluidState(pos).is(FluidTags.WATER);
		};
	}
}
