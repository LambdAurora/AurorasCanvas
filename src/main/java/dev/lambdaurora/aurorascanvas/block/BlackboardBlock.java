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
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a blackboard that can be edited by players if not locked.
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
	 * Returns whether this blackboard block is locked or not.
	 *
	 * @return {@code true} if locked, or {@code false} otherwise
	 */
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	protected void appendProperties(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES.get(state.get(FACING));
	}

	/* Placement */

	public boolean isPlacingPreferred(BlockState state, WorldView world, BlockPos pos) {
		return world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isSolid();
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var pos = ctx.getBlockPos();
		var fluidState = ctx.getWorld().getFluidState(pos);
		var state = this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		var world = ctx.getWorld();
		var directions = ctx.getPlacementDirections();

		var nbt = BlockItem.getBlockEntityNbtFromStack(ctx.getStack());
		if (nbt != null && nbt.contains("lit")) {
			state = state.with(LIT, nbt.getBoolean("lit"));
		}

		Direction firstDirection = Direction.NORTH;
		for (var direction : directions) {
			var adjacentState = world.getBlockState(pos.offset(direction));
			if (adjacentState.getBlock() instanceof BlackboardBlock) {
				return state.with(FACING, adjacentState.get(FACING));
			}

			if (direction.getAxis().isHorizontal()) {
				firstDirection = direction;

				var opposite = direction.getOpposite();
				state = state.with(FACING, opposite);

				if (this.isPlacingPreferred(state, world, pos)) {
					return state;
				}
			}
		}

		return state.setValue(FACING, firstDirection);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public void onPlaced(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (stack.hasCustomName()) {
				blackboard.setCustomName(stack.getName());
			}

			var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
			if (state.get(WATERLOGGED) && !this.isLocked())
				return;

			if (nbt != null && Blackboard.shouldConvert(nbt)) {
				var blackboardData = new Blackboard();
				blackboardData.readNbt(nbt);
				blackboard.copy(blackboardData);
			}
		}
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
			LevelAccessor world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		if (!this.isLocked()) {
			var blackboard = this.getBlackboardEntity(world, pos);
			if (blackboard != null && !world.isClientSide()) {
				if (state.get(WATERLOGGED) && !blackboard.isEmpty()) {
					blackboard.clear();
				}
			}
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, Level world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
		var stack = player.getStackInHand(hand);
		var offhand = player.getStackInHand(Hand.OFF_HAND);
		var facing = state.get(FACING);

		if (!this.isLocked() && hit.getSide() == facing) {
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

				var modifier = BlackboardDrawModifier.fromItem(stack);
				if (stack.isOf(Items.WATER_BUCKET) && this.tryClear(world, blackboard, player)) {
					world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
							2.f, 1.f);
					return ActionResult.success(world.isClient());
				} else if (stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER
						&& this.tryClear(world, blackboard, player)) {
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
					if (!player.getAbilities().creativeMode) {
						stack.decrement(1);

						if (stack.isEmpty()) {
							player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
						} else {
							player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
						}
					}
					world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS,
							2.f, 1.f);
					return ActionResult.success(world.isClient());
				} else if (offhand.isOf(Items.STICK) && (modifier != null) && !state.get(WATERLOGGED)) {
					int x;
					int y = (int) (AuroraUtil.posMod(hit.getPos().getY(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 16.0);
					} else {
						x = 15 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 16.0);
					}
					if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

					this.line(blackboard, player, x, y, modifier);

					world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
					return ActionResult.success(world.isClient());
				} else if ((modifier != null) && !state.get(WATERLOGGED)) {
					int x;
					int y = (int) (AuroraUtil.posMod(hit.getPos().getY(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 16.0);
					} else {
						x = 15 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 16.0);
					}
					if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					Blackboard.DrawAction action = Blackboard.DrawAction.DEFAULT;
					for (var possibleAction : Blackboard.DrawAction.ACTIONS) {
						Item offHandTool = possibleAction.getOffHandTool(world.getEnabledFlags());

						if (offHandTool != null && offhand.isOf(offHandTool)) {
							action = possibleAction;
							break;
						}
					}

					if (action.execute(blackboard, x, y, modifier)) {
						player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
						world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
						return ActionResult.success(world.isClient());
					}
				} else if (stack.isOf(Items.GLOW_INK_SAC) || stack.isOf(Items.INK_SAC)) {
					boolean lit = stack.isOf(Items.GLOW_INK_SAC);
					if (lit != state.get(LIT)) {
						if (lit) {
							world.playSound(null, pos, SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS,
									1.f, 1.f);
							world.setBlockState(pos, state.with(LIT, true));
						} else {
							world.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS,
									1.f, 1.f);
							world.setBlockState(pos, state.with(LIT, false));
						}

						if (!player.isCreative()) {
							stack.decrement(1);
						}

						return ActionResult.success(world.isClient());
					}
				}
			}
		}

		return super.onUse(state, world, pos, player, hand, hit);
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

			world.gameEvent(player, GameEvent.BLOCK_CHANGE, blackboard.getPos());
			return true;
		}
		return false;
	}

	@Override
	public void onBreak(Level world, BlockPos pos, BlockState state, Player player) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (!world.isClientSide() && player.isCreative()) {
				var stack = new ItemStack(this);
				var nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
				nbt.remove("custom_name");
				AuroraUtil.writeBlockEntityNbtToStack(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, nbt, false);

				if (blackboard.hasCustomName()) {
					stack.setCustomName(blackboard.getCustomName());
				}

				var itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
				itemEntity.setToDefaultPickupDelay();
				world.addFreshEntity(itemEntity);
			}
		}

		super.onBreak(world, pos, state, player);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		var stack = super.getPickStack(world, pos, state);
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			var nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
			nbt.remove("custom_name");
			AuroraUtil.writeBlockEntityNbtToStack(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, nbt, false);
		}

		return stack;
	}

	/* Block Entity Stuff */

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	public @Nullable BlackboardBlockEntity getBlackboardEntity(BlockView world, BlockPos pos) {
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
	public boolean tryFillWithFluid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			boolean shouldEmitEvent = false;

			var newState = state.setValue(WATERLOGGED, true);

			if (!world.isClientSide()) {
				world.setBlockState(pos, newState, Block.NOTIFY_ALL);
				world.scheduleFluidTick(pos, fluidState.getTags(), fluidState.getType().getTickDelay(world));

				var blackboard = this.getBlackboardEntity(world, pos);
				if (blackboard != null && !this.isLocked()) {
					if (!blackboard.isEmpty()) {
						blackboard.clear();
						shouldEmitEvent = true;
					}
				}
			}

			if (shouldEmitEvent) {
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.create(newState));
			}

			return true;
		} else {
			return false;
		}
	}

	static {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
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
		}

		var builder = ImmutableMap.<Direction, VoxelShape>builder();

		builder.put(Direction.NORTH, createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
		builder.put(Direction.EAST, createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
		builder.put(Direction.SOUTH, createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
		builder.put(Direction.WEST, createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

		SHAPES = new EnumMap<>(builder.build());
	}
}
