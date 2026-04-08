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
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a canvas that can be edited by players if not locked.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BlackboardBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final Map<Direction, VoxelShape> SHAPES;

	private final boolean locked;

	public BlackboardBlock(Properties settings, boolean locked) {
		super(settings);
		this.locked = locked;

		this.registerDefaultState(this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(LIT, false)
				.setValue(WATERLOGGED, false)
		);
	}

	/**
	 * Returns whether this canvas block is locked or not.
	 *
	 * @return {@code true} if locked, or {@code false} otherwise
	 */
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	/* Placement */

	public boolean isPlacingPreferred(BlockState state, LevelReader world, BlockPos pos) {
		return world.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isSolid();
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var world = ctx.getLevel();
		var pos = ctx.getClickedPos();
		var fluidState = world.getFluidState(pos);
		var state = this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
		var directions = ctx.getNearestLookingDirections();

		var nbt = BlockItem.getBlockEntityData(ctx.getItemInHand());
		if (nbt != null && nbt.contains("lit")) {
			state = state.setValue(LIT, nbt.getBoolean("lit"));
		}

		Direction firstDirection = Direction.NORTH;
		for (var direction : directions) {
			var adjacentState = world.getBlockState(pos.relative(direction));
			if (adjacentState.getBlock() instanceof BlackboardBlock) {
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

		return state.setValue(FACING, firstDirection);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (stack.hasCustomHoverName()) {
				blackboard.setCustomName(stack.getHoverName());
			}

			var nbt = BlockItem.getBlockEntityData(stack);
			if (state.getValue(WATERLOGGED) && !this.isLocked())
				return;

			if (nbt != null && Canvas.shouldConvert(nbt)) {
				var blackboardData = new Canvas();
				blackboardData.readNbt(nbt);
				blackboard.copy(blackboardData);
			}
		}
	}

	/* Updates */

	@Override
	public BlockState updateShape(
			BlockState state, Direction direction, BlockState newState,
			LevelAccessor world, BlockPos pos, BlockPos posFrom
	) {
		if (state.getValue(WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		if (!this.isLocked()) {
			var blackboard = this.getBlackboardEntity(world, pos);
			if (blackboard != null && !world.isClientSide()) {
				if (state.getValue(WATERLOGGED) && !blackboard.isEmpty()) {
					blackboard.clear();
				}
			}
		}

		return super.updateShape(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		var stack = player.getItemInHand(hand);
		var offhand = player.getItemInHand(InteractionHand.OFF_HAND);
		var facing = state.getValue(FACING);

		if (!this.isLocked() && hit.getDirection() == facing) {
			var blackboard = this.getBlackboardEntity(world, pos);
			if (blackboard != null) {
				if (blackboard.lastUser != null && blackboard.lastUser.isRemoved()) {
					blackboard.lastUser = null;
				}

				if (stack.getItem() instanceof PainterPaletteItem paletteItem) {
					if (offhand.isEmpty()) {
						offhand = paletteItem.getCurrentToolAsItem(stack);
					}

					stack = paletteItem.getCurrentColorAsItem(stack);
				}

				var modifier = DrawModifier.fromItem(stack);
				if (stack.is(Items.WATER_BUCKET) && this.tryClear(world, blackboard, player)) {
					world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 2.f, 1.f);
					return InteractionResult.sidedSuccess(world.isClientSide());
				} else if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER && this.tryClear(world, blackboard, player)) {
					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);

						if (stack.isEmpty()) {
							player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
						} else {
							player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
						}
					}
					world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 2.f, 1.f);
					return InteractionResult.sidedSuccess(world.isClientSide());
				} else if (offhand.is(Items.STICK) && (modifier != null) && !state.getValue(WATERLOGGED)) {
					int x;
					int y = (int) (Utils.posMod(hit.getLocation().y(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (Utils.posMod(hit.getLocation().x(), 1) * 16.0);
					} else {
						x = 15 - (int) (Utils.posMod(hit.getLocation().z(), 1) * 16.0);
					}
					if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

					this.line(blackboard, player, x, y, modifier);

					world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
					return InteractionResult.sidedSuccess(world.isClientSide());
				} else if ((modifier != null) && !state.getValue(WATERLOGGED)) {
					int x;
					int y = (int) (Utils.posMod(hit.getLocation().y(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (Utils.posMod(hit.getLocation().x(), 1) * 16.0);
					} else {
						x = 15 - (int) (Utils.posMod(hit.getLocation().z(), 1) * 16.0);
					}
					if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					Canvas.DrawAction action = Canvas.DrawAction.DEFAULT;
					for (var possibleAction : Canvas.DrawAction.ACTIONS) {
						Item offHandTool = possibleAction.getOffHandTool(world.enabledFeatures());

						if (offHandTool != null && offhand.is(offHandTool)) {
							action = possibleAction;
							break;
						}
					}

					if (action.execute(blackboard, x, y, modifier)) {
						player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
						world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
						return InteractionResult.sidedSuccess(world.isClientSide());
					}
				} else if (stack.is(Items.GLOW_INK_SAC) || stack.is(Items.INK_SAC)) {
					boolean lit = stack.is(Items.GLOW_INK_SAC);
					if (lit != state.getValue(LIT)) {
						if (lit) {
							world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.f, 1.f);
							world.setBlockAndUpdate(pos, state.setValue(LIT, true));
						} else {
							world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.f, 1.f);
							world.setBlockAndUpdate(pos, state.setValue(LIT, false));
						}

						if (!player.isCreative()) {
							stack.shrink(1);
						}

						return InteractionResult.sidedSuccess(world.isClientSide());
					}
				}
			}
		}

		return super.use(state, world, pos, player, hand, hit);
	}

	private void line(BlackboardBlockEntity blackboard, Player player, int x, int y, DrawModifier modifier) {
		if (blackboard.lastUser != player) {
			blackboard.lastUser = player;
			blackboard.lastX = x;
			blackboard.lastY = y;
		} else {
			blackboard.line(blackboard.lastX, blackboard.lastY, x, y, modifier);
			blackboard.lastUser = null;
		}
	}

	private boolean tryClear(Level world, BlackboardBlockEntity blackboard, @Nullable Player player) {
		if (!blackboard.isEmpty()) {
			blackboard.clear();

			world.gameEvent(player, GameEvent.BLOCK_CHANGE, blackboard.getBlockPos());
			return true;
		}
		return false;
	}

	@Override
	public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (!world.isClientSide() && player.isCreative()) {
				var stack = new ItemStack(this);
				var nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
				nbt.remove("custom_name");
				Utils.writeBlockEntityNbtToStack(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, nbt, false);

				if (blackboard.hasCustomName()) {
					stack.setHoverName(blackboard.getCustomName());
				}

				var itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
				itemEntity.setDefaultPickUpDelay();
				world.addFreshEntity(itemEntity);
			}
		}

		super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		var stack = super.getCloneItemStack(world, pos, state);
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			var nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
			nbt.remove("custom_name");
			Utils.writeBlockEntityNbtToStack(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, nbt, false);
		}

		return stack;
	}

	/* Block Entity Stuff */

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.create(pos, state);
	}

	public @Nullable BlackboardBlockEntity getBlackboardEntity(BlockGetter world, BlockPos pos) {
		var entity = world.getBlockEntity(pos);
		if (entity instanceof BlackboardBlockEntity blackboard)
			return blackboard;
		return null;
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			boolean shouldEmitEvent = false;

			var newState = state.setValue(WATERLOGGED, true);

			if (!world.isClientSide()) {
				world.setBlock(pos, newState, Block.UPDATE_ALL);
				world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));

				var blackboard = this.getBlackboardEntity(world, pos);
				if (blackboard != null && !this.isLocked()) {
					if (!blackboard.isEmpty()) {
						blackboard.clear();
						shouldEmitEvent = true;
					}
				}
			}

			if (shouldEmitEvent) {
				world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
			}

			return true;
		} else {
			return false;
		}
	}

	static {
		/*if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			UseItemCallback.EVENT.register((player, world, hand) -> {
				if (hand == Hand.OFF_HAND && !player.isSpectator()) {
					var target = MinecraftClient.getInstance().crosshairTarget;
					if (target != null && target.getType() == HitResult.Type.BLOCK) {
						var targetBlock = world.getBlockState(((BlockHitResult) target).getBlockPos());
						if (targetBlock.getBlock() instanceof BlackboardBlock)
							return TypedActionResult.fail(ItemStack.EMPTY);
					}
				}

				return TypedActionResult.pass(ItemStack.EMPTY);
			});
		}*/

		var builder = ImmutableMap.<Direction, VoxelShape>builder();

		builder.put(Direction.NORTH, box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
		builder.put(Direction.EAST, box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
		builder.put(Direction.SOUTH, box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
		builder.put(Direction.WEST, box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

		SHAPES = new EnumMap<>(builder.build());
	}
}
