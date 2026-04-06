/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.menu.slot;

import dev.lambdaurora.aurorascanvas.canvas.BlackboardColor;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a slot that only accepts items that can be used as {@link BlackboardColor blackboard colors}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class ColorSlot extends Slot {
	public ColorSlot(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return BlackboardColor.fromItem(stack.getItem()) != null || DrawModifier.fromItem(stack) != null;
	}
}
