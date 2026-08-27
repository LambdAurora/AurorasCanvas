/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a canvas item.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public abstract class CanvasItem<T extends CanvasHolder<T>> extends BlockItem {
	protected final boolean locked;

	public CanvasItem(CanvasBlock canvasBlock, Properties settings) {
		super(canvasBlock, settings);
		this.locked = canvasBlock.isLocked();
	}

	public abstract CanvasHolder.Type<T> canvasType();

	public T getCanvases(ItemStack stack) {
		var component = stack.get(this.canvasType().componentType());

		if (component == null) {
			return this.canvasType().createDefault();
		} else {
			return component;
		}
	}

	public String getBackground() {
		return BuiltInRegistries.ITEM.getKey(this).getPath().replace("waxed_", "");
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY) {
			var potionContents = otherStack.get(DataComponents.POTION_CONTENTS);

			if (otherStack.is(Items.WATER_BUCKET) || (potionContents != null && potionContents.is(Potions.WATER))) {
				if (!this.clearContents(self))
					return false;

				if (otherStack.is(Items.POTION)) {
					if (!player.getAbilities().instabuild) {
						var newStack = new ItemStack(Items.GLASS_BOTTLE);
						if (otherStack.getCount() != 1) {
							otherStack.shrink(1);
							player.getInventory().add(newStack);
						} else {
							cursor.set(newStack);
						}
					}
					player.playSound(SoundEvents.BOTTLE_EMPTY, 1.f, 1.f);
				} else {
					player.playSound(SoundEvents.BUCKET_EMPTY, 1.f, 1.f);
				}

				return true;
			}
		}
		return false;
	}

	protected boolean clearContents(ItemStack self) {
		var canvases = this.getCanvases(self);

		long cleared = canvases.stream().filter(canvas -> {
			if (!canvas.isEmpty()) {
				canvas.clear();
				return true;
			}

			return false;
		}).count();

		if (cleared > 0) {
			canvases.setOnStack(self);
		}

		return cleared > 0;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var canvases = this.getCanvases(stack).stream()
				.filter(Predicate.not(Canvas::isEmpty))
				.toList();

		if (!canvases.isEmpty()) {
			return Optional.of(new CanvasTooltipData(this.getBackground(), canvases, this.locked));
		}

		return super.getTooltipImage(stack);
	}
}
