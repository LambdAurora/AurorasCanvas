/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.AurorasCanvasSoundEvents;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Represents an easel entity item.
 *
 * @version 1.2.0
 * @since 1.0.0
 */
public class EaselEntityItem extends Item {
	public EaselEntityItem(Item.Properties properties) {
		super(properties);
	}

	/**
	 * Called when this item is used when targeting a Block
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Direction direction = context.getClickedFace();
		if (direction == Direction.DOWN) {
			return InteractionResult.FAIL;
		} else {
			Level level = context.getLevel();
			var blockPlaceContext = new BlockPlaceContext(context);
			BlockPos blockPos = blockPlaceContext.getClickedPos();
			ItemStack handStack = context.getItemInHand();
			Vec3 centerPos = Vec3.atBottomCenterOf(blockPos);
			AABB boundingBox = AurorasCanvasRegistry.EASEL_ENTITY_TYPE.getDimensions().makeBoundingBox(centerPos.x(), centerPos.y(), centerPos.z());
			if (level.noCollision(null, boundingBox) && level.getEntities(null, boundingBox).isEmpty()) {
				if (level instanceof ServerLevel serverLevel) {
					Consumer<EaselEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, handStack, context.getPlayer());
					var easel = AurorasCanvasRegistry.EASEL_ENTITY_TYPE.create(serverLevel, consumer, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
					if (easel == null) {
						return InteractionResult.FAIL;
					}

					float yRot = Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.f) + 22.5f) / 45.f) * 45.f;
					easel.snapTo(easel.getX(), easel.getY(), easel.getZ(), yRot, 0.f);
					serverLevel.addFreshEntityWithPassengers(easel);
					level.playSound(null, easel.getX(), easel.getY(), easel.getZ(), AurorasCanvasSoundEvents.EASEL_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
					easel.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
				}

				handStack.shrink(1);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.FAIL;
			}
		}
	}
}
