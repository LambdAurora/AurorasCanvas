/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.compat;

import com.mojang.serialization.Dynamic;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;

import java.util.Map;
import java.util.Set;

/**
 * Represents the fixer for item stack componentization for Aurora's Canvas items.
 *
 * @version 1.1.0
 * @since 1.1.0
 */
public final class ItemStackComponentizationFixer {
	private static final Set<String> CANVASES = Set.of(
			AurorasCanvas.NAMESPACE + ":blackboard",
			AurorasCanvas.NAMESPACE + ":chalkboard",
			AurorasCanvas.NAMESPACE + ":whiteboard",
			AurorasCanvas.NAMESPACE + ":waxed_blackboard",
			AurorasCanvas.NAMESPACE + ":waxed_chalkboard",
			AurorasCanvas.NAMESPACE + ":waxed_whiteboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":blackboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":chalkboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":waxed_blackboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":waxed_chalkboard"
	);
	private static final Set<String> GLASS_CANVASES = Set.of(
			AurorasCanvas.NAMESPACE + ":glassboard",
			AurorasCanvas.NAMESPACE + ":waxed_glassboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":glassboard",
			AurorasDecoDataUpper.OLD_NAMESPACE + ":waxed_glassboard"
	);
	private static final String PAINTER_PALETTE = AurorasCanvas.NAMESPACE + ":painter_palette";

	/**
	 * Fixes the item stacks of Aurora's Canvas to move to components.
	 *
	 * @param itemStack the item stack data
	 * @param dynamic the dynamic
	 */
	public static void fixItemStack(ItemStackComponentizationFix.ItemStackData itemStack, Dynamic<?> dynamic) {
		if (itemStack.is(CANVASES)) {
			var beData = itemStack.removeTag("BlockEntityTag").result();

			if (beData.isPresent()) {
				var canvas = beData.get().get("canvas").result();

				if (canvas.isPresent()) {
					itemStack.setComponent(AurorasCanvas.NAMESPACE + ":canvas", canvas.get());
				} else {
					// Aurora's Decorations format.
					itemStack.setComponent(AurorasCanvas.NAMESPACE + ":canvas", beData.get());
				}
			}
		} else if (itemStack.is(GLASS_CANVASES)) {
			var beData = itemStack.removeTag("BlockEntityTag").result();

			if (beData.isPresent()) {
				var canvas = beData.get().get("canvas").result();

				if (canvas.isPresent()) {
					itemStack.setComponent(AurorasCanvas.NAMESPACE + ":canvas/glass", canvas.get());
				} else {
					// Aurora's Decorations format.
					var data = dynamic.emptyMap();
					Dynamic.copyField(beData.get(), "version", data, "version");
					Dynamic.copyField(beData.get(), "pixels", data, "pixels");
					Dynamic.copyField(beData.get(), "lit", data, "lit");

					var root = dynamic.emptyMap();
					root.set("front", data);

					itemStack.setComponent(AurorasCanvas.NAMESPACE + ":canvas/glass", root);
				}
			}
		} else if (itemStack.is(PAINTER_PALETTE)) {
			itemStack.moveTagToComponent("inventory", AurorasCanvas.NAMESPACE + ":palette_inventory", dynamic.createMap(Map.of()));
		}
	}

	private ItemStackComponentizationFixer() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
