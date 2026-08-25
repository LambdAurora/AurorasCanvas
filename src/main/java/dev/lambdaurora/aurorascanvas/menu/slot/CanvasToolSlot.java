/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.menu.slot;

import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import net.minecraft.world.Container;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a slot that only accepts items that can be used as {@link dev.lambdaurora.aurorascanvas.canvas.DrawModifier draw modifiers}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CanvasToolSlot extends Slot {
	private final FeatureFlagSet enabledFeatures;

	public CanvasToolSlot(Container inventory, FeatureFlagSet enabledFeatures, int index, int x, int y) {
		super(inventory, index, x, y);
		this.enabledFeatures = enabledFeatures;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return DrawAction.ACTIONS.stream()
				.filter(drawAction -> drawAction.getOffHandTool(this.enabledFeatures) != null)
				.anyMatch(drawAction -> drawAction.getOffHandTool(this.enabledFeatures) == stack.getItem());
	}
}
