/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.recipe;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Represents the canvas clone recipe.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class CanvasCloneRecipe extends CustomRecipe {
	private static final Ingredient INPUT = Ingredient.of(AurorasCanvasRegistry.CANVAS_ITEMS);
	private static final Ingredient OUTPUT = Ingredient.of(
			AurorasCanvasRegistry.BLACKBOARD,
			AurorasCanvasRegistry.CHALKBOARD,
			AurorasCanvasRegistry.WHITEBOARD,
			AurorasCanvasRegistry.GLASSBOARD
	);

	public CanvasCloneRecipe(CraftingBookCategory craftingCategory) {
		super(craftingCategory);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean hasInput = false, hasOutput = false;
		int count = 0;

		for (int slot = 0; slot < input.size(); ++slot) {
			var stack = input.getItem(slot);

			if (INPUT.test(stack)) {
				if (OUTPUT.test(stack) && !this.isInput(stack))
					hasOutput = true;
				else if (this.isInput(stack))
					hasInput = true;
				count++;
			}
		}
		return hasInput && hasOutput && count == 2;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		Canvas blackboard = null;
		ItemStack output = null;
		Component customName = null;

		for (int slot = 0; slot < input.size(); ++slot) {
			var craftStack = input.getItem(slot);
			if (!craftStack.isEmpty()) {
				if (OUTPUT.test(craftStack) && !this.isInput(craftStack)) {
					output = craftStack;
				} else if (this.isInput(craftStack)) {
					var nbt = BlockItem.getBlockEntityData(craftStack);
					blackboard = Canvas.fromNbt(nbt);
					if (craftStack.hasCustomHoverName())
						customName = craftStack.getHoverName();
				}
			}
		}


		var out = output.copy();
		out.setCount(1);
		var nbt = Utils.getOrCreateBlockEntityNbt(out, AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
		blackboard.writeNbt(nbt);

		if (customName != null)
			out.setHoverName(customName);

		return out;
	}

	private boolean isInput(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null) {
			if (nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)) {
				byte[] pixels = nbt.getByteArray("pixels");
				for (byte pixel : pixels) {
					if (pixel != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack invStack = input.getItem(i);
			if (!invStack.isEmpty()) {
				if (invStack.getItem().hasCraftingRemainingItem()) {
					defaultedList.set(i, new ItemStack(invStack.getItem().getCraftingRemainingItem()));
				} else if (this.isInput(invStack)) {
					ItemStack remainder = invStack.copy();
					remainder.setCount(1);
					defaultedList.set(i, remainder);
				}
			}
		}

		return defaultedList;
	}


	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AurorasCanvasRegistry.CANVAS_CLONE_RECIPE_SERIALIZER;
	}
}