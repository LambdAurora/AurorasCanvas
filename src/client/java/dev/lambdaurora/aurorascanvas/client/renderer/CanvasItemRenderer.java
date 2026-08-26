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
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;
import dev.lambdaurora.aurorascanvas.client.AurorasCanvasClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the dynamic item renderer of canvases.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class CanvasItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private final ModelResourceLocation modelId;

	public CanvasItemRenderer(ModelResourceLocation modelId) {
		this.modelId = modelId;
	}

	@Override
	public void render(
			ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light, int overlay
	) {
		var modelManager = Minecraft.getInstance().getModelManager();
		var itemRenderer = Minecraft.getInstance().getItemRenderer();

		var model = modelManager.getModel(this.modelId);

		matrices.pushPose();

		matrices.translate(0.5, 0.5, 0.5);
		boolean leftHanded = mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		if (mode == ItemDisplayContext.HEAD) {
			var maskModel = modelManager.getModel(AurorasCanvasClient.BLACKBOARD_MASK);
			itemRenderer.render(
					stack, mode,
					false, matrices, vertexConsumers, light, overlay, maskModel
			);
		}

		matrices.pushPose();
		this.renderCanvas(stack, mode, matrices, vertexConsumers, light, leftHanded, model);
		matrices.popPose();

		itemRenderer.render(
				stack, mode,
				leftHanded,
				matrices, vertexConsumers, light, overlay, model
		);

		matrices.popPose();
	}

	protected void applyPose(ItemDisplayContext mode, PoseStack matrices, boolean leftHanded, BakedModel model) {
		float z = .933f;
		if (mode == ItemDisplayContext.HEAD) {
			matrices.translate(0.5, 0.5, z);
			matrices.scale(-1, -1, 1);
		} else if (mode == ItemDisplayContext.GUI) {
			matrices.translate(0.27, -0.08, 0);
			matrices.scale(-1, -1, 1);
		} else if (mode == ItemDisplayContext.GROUND) {
			matrices.translate(0.125, 0.5, 0.23333333);
			matrices.scale(-1, -1, 1);
		} else if (mode == ItemDisplayContext.FIXED) {
			matrices.translate(0.5, 0.5, 15 / 16.0 - 0.01);
			matrices.scale(-1, -1, 1);
		} else if (mode != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				&& mode != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && !mode.firstPerson()) {
			matrices.scale(-1, -1, 1);
		}

		model.getTransforms().getTransform(mode).apply(leftHanded, matrices);
		matrices.translate(0, 0, -0.5);

		if (mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				|| mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| mode.firstPerson()) {
			matrices.translate(0.5, 0.5, z);
			matrices.scale(-1, -1, 1);
		}
	}

	protected void renderCanvas(
			ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light,
			boolean leftHanded, BakedModel model
	) {
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null) {
			var canvases = SimpleCanvasHolder.TYPE.fromNbt(nbt);
			var canvas = canvases.canvas();

			if (!canvas.isEmpty()) {
				this.applyPose(mode, matrices, leftHanded, model);

				CanvasTexture.fromCanvas(canvas)
						.render(
								matrices.last().pose(), vertexConsumers,
								canvas.isGlowing() ? LightTexture.FULL_BLOCK : light,
								false
						);
			}
		}
	}
}
