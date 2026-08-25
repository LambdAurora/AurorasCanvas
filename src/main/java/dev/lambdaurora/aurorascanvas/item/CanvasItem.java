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
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
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
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a canvases item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CanvasItem extends BlockItem {
	protected final boolean locked;

	public CanvasItem(CanvasBlock canvasBlock, Properties settings) {
		super(canvasBlock, settings);
		this.locked = canvasBlock.isLocked();
	}

	public @Unmodifiable List<IndexedCanvas.Provider> canvasProviders() {
		return List.of(IndexedCanvas.SIMPLE);
	}

	public List<IndexedCanvas> getCanvases(ItemStack stack, boolean allowUnedited) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null) {
			return this.canvasProviders().stream()
					.map(provider -> provider.reader().fromNbt(nbt))
					.filter(canvas -> allowUnedited || !canvas.canvas().isUnedited())
					.toList();
		} else if (allowUnedited) {
			return this.canvasProviders().stream()
					.map(provider -> new IndexedCanvas(provider.key(), new Canvas()))
					.toList();
		}

		return List.of();
	}

	public String getBackground() {
		return BuiltInRegistries.ITEM.getKey(this).getPath().replace("waxed_", "");
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY) {
			if (otherStack.is(Items.WATER_BUCKET)
					|| (otherStack.is(Items.POTION) && PotionUtils.getPotion(otherStack) == Potions.WATER)) {
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
		var canvases = this.getCanvases(self, false);
		if (canvases.isEmpty()) return false;

		var nbt = Utils.getOrCreateBlockEntityNbt(self, AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
		int cleared = 0;
		for (var entry : canvases) {
			if (entry.canvas().isEmpty()) continue;
			entry.canvas().clear();
			entry.writeNbt(nbt);
			cleared++;
		}

		return cleared > 0;
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
			var nbt = Utils.getOrCreateBlockEntityNbt(stack, AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
			var blackboard = new Canvas();
			blackboard.writeNbt(nbt);
		}
		return stack;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var canvases = this.getCanvases(stack, false).stream().map(IndexedCanvas::canvas)
				.filter(Predicate.not(Canvas::isEmpty))
				.toList();

		if (!canvases.isEmpty()) {
			return Optional.of(new CanvasTooltipData(this.getBackground(), canvases, this.locked));
		}

		return super.getTooltipImage(stack);
	}
}
