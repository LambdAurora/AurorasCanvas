/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */package dev.lambdaurora.aurorascanvas.tooltip;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents the canvas tooltip data.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record CanvasTooltipData(
		String background,
		@Unmodifiable List<Canvas> canvases,
		boolean locked
) implements TooltipComponent {
}
