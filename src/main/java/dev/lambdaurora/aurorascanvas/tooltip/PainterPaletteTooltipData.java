/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.tooltip;

import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Represents the painter's palette tooltip data.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record PainterPaletteTooltipData(PainterPaletteItem.PainterPaletteInventory inventory) implements TooltipComponent {}
