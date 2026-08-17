/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Represents a canvas item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CanvasItem extends BlockItem {
	private final boolean locked;

	public CanvasItem(CanvasBlock canvasBlock, Properties settings) {
		super(canvasBlock, settings);
		this.locked = canvasBlock.isLocked();
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY) {
			if (otherStack.is(Items.WATER_BUCKET)
					|| (otherStack.is(Items.POTION) && PotionUtils.getPotion(otherStack) == Potions.WATER)) {
				var nbt = Utils.getOrCreateBlockEntityNbt(self, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
				var blackboard = Canvas.fromNbt(nbt);
				if (blackboard.isEmpty())
					return false;
				blackboard.clear();
				blackboard.writeNbt(nbt);

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

	@Override
	public void onCraftedBy(ItemStack stack, Level world, Player player) {
		this.ensureValidStack(stack);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return this.ensureValidStack(new ItemStack(this));
	}

	private ItemStack ensureValidStack(ItemStack stack) {
		if (BlockItem.getBlockEntityData(stack) == null) {
			var nbt = Utils.getOrCreateBlockEntityNbt(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
			var blackboard = new Canvas();
			blackboard.writeNbt(nbt);
		}
		return stack;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)) {
			var blackboard = Canvas.fromNbt(nbt);
			return Optional.of(new CanvasTooltipData(
					BuiltInRegistries.ITEM.getKey(this).getPath().replace("waxed_", ""),
					blackboard, this.locked
			));
		}
		return super.getTooltipImage(stack);
	}
}
