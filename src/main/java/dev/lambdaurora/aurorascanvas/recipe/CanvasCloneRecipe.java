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
import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents the canvas clone recipe.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CanvasCloneRecipe extends CustomRecipe {
	private static final Ingredient INPUT = Ingredient.fromValues(Stream.of(
			new Ingredient.TagValue(AurorasCanvasRegistry.CANVAS_ITEMS),
			new Ingredient.TagValue(AurorasCanvasRegistry.CANVAS_COMPATIBLE_ITEMS)
	));
	private static final Ingredient OUTPUT = Ingredient.of(
			AurorasCanvasRegistry.BLACKBOARD,
			AurorasCanvasRegistry.CHALKBOARD,
			AurorasCanvasRegistry.WHITEBOARD,
			AurorasCanvasRegistry.GLASSBOARD
	);

	public CanvasCloneRecipe(Identifier id, CraftingBookCategory craftingCategory) {
		super(id, craftingCategory);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		boolean hasInput = false, hasOutput = false;
		int count = 0;

		for (int slot = 0; slot < inv.getContainerSize(); ++slot) {
			var stack = inv.getItem(slot);

			if (INPUT.test(stack)) {
				var input = this.getInput(stack);

				if (OUTPUT.test(stack) && input.isEmpty())
					hasOutput = true;
				else if (input.isPresent())
					hasInput = true;
				count++;
			}
		}
		return hasInput && hasOutput && count == 2;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryManager) {
		CanvasHolder<?> canvases = null;
		ItemStack output = null;
		Component customName = null;

		for (int slot = 0; slot < inv.getContainerSize(); ++slot) {
			var craftStack = inv.getItem(slot);
			if (!craftStack.isEmpty()) {
				var input = this.getInput(craftStack);

				if (OUTPUT.test(craftStack) && input.isEmpty()) {
					output = craftStack;
				} else if (input.isPresent()) {
					canvases = input.get();
					if (craftStack.hasCustomHoverName())
						customName = craftStack.getHoverName();
				}
			}
		}

		var out = output.copy();
		out.setCount(1);

		if (!(output.getItem() instanceof CanvasItem<?> canvasItem)) {
			return ItemStack.EMPTY;
		}

		var outputCanvases = canvasItem.getCanvases(output);

		if (outputCanvases.type().equals(canvases.type())) {
			var inputCanvasList = canvases.stream().toList();
			var outputCanvasList = outputCanvases.stream().toList();

			for (int i = 0; i < inputCanvasList.size(); i++) {
				outputCanvasList.get(i).copy(inputCanvasList.get(i));
			}
		} else {
			outputCanvases.getDefault().copy(canvases.getDefault());
		}

		var nbt = Utils.getOrCreateBlockEntityNbt(out, AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
		nbt.merge(outputCanvases.toNbt());

		if (customName != null)
			out.setHoverName(customName);

		return out;
	}

	private Optional<? extends CanvasHolder<?>> getInput(ItemStack stack) {
		if (stack.getItem() instanceof CanvasItem<? extends CanvasHolder<?>> canvasItem) {
			return Optional.of(canvasItem.getCanvases(stack)).filter(Predicate.not(canvases -> canvases.stream().allMatch(Canvas::isEmpty)));
		} else if (stack.getItem().getClass().getName().equals("net.mehvahdjukaar.supplementaries.common.items.BlackboardItem")) {
			var blockEntityData = BlockItem.getBlockEntityData(stack);
			if (blockEntityData == null) return Optional.empty();
			return SupplementariesCompat.canvasHolderFromNbt(blockEntityData);
		}

		return Optional.empty();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingInventory) {
		NonNullList<ItemStack> defaultedList = NonNullList.withSize(craftingInventory.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack invStack = craftingInventory.getItem(i);
			if (!invStack.isEmpty()) {
				if (invStack.getItem().hasCraftingRemainingItem()) {
					defaultedList.set(i, new ItemStack(invStack.getItem().getCraftingRemainingItem()));
				} else if (this.getInput(invStack).isPresent()) {
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