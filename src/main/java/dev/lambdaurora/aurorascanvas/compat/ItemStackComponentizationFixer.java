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
	 * @param itemStackData the item stack data
	 * @param dynamic the dynamic
	 */
	public static void fixItemStack(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		if (itemStackData.is(CANVASES)) {
			itemStackData.moveTagToComponent("BlockEntityTag", AurorasCanvas.NAMESPACE + ":canvas");
		} else if (itemStackData.is(GLASS_CANVASES)) {
			itemStackData.moveTagToComponent("BlockEntityTag", AurorasCanvas.NAMESPACE + ":canvas/glass");
		} else if (itemStackData.is(PAINTER_PALETTE)) {
			itemStackData.moveTagToComponent("inventory", AurorasCanvas.NAMESPACE + ":palette_inventory", dynamic.createMap(Map.of()));
		}
	}

	private ItemStackComponentizationFixer() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
	}
}
