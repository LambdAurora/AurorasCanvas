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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import java.util.function.BiFunction;

/**
 * Represents a canvas that can be edited by players if not locked.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class CanvasBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<? extends CanvasBlock> CODEC = makeCodec(CanvasBlock::new);

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	static <W extends CanvasBlock> MapCodec<W> makeCodec(BiFunction<Properties, Boolean, W> instantiator) {
		return RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								propertiesCodec(),
								Codec.BOOL.fieldOf("locked").forGetter(CanvasBlock::isLocked)
						)
						.apply(instance, instantiator)
		);
	}

	private static final Map<Direction, VoxelShape> SHAPES;

	private final boolean locked;

	public CanvasBlock(Properties settings, boolean locked) {
		super(settings);
		this.locked = locked;

		this.registerDefaultState(this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, false)
		);
	}

	@Override
	protected MapCodec<? extends CanvasBlock> codec() {
		return CODEC;
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
		builder.add(FACING, WATERLOGGED);
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

		Direction firstDirection = Direction.NORTH;
		for (var direction : directions) {
			var adjacentState = world.getBlockState(pos.relative(direction));
			if (adjacentState.getBlock() instanceof CanvasBlock) {
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
		var blackboard = this.getCanvasEntity(world, pos);
		if (blackboard != null) {
			if (stack.has(DataComponents.CUSTOM_NAME)) {
				blackboard.setCustomName(stack.getHoverName());
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
			var blackboard = this.getCanvasEntity(world, pos);
			if (blackboard != null && !world.isClientSide()) {
				if (state.getValue(WATERLOGGED) && !blackboard.isEmpty()) {
					blackboard.clear();
				}
			}
		}

		return super.updateShape(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	protected boolean isUseFaceValid(BlockState state, Direction direction) {
		return direction.equals(state.getValue(FACING));
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		var offhand = player.getItemInHand(InteractionHand.OFF_HAND);
		var facing = hit.getDirection();

		if (!this.isLocked() && this.isUseFaceValid(state, facing)) {
			var canvasEntity = this.getCanvasEntity(world, pos);
			if (canvasEntity != null) {
				var syncedCanvas = canvasEntity.getSyncedCanvas(facing);

				var currentStack = stack;
				var paletteInventory = stack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);

				if (paletteInventory != null) {
					if (offhand.isEmpty()) {
						offhand = paletteInventory.getSelectedTool();
					}

					currentStack = paletteInventory.getSelectedColor();
				}

				var modifier = DrawModifier.fromItem(currentStack);
				if (currentStack.is(Items.WATER_BUCKET) && this.tryClear(world, canvasEntity, player)) {
					world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 2.f, 1.f);
					return ItemInteractionResult.sidedSuccess(world.isClientSide());
				} else if (this.isPotionWater(currentStack) && this.tryClear(world, canvasEntity, player)) {
					player.awardStat(Stats.ITEM_USED.get(currentStack.getItem()));
					if (!player.getAbilities().instabuild) {
						currentStack.shrink(1);

						if (currentStack.isEmpty()) {
							player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
						} else {
							player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
						}
					}
					world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 2.f, 1.f);
					return ItemInteractionResult.sidedSuccess(world.isClientSide());
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

					if (syncedCanvas.tryDrawLine(player, x, y, modifier)) {
						if (player instanceof ServerPlayer serverPlayer) {
							AurorasCanvasRegistry.DRAW_ON_CANVAS_TRIGGER.trigger(serverPlayer, stack);
						}

						player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
						world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
					}

					return ItemInteractionResult.sidedSuccess(world.isClientSide());
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

					DrawAction action = DrawAction.DEFAULT;
					for (var possibleAction : DrawAction.ACTIONS) {
						Item offHandTool = possibleAction.getOffHandTool(world.enabledFeatures());

						if (offHandTool != null && offhand.is(offHandTool)) {
							action = possibleAction;
							break;
						}
					}

					if (action.execute(syncedCanvas, x, y, modifier)) {
						if (player instanceof ServerPlayer serverPlayer) {
							AurorasCanvasRegistry.DRAW_ON_CANVAS_TRIGGER.trigger(serverPlayer, stack);
						}

						player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
						world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
						return ItemInteractionResult.sidedSuccess(world.isClientSide());
					}
				} else if (currentStack.is(Items.GLOW_INK_SAC) || currentStack.is(Items.INK_SAC)) {
					boolean lit = currentStack.is(Items.GLOW_INK_SAC);
					if (lit != syncedCanvas.isGlowing()) {
						if (lit) {
							world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.f, 1.f);
							syncedCanvas.setGlowing(true);
						} else {
							world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.f, 1.f);
							syncedCanvas.setGlowing(false);
						}

						if (!player.isCreative()) {
							currentStack.shrink(1);
						}

						return ItemInteractionResult.sidedSuccess(world.isClientSide());
					}
				}
			}
		}

		return super.useItemOn(stack, state, world, pos, player, hand, hit);
	}

	private boolean isPotionWater(ItemStack stack) {
		var contents = stack.get(DataComponents.POTION_CONTENTS);

		if (contents != null) {
			return contents.is(Potions.WATER);
		}

		return false;
	}

	private boolean tryClear(Level world, CanvasBlockEntity blackboard, @Nullable Player player) {
		if (!blackboard.isEmpty()) {
			blackboard.clear();

			world.gameEvent(player, GameEvent.BLOCK_CHANGE, blackboard.getBlockPos());
			return true;
		}
		return false;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		var canvasEntity = this.getCanvasEntity(level, pos);
		if (canvasEntity != null) {
			if (!level.isClientSide() && player.isCreative()) {
				this.playerDestroy(level, player, pos, state, canvasEntity, player.getMainHandItem().copy());
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		var stack = super.getCloneItemStack(level, pos, state);
		var canvasEntity = this.getCanvasEntity(level, pos);
		if (canvasEntity != null && !canvasEntity.isEmpty()) {
			var nbt = canvasEntity.writeCanvasNbt(new CompoundTag());
			nbt.remove("custom_name");
			Utils.writeBlockEntityNbtToStack(stack, this.getBlockEntityType(), nbt, false);
		}

		return stack;
	}

	/* Block Entity Stuff */

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	protected BlockEntityType<? extends CanvasBlockEntity> getBlockEntityType() {
		return AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.getBlockEntityType().create(pos, state);
	}

	public @Nullable CanvasBlockEntity getCanvasEntity(BlockGetter world, BlockPos pos) {
		var entity = world.getBlockEntity(pos);
		if (entity instanceof CanvasBlockEntity blackboard)
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

				var blackboard = this.getCanvasEntity(world, pos);
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
		var builder = ImmutableMap.<Direction, VoxelShape>builder();

		builder.put(Direction.NORTH, box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
		builder.put(Direction.EAST, box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
		builder.put(Direction.SOUTH, box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
		builder.put(Direction.WEST, box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

		SHAPES = new EnumMap<>(builder.build());
	}
}
