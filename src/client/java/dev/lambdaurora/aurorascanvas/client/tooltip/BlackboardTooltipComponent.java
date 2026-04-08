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
import dev.lambdaurora.aurorascanvas.tooltip.BlackboardTooltipData;
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
import org.joml.Matrix4f;

/**
 * Represents the blackboard tooltip component. Displays the blackboard's contents.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BlackboardTooltipComponent implements ClientTooltipComponent {
	private static final Identifier LOCK_ICON_TEXTURE = new Identifier("textures/gui/container/cartography_table.png");
	private static final Identifier GLOW_TEXTURE = AurorasCanvas.id("textures/gui/glowing_sprite.png");

	private final Minecraft client = Minecraft.getInstance();
	private final CanvasTexture texture;
	private final RenderType background;
	private final Canvas canvas;
	private final boolean locked;

	public BlackboardTooltipComponent(String background, Canvas canvas, boolean locked) {
		this.background = RenderType.text(AurorasCanvas.id("textures/block/blackboard/" + background + ".png"));
		this.canvas = canvas;
		this.locked = locked;
		this.texture = CanvasTexture.fromCanvas(canvas);
	}

	public BlackboardTooltipComponent(BlackboardTooltipData data) {
		this(data.background(), data.canvas(), data.locked());
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
		this.texture.render(model, vertexConsumers, LightTexture.FULL_BRIGHT, false);

		if (this.canvas.isLit()) {
			matrices.pushPose();
			matrices.translate(0, 0, 1);
			model = matrices.last().pose();

			var glow = RenderType.text(GLOW_TEXTURE);

			float speed = 600.f;
			float offset = ((System.currentTimeMillis() % (int) speed) / speed);

			offset *= 4.f;
			offset = (float) (Math.floor(offset) / 4.f);

			this.quad(glow, 0.f, offset, 1.f, offset + (0.25f), model, vertexConsumers, LightTexture.FULL_BRIGHT);

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

	private void quad(
			RenderType renderType, float uMin, float vMin, float uMax, float vMax,
			Matrix4f model, MultiBufferSource vertexConsumers, int light
	) {
		var vertices = vertexConsumers.getBuffer(renderType);
		vertices.vertex(model, 0.f, 1.f, 0.f).color(255, 255, 255, 255)
				.uv(uMin, vMax).uv2(light).endVertex();
		vertices.vertex(model, 1.f, 1.f, 0.f).color(255, 255, 255, 255)
				.uv(uMax, vMax).uv2(light).endVertex();
		vertices.vertex(model, 1.f, 0.f, 0.f).color(255, 255, 255, 255)
				.uv(uMax, vMin).uv2(light).endVertex();
		vertices.vertex(model, 0.f, 0.f, 0.f).color(255, 255, 255, 255)
				.uv(uMin, vMin).uv2(light).endVertex();
	}
}
