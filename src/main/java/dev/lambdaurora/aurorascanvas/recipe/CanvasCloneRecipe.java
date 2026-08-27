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
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
		CanvasHolder<?> holder = null;
		Component customName = null;

		for (int slot = 0; slot < input.size(); ++slot) {
			var craftStack = input.getItem(slot);
			if (!craftStack.isEmpty()) {
				if (OUTPUT.test(craftStack) && !this.isInput(craftStack)) {
					output = craftStack;
				}
			}
		}

		assert output != null;
		var out = output.copy();
		out.setCount(1);

		for (int slot = 0; slot < input.size(); ++slot) {
			var craftStack = input.getItem(slot);
			if (this.isInput(craftStack) && craftStack.getItem() instanceof CanvasItem<?> canvasItem) {
				var canvases = craftStack.get(canvasItem.canvasType().componentType());

				if (canvases != null) {
					canvases.copy().setOnStack(out);
				}
				if (craftStack.has(DataComponents.CUSTOM_NAME)) {
					out.set(DataComponents.CUSTOM_NAME, craftStack.get(DataComponents.CUSTOM_NAME));
				}
			}
		}

		return out;
	}

	private boolean isInput(ItemStack stack) {
		return stack.has(AurorasCanvasRegistry.CANVAS_COMPONENT_TYPE) || stack.has(AurorasCanvasRegistry.GLASS_CANVAS_COMPONENT_TYPE);
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