/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public final class Utils {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());

	private Utils() {
		throw new UnsupportedOperationException("Utils only contains static definitions.");
	}

	public static double posMod(double n, double d) {
		double v = n % d;
		if (v < 0) v = d + v;
		return v;
	}

	public static void writeBlockEntityNbtToStack(ItemStack stack, BlockEntityType<?> type, CompoundTag nbt, boolean force) {
		boolean hasDummy = false;
		if (nbt.isEmpty() && force) {
			nbt.putBoolean("aurorasdeco$dummy", true);
			hasDummy = true;
		}

		BlockItem.setBlockEntityData(stack, type, nbt);
		nbt.remove("id");

		if (hasDummy) {
			nbt.remove("aurorasdeco$dummy");
		}
	}

	public static CompoundTag getOrCreateBlockEntityNbt(ItemStack stack, BlockEntityType<?> type) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt == null) {
			/*
			 * setBlockEntityNbt only actually sets the nbt tag if it isn't empty.
			 * We want to hit the code path to set the nbt tag. So we add a dummy boolean to our tag,
			 * call the method to add it (which actually adds it because it's not empty),
			 * and then remove the dummy boolean again.
			 */
			var newNbt = new CompoundTag();
			writeBlockEntityNbtToStack(stack, type, newNbt, true);
			return newNbt;
		} else {
			return nbt;
		}
	}
}
