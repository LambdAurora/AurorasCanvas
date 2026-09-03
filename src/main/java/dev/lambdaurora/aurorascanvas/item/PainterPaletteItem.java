/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.CanvasColor;
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import dev.lambdaurora.aurorascanvas.menu.NestedMenu;
import dev.lambdaurora.aurorascanvas.menu.PainterPaletteMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Represents a painter's palette item which can be used for easier painting on canvases.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class PainterPaletteItem extends Item {
	private static final int DEFAULT_BACKGROUND_COLOR = 0xff967441;

	public PainterPaletteItem(Properties settings) {
		super(settings);
	}

	public static PainterPaletteInventory getInventory(ItemStack paletteStack) {
		return paletteStack.getOrDefault(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE, PainterPaletteInventory.EMPTY);
	}

	public static MutableComponent getSelectedToolMessage(PainterPaletteInventory inventory, FeatureFlagSet enabledFeatures) {
		Component toolName = DrawAction.ACTIONS.stream()
				.filter(drawAction -> {
					var offHandTool = drawAction.getOffHandTool(enabledFeatures);
					var selectedTool = inventory.getSelectedTool();

					return (offHandTool == null && selectedTool.isEmpty()) || selectedTool.is(offHandTool);
				}).findFirst()
				.map(DrawAction::getName).orElseThrow(() -> new IllegalStateException("Could not get tool name."));

		return Component.translatable(AurorasCanvas.NAMESPACE + ".change_tool", toolName);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack otherStack, Slot thisSlot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY && otherStack.isEmpty() && !(player.containerMenu instanceof PainterPaletteMenu)) {
			NestedMenu.OriginType originType = null;
			if (thisSlot.container == player.getInventory()) {
				originType = NestedMenu.OriginType.PLAYER;
			} else if (thisSlot.container == player.getEnderChestInventory()) {
				originType = NestedMenu.OriginType.ENDER_CHEST;
			}

			if (originType != null && !player.level().isClientSide()) {
				player.inventoryMenu.resumeRemoteUpdates();
				player.openMenu(new PainterPaletteMenu.Factory(thisStack, originType, thisSlot.getContainerSlot()));
			}

			return true;
		}

		return super.overrideOtherStackedOnMe(thisStack, otherStack, thisSlot, clickType, player, cursor);
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);

		if (user.isShiftKeyDown()) {
			if (!user.level().isClientSide()) {
				int index = user.getInventory().findSlotMatchingItem(stack);

				user.containerMenu.resumeRemoteUpdates();
				user.openMenu(new PainterPaletteMenu.Factory(stack, NestedMenu.OriginType.PLAYER, index));
			}

			return InteractionResult.CONSUME;
		}

		return super.use(world, user, hand);
	}

	public void onScroll(Player player, ItemStack paletteStack, double scrollDelta, boolean toolModifier) {
		var inventory = getInventory(paletteStack);

		if (inventory.isPaletteEmpty()) {
			return;
		}

		var mutable = inventory.toMutable();

		if (!toolModifier) {
			if (scrollDelta < 0) {
				byte nextColor = mutable.findFirstNextColor();

				if (nextColor != -1) {
					mutable.setSelectedColor(nextColor);
				}
			} else {
				byte previousColor = mutable.findFirstPreviousColor();

				if (previousColor != -1) {
					mutable.setSelectedColor(previousColor);
				}
			}

			var copy = paletteStack.copy();
			if (mutable.isEmpty()) {
				copy.remove(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
			} else {
				copy.set(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE, mutable.toImmutable());
			}
			player.setItemInHand(InteractionHand.MAIN_HAND, copy);

			player.inventoryMenu.broadcastChanges();

			var modifier = DrawModifier.fromItem(inventory.getSelectedColor());

			if (!(modifier instanceof CanvasColor) && modifier != null) {
				player.sendOverlayMessage(Component.translatable(AurorasCanvas.NAMESPACE + ".change_modifier", modifier.getName()));
			}
		} else {
			int nextTool = mutable.scrollTool(scrollDelta < 0);

			if (mutable.getSelectedToolSlot() != nextTool) {
				mutable.setSelectedToolSlot(nextTool);

				var copy = paletteStack.copy();
				if (mutable.isEmpty()) {
					copy.remove(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
				} else {
					copy.set(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE, mutable.toImmutable());
				}
				player.setItemInHand(InteractionHand.MAIN_HAND, copy);

				player.inventoryMenu.broadcastChanges();

				var message = getSelectedToolMessage(inventory, player.level().enabledFeatures());
				CanvasColor primaryColor = CanvasColor.fromItem(inventory.getSelectedColor().getItem());

				if (primaryColor != null && primaryColor != CanvasColor.EMPTY) message.withStyle(style -> style.withColor(primaryColor.getColor()));

				player.sendOverlayMessage(message);
			}
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var inventory = stack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
		if (inventory != null) {
			return Optional.of(inventory);
		}
		return super.getTooltipImage(stack);
	}
}
