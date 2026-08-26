/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.menu;

import dev.lambdaurora.aurorascanvas.menu.slot.LockedSlot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class NestedMenu extends AbstractContainerMenu {
	protected final OriginType originType;
	protected final int lockedSlot;

	protected NestedMenu(@Nullable MenuType<?> type, int syncId, OriginType originType, int lockedSlot) {
		super(type, syncId);
		this.originType = originType;
		this.lockedSlot = lockedSlot;
	}

	protected void addPlayerInventory(Inventory playerInventory, int columnStart, int playerInventoryStart) {
		// Player inventory.
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				if (originType == OriginType.PLAYER && column + row * 9 + 9 == lockedSlot) {
					this.addSlot(new LockedSlot(playerInventory, column + row * 9 + 9, columnStart + column * 18, playerInventoryStart + row * 18));
				} else {
					this.addSlot(new Slot(playerInventory, column + row * 9 + 9, columnStart + column * 18, playerInventoryStart + row * 18));
				}
			}
		}

		for (int column = 0; column < 9; column++) {
			if (originType == OriginType.PLAYER && column == lockedSlot) {
				this.addSlot(new LockedSlot(playerInventory, column, columnStart + column * 18, playerInventoryStart + 58));
			} else {
				this.addSlot(new Slot(playerInventory, column, columnStart + column * 18, playerInventoryStart + 58));
			}
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);

		if (!player.level().isClientSide()) {
			var affectedInventory = switch (this.originType) {
				case PLAYER -> player.getInventory();
				case ENDER_CHEST -> player.getEnderChestInventory();
			};
			var stack = affectedInventory.getItem(this.lockedSlot);

			if (this.saveToOriginItem(stack)) {
				affectedInventory.setChanged();
			}
		}
	}

	protected abstract boolean saveToOriginItem(ItemStack stack);

	public enum OriginType {
		PLAYER,
		ENDER_CHEST;

		private static final List<OriginType> VALUES = List.of(values());
		public static final StreamCodec<ByteBuf, OriginType> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(VALUES::get, Enum::ordinal);
	}
}
