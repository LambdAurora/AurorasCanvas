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
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasPressBlockEntity;
import dev.lambdaurora.aurorascanvas.client.model.AurorasCanvasBlockStateDefinitions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CanvasPressBlockEntityRenderer implements BlockEntityRenderer<CanvasPressBlockEntity, CanvasPressBlockEntityRenderState> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final RandomSource RANDOM = new LegacyRandomSource(RandomSupport.generateUniqueSeed());
	private final BlockModelResolver blockModelResolver;

	public CanvasPressBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.blockModelResolver = ctx.blockModelResolver();
	}

	@Override
	public CanvasPressBlockEntityRenderState createRenderState() {
		return new CanvasPressBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(
			CanvasPressBlockEntity blockEntity,
			CanvasPressBlockEntityRenderState state,
			float partialTicks, Vec3 cameraPosition,
			ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		var parentBlockState = blockEntity.getBlockState();

		var pressPlateState = AurorasCanvasBlockStateDefinitions.CANVAS_PRESS_PRESS_PLATE_FAKE_STATE.any();
		for (var property : parentBlockState.getProperties()) {
			pressPlateState = this.setState(pressPlateState, parentBlockState, property);
		}
		this.blockModelResolver.update(state.pressPlateModel, pressPlateState, BlockDisplayContext.create());

		var screwState = AurorasCanvasBlockStateDefinitions.CANVAS_PRESS_SCREW_FAKE_STATE.any();
		for (var property : parentBlockState.getProperties()) {
			screwState = this.setState(screwState, parentBlockState, property);
		}
		this.blockModelResolver.update(state.pressPlateModel, screwState, BlockDisplayContext.create());

		state.gameTime = blockEntity.getLevel().getGameTime();
	}

	private <T extends Comparable<T>> BlockState setState(BlockState toMutate, BlockState parent, Property<T> property) {
		return toMutate.setValue(property, parent.getValue(property));
	}

	@Override
	public void submit(CanvasPressBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		{
			matrices.pushPose();

			// Woooo,,,, witness the WIP/debug code,,,,, -Lavender
			long aaa = 6 - state.gameTime % 12;
			if (aaa > 0) {
				aaa = -aaa;
			}
			matrices.translate(0, aaa / 32.f, 0);

			state.pressPlateModel.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

			{
				matrices.pushPose();

				matrices.translate(0.5, 0, 0.5);
				matrices.mulPose(Axis.YP.rotationDegrees(-(state.gameTime % 360)));
				matrices.translate(-0.5, 0, -0.5);

				state.screwModel.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

				matrices.popPose();
			}

			matrices.popPose();
		}
	}
}
