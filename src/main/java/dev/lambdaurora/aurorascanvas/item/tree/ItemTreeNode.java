/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item.tree;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public interface ItemTreeNode {
	CreativeModeTab.TabVisibility getVisibility();

	void build(Collection<ItemStack> stacks, FeatureFlagSet enabledFeatures, CreativeModeTab.TabVisibility visibility);

	interface Item extends ItemTreeNode {
		ItemStack stack();

		void setVisibility(CreativeModeTab.TabVisibility visibility);
	}
}
