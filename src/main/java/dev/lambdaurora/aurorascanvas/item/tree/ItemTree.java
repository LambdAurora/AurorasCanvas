/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item.tree;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasIds;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

public class ItemTree extends ItemTreeGroupNode {
	private static final ItemTreeGroupNode BLACKBOARDS = ItemTreeGroupNode.create(
			AurorasCanvasIds.CANVAS_ID,
			groupNode -> {
				groupNode.add(BLACKBOARD.item().value());
				groupNode.add(WAXED_BLACKBOARD.item().value(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				groupNode.add(CHALKBOARD.item().value());
				groupNode.add(WAXED_CHALKBOARD.item().value(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				groupNode.add(WHITEBOARD.item().value());
				groupNode.add(WAXED_WHITEBOARD.item().value(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				groupNode.add(GLASSBOARD.item().value());
				groupNode.add(WAXED_GLASSBOARD.item().value(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				//groupNode.add(BLACKBOARD_PRESS_BLOCK);
			}
	);

	private static final Identifier PHASE = AurorasCanvas.id("phase");
	private static final Identifier ROOT = AurorasCanvas.id("root");

	public ItemTree() {super(ROOT);}

	public static ItemTree fromStacks(List<ItemStack> displayStacks, List<ItemStack> searchStacks) {
		var tree = new ItemTree();
		var nodes = new ArrayList<ItemTreeItemNode>();

		for (var stack : displayStacks) {
			nodes.add(new ItemTreeItemNode(stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY));
		}

		for (int i = 0; i < searchStacks.size(); i++) {
			ItemStack current = searchStacks.get(i);
			ItemStack previous = i == 0 ? null : searchStacks.get(i - 1);
			int foundIndex = -1;

			for (int j = 0; j < nodes.size(); j++) {
				ItemTreeItemNode node = nodes.get(j);

				if (ItemStack.isSameItemSameComponents(node.stack(), current)) {
					node.setVisibility(CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
					foundIndex = -1;
					break;
				} else if (previous != null && ItemStack.isSameItemSameComponents(node.stack(), previous)) {
					foundIndex = j + 1;
				}
			}

			if (foundIndex != -1) {
				nodes.add(foundIndex, new ItemTreeItemNode(current, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY));
			}
		}

		tree.nodes.addAll(nodes);
		return tree;
	}

	public static void init() {
		register(CreativeModeTabs.FUNCTIONAL_BLOCKS, ItemTree::modifyFunctionalBlocks);
		register(CreativeModeTabs.TOOLS_AND_UTILITIES, ItemTree::modifyToolsAndUtilities);
		register(CreativeModeTabs.REDSTONE_BLOCKS, ItemTree::modifyRedstone);
	}

	private static void register(ResourceKey<CreativeModeTab> tab, Consumer<ItemTree> modifier) {
		var event = ItemGroupEvents.modifyEntriesEvent(tab);
		event.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE);
		event.register(PHASE, modifyItems(modifier));
	}

	@SuppressWarnings("UnstableApiUsage")
	private static ItemGroupEvents.ModifyEntries modifyItems(Consumer<ItemTree> modifier) {
		return entries -> {
			var tree = fromStacks(entries.getDisplayStacks(), entries.getSearchTabStacks());

			modifier.accept(tree);

			entries.getDisplayStacks().clear();
			entries.getSearchTabStacks().clear();
			tree.build(entries.getDisplayStacks(), entries.getEnabledFeatures(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
			tree.build(entries.getSearchTabStacks(), entries.getEnabledFeatures(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
		};
	}

	private static void modifyFunctionalBlocks(ItemTree tree) {
		var itemFrames = tree.collectItemsAsGroup(
				Identifier.withDefaultNamespace("item_frame"),
				Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME
		);
		tree.addAfter(itemFrames, BLACKBOARDS);

		tree.addAfter(Items.ARMOR_STAND, EASEL_ITEM);
	}

	private static void modifyToolsAndUtilities(ItemTree tree) {
		tree.addAfter(Items.BRUSH, PAINTER_PALETTE_ITEM);
	}

	private static void modifyRedstone(ItemTree tree) {
		tree.addAfter(Items.ARMOR_STAND, EASEL_ITEM);
	}
}
