/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.tooltip;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasTexture;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Represents the blackboard tooltip component. Displays the blackboard's contents.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class CanvasTooltipComponent implements ClientTooltipComponent {
	private static final Identifier LOCK_ICON_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/cartography_table.png");
	private static final Identifier GLOWING_SPRITE = AurorasCanvas.id("glowing");

	private final Minecraft client = Minecraft.getInstance();
	private final RenderType background;
	private final List<Entry> canvases;
	private final boolean locked;

	public CanvasTooltipComponent(String background, @Unmodifiable List<Canvas> canvases, boolean locked) {
		this.background = RenderType.text(AurorasCanvas.id("textures/block/canvas/" + background + ".png"));
		this.canvases = canvases.stream().map(canvas -> new Entry(CanvasTexture.fromCanvas(canvas), canvas.isGlowing())).toList();
		this.locked = locked;
	}

	public CanvasTooltipComponent(CanvasTooltipData data) {
		this(data.background(), data.canvases(), data.locked());
	}

	@Override
	public int getHeight() {
		return 128 + 2;
	}

	@Override
	public int getWidth(Font font) {
		return 128;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		var matrices = graphics.pose();
		var vertexConsumers = this.client.renderBuffers().bufferSource();
		matrices.pushPose();
		matrices.translate(x, y, 0);
		matrices.scale(128.f, 128.f, 1);

		var model = matrices.last().pose();

		this.quad(this.background, 0.f, 0.f, 1.f, 1.f, model, vertexConsumers, LightTexture.FULL_BRIGHT);

		matrices.translate(0, 0, 1);

		var entry = this.selectEntry();

		entry.texture.render(model, vertexConsumers, LightTexture.FULL_BRIGHT, false);

		if (entry.glowing) {
			matrices.pushPose();
			matrices.scale(1 / 128.f, 1/128.f, 1);
			matrices.translate(0, 0, 1);

			graphics.blitSprite(GLOWING_SPRITE, 0, 0, 128, 128);

			matrices.popPose();
		}

		if (this.locked) {
			matrices.translate(.5f, .5f, 1);
			matrices.scale(.5f, .5f, 1.f);
			model = matrices.last().pose();
			var locked = RenderType.text(LOCK_ICON_TEXTURE);
			this.quad(locked, 0.f, .6484375f, .2421875f, .890625f, model, vertexConsumers, LightTexture.FULL_BRIGHT);
		}

		vertexConsumers.endBatch();
		matrices.popPose();
	}

	private Entry selectEntry() {
		if (this.canvases.size() == 1) {
			return this.canvases.get(0);
		}

		int period = 5;
		int fullCycle = this.canvases.size() * period;

		int index = Math.toIntExact((System.currentTimeMillis() / 1000) % fullCycle / period);
		return this.canvases.get(index);
	}

	private void quad(
			RenderType renderType, float uMin, float vMin, float uMax, float vMax,
			Matrix4f model, MultiBufferSource vertexConsumers, int light
	) {
		var vertices = vertexConsumers.getBuffer(renderType);
		vertices.addVertex(model, 0.f, 1.f, 0.f).setColor(255, 255, 255, 255)
				.setUv(uMin, vMax).setLight(light);
		vertices.addVertex(model, 1.f, 1.f, 0.f).setColor(255, 255, 255, 255)
				.setUv(uMax, vMax).setLight(light);
		vertices.addVertex(model, 1.f, 0.f, 0.f).setColor(255, 255, 255, 255)
				.setUv(uMax, vMin).setLight(light);
		vertices.addVertex(model, 0.f, 0.f, 0.f).setColor(255, 255, 255, 255)
				.setUv(uMin, vMin).setLight(light);
	}

	private record Entry(CanvasTexture texture, boolean glowing) {}
}
