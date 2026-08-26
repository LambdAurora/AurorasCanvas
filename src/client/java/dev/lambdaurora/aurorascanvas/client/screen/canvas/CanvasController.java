/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasLikeHolder;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CanvasController {
	private final int id;
	private final CanvasHolder<?> root;
	private final PainterPaletteInventory painterPalette;
	private final List<DrawAction> availableTools;
	final CanvasLikeHolder<TrackedCanvasHandler> canvas;
	final Identifier backgroundTexture;

	DrawAction currentAction;
	DrawModifier currentModifier;

	public CanvasController(int id, Level level, CanvasItem<?> item, PainterPaletteInventory painterPalette, CanvasHolder<?> root) {
		this.id = id;
		this.root = root;
		this.painterPalette = painterPalette;
		this.availableTools = painterPalette.getAvailableTools(level.enabledFeatures());
		this.canvas = this.root().map(TrackedCanvasHandler::new);
		this.backgroundTexture = AurorasCanvas.id("textures/block/canvas/" + item.getBackground() + ".png");

		this.currentAction = DrawAction.DEFAULT;
		this.currentModifier = Objects.requireNonNull(DrawModifier.fromItem(painterPalette.getSelectedColor()));
	}

	public int id() {
		return this.id;
	}

	public CanvasHolder<?> root() {
		return this.root;
	}

	public @Unmodifiable List<DrawAction> getAvailableTools() {
		return this.availableTools;
	}

	@Unmodifiable
	List<ItemStack> getAvailableColors() {
		return this.painterPalette.getColors();
	}
}
