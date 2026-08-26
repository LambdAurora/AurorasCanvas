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
import dev.lambdaurora.aurorascanvas.tooltip.PainterPaletteTooltipData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
 * @version 1.1.0
 * @since 1.0.0
 */
public class PainterPaletteItem extends Item {
	private static final int DEFAULT_BACKGROUND_COLOR = 0xff967441;

	public PainterPaletteItem(Properties settings) {
		super(settings);
	}

	public static PainterPaletteInventory getInventory(ItemStack paletteStack) {
		var inventory = paletteStack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);

		return inventory != null ? inventory : new PainterPaletteInventory();
	}

	public ItemStack getCurrentColorAsItem(ItemStack paletteStack) {
		return getInventory(paletteStack).getSelectedColor();
	}

	public ItemStack getCurrentToolAsItem(ItemStack paletteStack) {
		var inventory = getInventory(paletteStack);
		if (inventory.getSelectedToolSlot() == -1) return ItemStack.EMPTY;

		return inventory.getSelectedTool();
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
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);

		if (user.isShiftKeyDown()) {
			if (!user.level().isClientSide()) {
				int index = user.getInventory().findSlotMatchingItem(stack);

				user.containerMenu.resumeRemoteUpdates();
				user.openMenu(new PainterPaletteMenu.Factory(stack, NestedMenu.OriginType.PLAYER, index));
			}

			return InteractionResultHolder.consume(stack);
		}

		return super.use(world, user, hand);
	}

	public void onScroll(Player player, ItemStack paletteStack, double scrollDelta, boolean toolModifier) {
		var inventory = this.getInventory(paletteStack);

		if (inventory.isEmpty()) {
			return;
		}

		if (!toolModifier) {
			if (scrollDelta < 0) {
				byte nextColor = inventory.findFirstNextColor();

				if (nextColor != -1) {
					inventory.setSelectedColor(nextColor);
				}
			} else {
				byte previousColor = inventory.findFirstPreviousColor();

				if (previousColor != -1) {
					inventory.setSelectedColor(previousColor);
				}
			}

			if (inventory.isEmpty()) {
				paletteStack.remove(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
			} else {
				paletteStack.set(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE, inventory);
			}

			player.inventoryMenu.broadcastChanges();

			var modifier = DrawModifier.fromItem(inventory.getSelectedColor());

			if (!(modifier instanceof CanvasColor) && modifier != null) {
				player.displayClientMessage(Component.translatable(AurorasCanvas.NAMESPACE + ".change_modifier", modifier.getName()), true);
			}
		} else {
			int nextTool = inventory.scrollTool(scrollDelta < 0);

			if (inventory.getSelectedToolSlot() != nextTool) {
				inventory.setSelectedToolSlot(nextTool);

				if (inventory.isEmpty()) {
					paletteStack.remove(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
				} else {
					paletteStack.set(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE, inventory);
				}

				player.inventoryMenu.broadcastChanges();

				var message = getSelectedToolMessage(inventory, player.level().enabledFeatures());
				CanvasColor primaryColor = CanvasColor.fromItem(inventory.getSelectedColor().getItem());

				if (primaryColor != null && primaryColor != CanvasColor.EMPTY) message.withStyle(style -> style.withColor(primaryColor.getColor()));

				player.displayClientMessage(message, true);
			}
		}
	}

	public int getColor(ItemStack paletteStack, int tintIndex) {
		var inventory = paletteStack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);

		DrawModifier primaryColor = null;
		DrawModifier previousColor = null;
		DrawModifier nextColor = null;

		if (inventory != null) {
			primaryColor = DrawModifier.fromItem(inventory.getSelectedColor());
			previousColor = inventory.getPreviousColor();
			nextColor = inventory.getNextColor();
		}

		return switch (tintIndex) {
			case 1 -> primaryColor == null ? DEFAULT_BACKGROUND_COLOR : primaryColor.getColor();
			case 2 -> primaryColor == null ? 0xffffffff : primaryColor.getColor();
			case 3 -> previousColor == null ? DEFAULT_BACKGROUND_COLOR : previousColor.getColor();
			case 4 -> nextColor == null ? DEFAULT_BACKGROUND_COLOR : nextColor.getColor();
			default -> 0xffffffff;
		};
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var inventory = stack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);
		if (inventory != null) {
			return Optional.of(new PainterPaletteTooltipData(inventory));
		}
		return super.getTooltipImage(stack);
	}
}
