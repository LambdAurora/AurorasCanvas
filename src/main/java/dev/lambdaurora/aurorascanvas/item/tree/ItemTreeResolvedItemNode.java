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

public class ItemTreeResolvedItemNode implements ItemTreeNode.Item {
	private final ItemStack stack;
	private CreativeModeTab.TabVisibility visibility;

	public ItemTreeResolvedItemNode(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		this.stack = stack;
		this.visibility = visibility;
	}

	public ItemTreeResolvedItemNode(ItemStack stack) {
		this(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	@Override
	public ItemStack stack() {
		return this.stack;
	}

	@Override
	public CreativeModeTab.TabVisibility getVisibility() {
		return this.visibility;
	}

	@Override
	public void setVisibility(CreativeModeTab.TabVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public void build(Collection<ItemStack> stacks, FeatureFlagSet enabledFeatures, CreativeModeTab.TabVisibility visibility) {
		if ((this.visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || this.visibility == visibility)
				&& this.stack.getItem().isEnabled(enabledFeatures)) {
			stacks.add(this.stack);
		}
	}
}

