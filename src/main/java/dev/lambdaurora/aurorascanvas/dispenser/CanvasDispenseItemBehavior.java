/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.dispenser;

import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

/**
 * Represents the dispense item behavior of canvas items.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CanvasDispenseItemBehavior extends OptionalDispenseItemBehavior {
	public static final CanvasDispenseItemBehavior INSTANCE = new CanvasDispenseItemBehavior();

	@Override
	public ItemStack execute(BlockSource source, ItemStack stack) {
		BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));

		for (EaselEntity easel : source.getLevel().getEntitiesOfClass(EaselEntity.class, new AABB(pos), easel -> easel.isAlive() && easel.getItem().isEmpty())) {
			easel.setItem(stack.split(1));
			this.setSuccess(true);
			return stack;
		}

		return super.execute(source, stack);
	}
}
