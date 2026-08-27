/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.holder.GlassCanvasHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the dynamic item renderer of glass canvases.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class GlassCanvasItemRenderer extends CanvasItemRenderer {
	public GlassCanvasItemRenderer(Identifier modelId) {
		super(modelId);
	}

	@Override
	protected void renderCanvas(
			ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light,
			boolean leftHanded, BakedModel model
	) {
		var canvases = stack.get(AurorasCanvasRegistry.GLASS_CANVAS_COMPONENT_TYPE);
		if (canvases != null) {
			this.applyPose(mode, matrices, leftHanded, model);

			var back = canvases.back();
			if (!back.isEmpty()) {
				matrices.pushPose();
				{
					matrices.translate(0, 0, 1 / 16.f);
					matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0, 0);
					CanvasTexture.fromCanvas(back)
							.render(
									matrices.last().pose(), vertexConsumers,
									back.isGlowing() ? LightTexture.FULL_BLOCK : light,
									false
							);

					CanvasTexture.fromCanvas(back)
							.render(
									matrices.last().pose(), vertexConsumers,
									back.isGlowing() ? LightTexture.FULL_BLOCK : light,
									true
							);
				}
				matrices.popPose();
			}

			var canvas = canvases.front();
			if (!canvas.isEmpty()) {
				CanvasTexture.fromCanvas(canvas)
						.render(
								matrices.last().pose(), vertexConsumers,
								canvas.isGlowing() ? LightTexture.FULL_BLOCK : light,
								false
						);

				CanvasTexture.fromCanvas(canvas)
						.render(
								matrices.last().pose(), vertexConsumers,
								canvas.isGlowing() ? LightTexture.FULL_BLOCK : light,
								true
						);
			}
		}
	}
}
