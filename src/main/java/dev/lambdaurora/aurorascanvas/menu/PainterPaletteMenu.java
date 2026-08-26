/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.menu;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import dev.lambdaurora.aurorascanvas.menu.slot.CanvasToolSlot;
import dev.lambdaurora.aurorascanvas.menu.slot.ColorSlot;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the painter's palette screen handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class PainterPaletteMenu extends NestedMenu {
	private final PainterPaletteInventory inventory;

	public PainterPaletteMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(syncId, playerInventory, buf.readEnum(OriginType.class), buf.readVarInt(), new PainterPaletteInventory());
	}

	public PainterPaletteMenu(
			int syncId, Inventory playerInventory, OriginType originType, int lockedSlot,
			PainterPaletteInventory inventory
	) {
		super(AurorasCanvasRegistry.PAINTER_PALETTE_MENU_TYPE, syncId, originType, lockedSlot);
		this.inventory = inventory;
		this.inventory.startOpen(playerInventory.player);

		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new ColorSlot(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
			}
		}

		for (int row = 0; row < 4; ++row) {
			this.addSlot(new CanvasToolSlot(
					inventory, playerInventory.player.level().enabledFeatures(),
					(inventory.getContainerSize() - 4) + row, -16, 18 + row * 18
			));
		}

		this.addPlayerInventory(playerInventory, 8, 85);

		this.addDataSlots(inventory.getProperties());
	}

	public PainterPaletteInventory getInventory() {
		return this.inventory;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		var slot = this.slots.get(id);

		if (slot instanceof ColorSlot && !slot.getItem().isEmpty()) {
			this.inventory.setSelectedColor(slot.getContainerSlot());
			return true;
		} else if (slot instanceof CanvasToolSlot) {
			if (slot.getItem().isEmpty()) this.inventory.setSelectedToolSlot(-1);
			else this.inventory.setSelectedToolSlot(slot.getContainerSlot());
			return true;
		} else if (id == this.inventory.getContainerSize()) {
			this.inventory.setSelectedToolSlot(-1);
			return true;
		}

		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		var itemStack = ItemStack.EMPTY;
		var slot = this.slots.get(fromIndex);

		if (slot.isActive()) {
			var currentStack = slot.getItem();
			itemStack = currentStack.copy();
			if (fromIndex < this.inventory.getContainerSize()) {
				if (!this.moveItemStackTo(currentStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				int playerInventoryEnd = this.inventory.getContainerSize() + 27;
				int hotbarEnd = playerInventoryEnd + 9;

				if (!this.moveItemStackTo(currentStack, 0, this.inventory.getContainerSize(), false)) {
					if (fromIndex >= playerInventoryEnd && fromIndex < hotbarEnd) {
						if (!this.moveItemStackTo(currentStack, this.inventory.getContainerSize(), playerInventoryEnd, false)) {
							return ItemStack.EMPTY;
						}
					} else if (fromIndex < playerInventoryEnd) {
						if (!this.moveItemStackTo(currentStack, playerInventoryEnd, hotbarEnd, false)) {
							return ItemStack.EMPTY;
						}
					} else if (!this.moveItemStackTo(currentStack, playerInventoryEnd, playerInventoryEnd, false)) {
						return ItemStack.EMPTY;
					}

					return ItemStack.EMPTY;
				}
			}

			if (currentStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.inventory.stillValid(player);
	}

	@Override
	protected boolean saveToOriginItem(ItemStack stack) {
		var nbt = inventory.toNbt();
		if (nbt != null) stack.addTagElement("inventory", nbt);
		else {
			if (stack.getTagElement("inventory") == null) {
				return false;
			}

			stack.removeTagKey("inventory");
		}

		return true;
	}

	public record Factory(ItemStack self, OriginType type, int lockedSlot) implements ExtendedScreenHandlerFactory {
		@Override
		public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
			buf.writeEnum(this.type);
			buf.writeVarInt(this.lockedSlot);
		}

		@Override
		public Component getDisplayName() {
			return this.self.getHoverName();
		}

		@Override
		public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
			var inventory = PainterPaletteInventory.fromNbt(this.self.getTagElement("inventory"));

			return new PainterPaletteMenu(syncId, playerInventory, this.type, this.lockedSlot, inventory);
		}
	}
}
