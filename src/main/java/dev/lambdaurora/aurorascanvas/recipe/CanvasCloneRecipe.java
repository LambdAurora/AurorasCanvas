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
import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents the canvas clone recipe.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class CanvasCloneRecipe extends CustomRecipe {
	public static final Event<Identifier, InputGetter> INPUT_GETTER_EVENT = YumiEvents.EVENTS.create(InputGetter.class,
			listeners -> stack -> {
				for (var listener : listeners) {
					var result = listener.getInput(stack);

					if (result.isPresent()) {
						return result;
					}
				}

				return Optional.empty();
			});

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
				var inputCanvases = this.getInput(stack);

				if (OUTPUT.test(stack) && inputCanvases.isEmpty())
					hasOutput = true;
				else if (inputCanvases.isPresent())
					hasInput = true;
				count++;
			}
		}
		return hasInput && hasOutput && count == 2;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack output = null;

		for (int slot = 0; slot < input.size(); ++slot) {
			var craftStack = input.getItem(slot);
			if (!craftStack.isEmpty()) {
				var inputCanvases = this.getInput(craftStack);

				if (OUTPUT.test(craftStack) && inputCanvases.isEmpty()) {
					output = craftStack;
				}
			}
		}

		assert output != null;
		var out = output.copy();
		out.setCount(1);

		for (int slot = 0; slot < input.size(); ++slot) {
			var craftStack = input.getItem(slot);
			var inputCanvases = this.getInput(craftStack);

			if (inputCanvases.isPresent() && out.getItem() instanceof CanvasItem<?> canvasItem) {
				var canvases = inputCanvases.get();

				var outputCanvases = canvasItem.getCanvases(out);

				if (outputCanvases.type().equals(canvases.type())) {
					outputCanvases = canvases.copy();
				} else {
					outputCanvases.getDefault().copy(canvases.getDefault());
				}

				outputCanvases.setOnStack(out);

				if (craftStack.has(DataComponents.CUSTOM_NAME)) {
					out.set(DataComponents.CUSTOM_NAME, craftStack.get(DataComponents.CUSTOM_NAME));
				}
			}
		}

		return out;
	}

	private Optional<? extends CanvasHolder<?>> getInput(ItemStack stack) {
		if (stack.getItem() instanceof CanvasItem<? extends CanvasHolder<?>> canvasItem) {
			return Optional.of(canvasItem.getCanvases(stack)).filter(Predicate.not(canvases -> canvases.stream().allMatch(Canvas::isEmpty)));
		}

		return INPUT_GETTER_EVENT.invoker().getInput(stack);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack invStack = input.getItem(i);
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

	public interface InputGetter {
		Optional<? extends CanvasHolder<?>> getInput(ItemStack stack);
	}
}