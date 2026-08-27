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
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.client.model.AurorasCanvasModelLayers;
import dev.lambdaurora.aurorascanvas.client.model.entity.EaselEntityModel;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

/**
 * Represents the entity renderer for the easel entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class EaselEntityRenderer extends LivingEntityRenderer<EaselEntity, EaselEntityModel<EaselEntity>> {
	private static final Identifier TEXTURE = AurorasCanvas.id("textures/entity/easel/wood.png");

	public EaselEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new EaselEntityModel<>(context.bakeLayer(AurorasCanvasModelLayers.EASEL)), 0);
		this.addLayer(new CanvasRenderLayer(this, context.getItemRenderer()));
	}

	@Override
	public Identifier getTextureLocation(EaselEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void setupRotations(EaselEntity entity, PoseStack matrices, float bob, float yBodyRot, float partialTicks, float scale) {
		matrices.mulPose(Axis.YP.rotationDegrees(180.f - yBodyRot));

		float lastHitDelta = (float) (entity.level().getGameTime() - entity.lastHit) + partialTicks;
		if (lastHitDelta < 5.f) {
			matrices.mulPose(Axis.YP.rotationDegrees(Mth.sin(lastHitDelta / 1.5f * (float) Math.PI) * 3.f));
		}
	}

	@Override
	protected boolean shouldShowName(EaselEntity entity) {
		double d = this.entityRenderDispatcher.distanceToSqr(entity);
		float maxDistance = entity.isCrouching() ? 32.f : 64.f;
		return !(d >= maxDistance * maxDistance) && entity.isCustomNameVisible();
	}

	private static class CanvasRenderLayer extends RenderLayer<EaselEntity, EaselEntityModel<EaselEntity>> {
		private final ItemRenderer itemRenderer;

		public CanvasRenderLayer(RenderLayerParent<EaselEntity, EaselEntityModel<EaselEntity>> renderer, ItemRenderer itemRenderer) {
			super(renderer);
			this.itemRenderer = itemRenderer;
		}

		@Override
		public void render(
				PoseStack matrices, MultiBufferSource buffer,
				int packedLight,
				EaselEntity entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch
		) {
			var stack = entity.getItem();
			if (!stack.isEmpty()) {
				matrices.pushPose();
				var partPose = EaselEntityModel.FRONT_PART_POSE;
				matrices.scale(-1, -1, 1);
				matrices.translate(partPose.x / 16.f, -partPose.y / 16.f, partPose.z / 16.f);
				if (partPose.xRot != 0.f || partPose.yRot != 0.f || partPose.zRot != 0.f) {
					matrices.mulPose(new Quaternionf().rotationZYX(partPose.zRot, partPose.yRot, -partPose.xRot));
				}
				matrices.translate(0, 24 / 16f, -1 / 16f);

				this.itemRenderer.renderStatic(
						stack, ItemDisplayContext.FIXED,
						packedLight, OverlayTexture.NO_OVERLAY,
						matrices, buffer,
						entity.level(), entity.getId()
				);
				matrices.popPose();
			}
		}
	}
}
