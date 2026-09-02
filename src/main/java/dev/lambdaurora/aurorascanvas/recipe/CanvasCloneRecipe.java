/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasLikeHolder;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents the canvas clone recipe.
 *
 * @author LambdAurora
 * @version 1.2.0
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

	private static final Ingredient OUTPUT = Ingredient.of(
			AurorasCanvasRegistry.BLACKBOARD,
			AurorasCanvasRegistry.CHALKBOARD,
			AurorasCanvasRegistry.WHITEBOARD,
			AurorasCanvasRegistry.GLASSBOARD
	);

	public static final MapCodec<CanvasCloneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Ingredient.CODEC.fieldOf("input").forGetter(o -> o.input)
			).apply(instance, CanvasCloneRecipe::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, CanvasCloneRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.input,
			CanvasCloneRecipe::new
	);
	public static final RecipeSerializer<CanvasCloneRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	private final Ingredient input;

	public CanvasCloneRecipe(Ingredient input) {
		this.input = input;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.ingredientCount() != 2) {
			return false;
		}

		boolean hasInput = false, hasOutput = false;
		int count = 0;

		for (int slot = 0; slot < input.size(); ++slot) {
			var stack = input.getItem(slot);

			if (this.input.test(stack)) {
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
	public ItemStack assemble(CraftingInput input) {
		if (input.ingredientCount() != 2) {
			return ItemStack.EMPTY;
		}

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

	private Optional<? extends CanvasHolder<?, ?>> getInput(ItemStack stack) {
		if (stack.getItem() instanceof CanvasItem<? extends CanvasHolder<?, ?>> canvasItem) {
			return Optional.of(canvasItem.getCanvases(stack)).filter(Predicate.not(CanvasLikeHolder::isEmpty));
		}

		return INPUT_GETTER_EVENT.invoker().getInput(stack);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack invStack = input.getItem(i);
			if (!invStack.isEmpty()) {
				ItemStackTemplate remainder = invStack.getItem().getCraftingRemainder();
				if (remainder != null) {
					defaultedList.set(i, remainder.create());
				} else if (this.getInput(invStack).isPresent()) {
					defaultedList.set(i, invStack.copyWithCount(1));
				}
			}
		}

		return defaultedList;
	}

	@Override
	public RecipeSerializer<CanvasCloneRecipe> getSerializer() {
		return SERIALIZER;
	}

	public interface InputGetter {
		Optional<? extends CanvasHolder<?, ?>> getInput(ItemStack stack);
	}
}