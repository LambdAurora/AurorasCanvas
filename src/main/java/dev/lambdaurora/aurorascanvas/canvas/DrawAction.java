/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.canvas;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Represents a draw action.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum DrawAction {
	DEFAULT(AurorasCanvas.NAMESPACE + ".tool.pixel") {
		@Override
		public @Nullable Item getOffHandTool(FeatureFlagSet enabledFeatures) {
			return null;
		}

		@Override
		public boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier) {
			var colorData = canvas.getPixel(x, y);
			return canvas.setPixel(x, y, modifier.apply(colorData));
		}
	},
	BRUSH(AurorasCanvas.NAMESPACE + ".tool.brush") {
		@Override
		public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
			return Items.BRUSH;
		}

		@Override
		public boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier) {
			return canvas.drawBrush(x, y, modifier);
		}
	},
	LINE(AurorasCanvas.NAMESPACE + ".tool.line") {
		@Override
		public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
			return Items.STICK;
		}

		@Override
		public boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier) {
			return false;
		}
	},
	FILL(AurorasCanvas.NAMESPACE + ".tool.fill") {
		@Override
		public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
			return Items.BUCKET;
		}

		@Override
		public boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier) {
			var colorData = canvas.getPixel(x, y);
			return canvas.fillColor(x, y, modifier.apply(colorData));
		}
	},
	REPLACE(AurorasCanvas.NAMESPACE + ".tool.replace") {
		@Override
		public Item getOffHandTool(FeatureFlagSet enabledFeatures) {
			return Items.ENDER_PEARL;
		}

		@Override
		public boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier) {
			var colorData = canvas.getPixel(x, y);
			return canvas.replaceColor(x, y, modifier.apply(colorData));
		}
	};

	public static final List<DrawAction> ACTIONS = List.of(values());

	private final String translationKey;

	DrawAction(String translationKey) {
		this.translationKey = translationKey;
	}

	public Component getName() {
		return Component.translatable(this.translationKey);
	}

	public abstract @Nullable Item getOffHandTool(FeatureFlagSet enabledFeatures);

	public abstract boolean execute(CanvasHandler canvas, int x, int y, DrawModifier modifier);

	public static @Nullable DrawAction byItem(FeatureFlagSet enabledFeatures, Item item) {
		for (var possibleAction : DrawAction.ACTIONS) {
			Item offHandTool = possibleAction.getOffHandTool(enabledFeatures);

			if (item.equals(offHandTool) || (offHandTool == null && item == Items.AIR)) {
				return possibleAction;
			}
		}

		return null;
	}
}
