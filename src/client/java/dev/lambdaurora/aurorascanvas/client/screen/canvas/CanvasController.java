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
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem.PainterPaletteInventory;
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
	private final IndexedCanvas root;
	private final PainterPaletteInventory painterPalette;
	private final List<DrawAction> availableTools;
	final TrackedCanvasHandler canvas;
	final Identifier backgroundTexture;

	DrawAction currentAction;
	DrawModifier currentModifier;

	public CanvasController(int id, Level level, CanvasItem item, PainterPaletteInventory painterPalette, IndexedCanvas root) {
		this.id = id;
		this.root = root;
		this.painterPalette = painterPalette;
		this.availableTools = painterPalette.getAvailableTools(level.enabledFeatures());
		this.canvas = new TrackedCanvasHandler(this.root.canvas());
		this.backgroundTexture = AurorasCanvas.id("textures/block/canvas/" + item.getBackground() + ".png");

		this.currentAction = DrawAction.DEFAULT;
		this.currentModifier = Objects.requireNonNull(DrawModifier.fromItem(painterPalette.getSelectedColor()));
	}

	public int id() {
		return this.id;
	}

	public IndexedCanvas root() {
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
