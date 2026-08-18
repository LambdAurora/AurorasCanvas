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
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
	public GlassCanvasItemRenderer(ModelResourceLocation modelId) {
		super(modelId);
	}

	@Override
	protected void renderCanvas(
			ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light,
			boolean leftHanded, BakedModel model
	) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && (
				nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)
						|| nbt.contains("front", Tag.TAG_COMPOUND)
						|| nbt.contains("back", Tag.TAG_COMPOUND)
		)) {
			this.applyPose(mode, matrices, leftHanded, model);

			matrices.pushPose();
			{
				matrices.translate(0, 0, 1 / 16.f);
				matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0, 0);
				var back = Canvas.fromNbt(nbt.getCompound("back"));
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

			var canvas = this.getFrontCanvas(nbt);
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

	private Canvas getFrontCanvas(CompoundTag nbt) {
		if (nbt.contains("pixels", CompoundTag.TAG_BYTE_ARRAY)) {
			return Canvas.fromNbt(nbt);
		} else {
			return Canvas.fromNbt(nbt.getCompound("front"));
		}
	}
}
