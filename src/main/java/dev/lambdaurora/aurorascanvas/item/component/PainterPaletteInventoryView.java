/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item.component;

import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Represents a painter's palette inventory view.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public interface PainterPaletteInventoryView {
	@Unmodifiable
	List<ItemStack> getTools();

	default @Unmodifiable List<DrawAction> getAvailableTools(FeatureFlagSet enabledFeatures) {
		var tools = new HashSet<>(
				this.getTools().stream().map(stack -> DrawAction.byItem(enabledFeatures, stack.getItem()))
						.filter(Objects::nonNull)
						.toList()
		);

		tools.add(DrawAction.DEFAULT);

		return tools.stream()
				.sorted(Comparator.comparingInt(DrawAction::ordinal))
				.toList();
	}

	@Unmodifiable
	List<ItemStack> getColors();

	ItemStack getColorStack(int index);

	byte getSelectedColorSlot();

	default ItemStack getSelectedColor() {
		return this.getColorStack(this.getSelectedColorSlot());
	}

	int getSelectedToolSlot();

	ItemStack getSelectedTool();

	byte findFirstNextColor();

	byte findFirstPreviousColor();

	default ItemStack getNextColorStack() {
		byte previousSlot = this.findFirstPreviousColor();
		byte nextSlot = this.findFirstNextColor();

		if (nextSlot == -1 || nextSlot == previousSlot) return ItemStack.EMPTY;
		else return this.getColorStack(nextSlot);
	}

	default @Nullable DrawModifier getNextColor() {
		return DrawModifier.fromItem(this.getNextColorStack());
	}

	default ItemStack getPreviousColorStack() {
		byte previousSlot = this.findFirstPreviousColor();

		if (previousSlot == -1) return ItemStack.EMPTY;
		else return this.getColorStack(previousSlot);
	}

	default @Nullable DrawModifier getPreviousColor() {
		return DrawModifier.fromItem(this.getPreviousColorStack());
	}

	boolean isPaletteEmpty();
}
