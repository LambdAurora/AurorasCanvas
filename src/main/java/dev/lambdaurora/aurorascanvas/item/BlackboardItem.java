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
import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Represents a blackboard item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardItem extends BlockItem {
	private final boolean locked;

	public BlackboardItem(BlackboardBlock blackboardBlock, Properties settings) {
		super(blackboardBlock, settings);
		this.locked = blackboardBlock.isLocked();
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY) {
			if (otherStack.is(Items.WATER_BUCKET)
					|| (otherStack.is(Items.POTION) && PotionUtil.getPotion(otherStack) == Potions.WATER)) {
				var nbt = AuroraUtil.getOrCreateBlockEntityNbt(self, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
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
							player.getInventory().insertStack(newStack);
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
	public void onCraft(ItemStack stack, Level world, Player player) {
		this.ensureValidStack(stack);
	}

	@Override
	public ItemStack getDefaultStack() {
		return this.ensureValidStack(new ItemStack(this));
	}

	private ItemStack ensureValidStack(ItemStack stack) {
		if (BlockItem.getBlockEntityData(stack) == null) {
			var nbt = AuroraUtil.getOrCreateBlockEntityNbt(stack, AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
			var blackboard = new Canvas();
			blackboard.writeNbt(nbt);
		}
		return stack;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)) {
			var blackboard = Blackboard.fromNbt(nbt);
			return Optional.of(new BlackboardTooltipData(
					BuiltInRegistries.ITEM.getKey(this).getPath().replace("waxed_", ""),
					blackboard, this.locked)
			);
		}
		return super.getTooltipData(stack);
	}
}
