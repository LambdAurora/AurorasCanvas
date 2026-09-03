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
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Collection;

public class ItemTreeItemNode implements ItemTreeNode.Item {
	private final ItemStackTemplate stack;
	private CreativeModeTab.TabVisibility visibility;

	public ItemTreeItemNode(ItemStackTemplate stack, CreativeModeTab.TabVisibility visibility) {
		this.stack = stack;
		this.visibility = visibility;
	}

	public ItemTreeItemNode(ItemStackTemplate stack) {
		this(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	@Override
	public ItemStack stack() {
		return this.stack.create();
	}

	public ItemStackTemplate templateStack() {
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
				&& this.stack.item().value().isEnabled(enabledFeatures)) {
			stacks.add(this.stack());
		}
	}
}
