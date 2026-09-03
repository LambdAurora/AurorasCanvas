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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
 * @version 1.2.0
 * @since 1.0.0
 */
public class EaselEntityRenderer extends LivingEntityRenderer<EaselEntity, EaselEntityRenderState, EaselEntityModel<EaselEntityRenderState>> {
	private static final Identifier TEXTURE = AurorasCanvas.id("textures/entity/easel/wood.png");

	public EaselEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new EaselEntityModel<>(context.bakeLayer(AurorasCanvasModelLayers.EASEL)), 0);
		this.addLayer(new CanvasRenderLayer(this));
	}

	@Override
	public Identifier getTextureLocation(EaselEntityRenderState entity) {
		return TEXTURE;
	}

	@Override
	public EaselEntityRenderState createRenderState() {
		return new EaselEntityRenderState();
	}

	@Override
	public void extractRenderState(EaselEntity entity, EaselEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.FIXED, entity);
		state.wiggle = (float) (entity.level().getGameTime() - entity.lastHit) + partialTicks;
	}

	@Override
	protected void setupRotations(EaselEntityRenderState entity, PoseStack matrices, float yBodyRot, float scale) {
		matrices.mulPose(Axis.YP.rotationDegrees(180.f - yBodyRot));

		if (entity.wiggle < 5.f) {
			matrices.mulPose(Axis.YP.rotationDegrees(Mth.sin(entity.wiggle / 1.5f * (float) Math.PI) * 3.f));
		}
	}

	@Override
	protected boolean shouldShowName(EaselEntity entity, final double distanceToCameraSq) {
		float maxDistance = entity.isCrouching() ? 32.f : 64.f;
		return !(distanceToCameraSq >= maxDistance * maxDistance) && entity.isCustomNameVisible();
	}

	private static class CanvasRenderLayer extends RenderLayer<EaselEntityRenderState, EaselEntityModel<EaselEntityRenderState>> {
		public CanvasRenderLayer(RenderLayerParent<EaselEntityRenderState, EaselEntityModel<EaselEntityRenderState>> renderer) {
			super(renderer);
		}

		@Override
		public void submit(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int packedLight, EaselEntityRenderState state, float yRot, float xRot) {
			if (!state.item.isEmpty()) {
				matrices.pushPose();
				var partPose = EaselEntityModel.FRONT_PART_POSE;
				matrices.scale(-1, -1, 1);
				matrices.translate(partPose.x() / 16.f, -partPose.y() / 16.f, partPose.z() / 16.f);
				if (partPose.xRot() != 0.f || partPose.yRot() != 0.f || partPose.zRot() != 0.f) {
					matrices.mulPose(new Quaternionf().rotationZYX(partPose.zRot(), partPose.yRot(), -partPose.xRot()));
				}
				matrices.translate(0, 24 / 16f, -1 / 16f);

				state.item.submit(matrices, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
				matrices.popPose();
			}
		}
	}
}
