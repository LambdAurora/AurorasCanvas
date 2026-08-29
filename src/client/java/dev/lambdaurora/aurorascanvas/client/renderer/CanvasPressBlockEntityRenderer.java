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
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.CanvasPressBlock;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasPressBlockEntity;
import dev.lambdaurora.aurorascanvas.client.model.UnbakedVariantModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CanvasPressBlockEntityRenderer implements BlockEntityRenderer<CanvasPressBlockEntity> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Identifier PRESS_PLATE_ID = AurorasCanvas.id("blockstates/canvas_press/press_plate.json");
	public static final Identifier SCREW_ID = AurorasCanvas.id("blockstates/canvas_press/screw.json");
	public static final ModelResourceLocation PRESS_PLATE_MODEL_ID = new ModelResourceLocation(AurorasCanvas.id("canvas_press/press_plate"), "special");
	public static final ModelResourceLocation SCREW_MODEL_ID = new ModelResourceLocation(AurorasCanvas.id("canvas_press/screw"), "special");
	private static final RandomSource RANDOM = new LegacyRandomSource(RandomSupport.generateUniqueSeed());
	private final Minecraft client = Minecraft.getInstance();

	public CanvasPressBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(
			CanvasPressBlockEntity entity, float tickDelta,
			PoseStack matrices, MultiBufferSource vertexConsumers,
			int light, int overlay
	) {
		var level = entity.getLevel();

		if (level == null) return;

		BlockState state = entity.getBlockState();
		BlockPos pos = entity.getBlockPos();

		var pressPlateModel = client.getModelManager().getModel(PRESS_PLATE_MODEL_ID);
		var screwModel = client.getModelManager().getModel(SCREW_MODEL_ID);

		{
			matrices.pushPose();

			// Woooo,,,, witness the WIP/debug code,,,,, -Lavender
			long aaa = 6 - level.getGameTime() % 12;
			if (aaa > 0) {
				aaa = -aaa;
			}
			matrices.translate(0, aaa / 32.f, 0);

			client.getBlockRenderer().getModelRenderer().tesselateBlock(
					level, pressPlateModel, state, pos,
					matrices, vertexConsumers.getBuffer(RenderType.solid()), true,
					RANDOM, state.getSeed(pos), OverlayTexture.NO_OVERLAY
			);

			{
				matrices.pushPose();

				matrices.translate(0.5, 0, 0.5);
				matrices.mulPose(Axis.YP.rotationDegrees(-(level.getGameTime() % 360)));
				matrices.translate(-0.5, 0, -0.5);

				client.getBlockRenderer().getModelRenderer().tesselateBlock(
						level, screwModel, state, pos,
						matrices, vertexConsumers.getBuffer(RenderType.solid()), true,
						RANDOM, state.getSeed(pos), OverlayTexture.NO_OVERLAY
				);

				matrices.popPose();
			}

			matrices.popPose();
		}
	}

	public static void initModels(ModelLoadingPlugin.Context context) {
		boolean[] firstRun = {true};

		context.modifyModelOnLoad().register((model, ctx) -> {
			if (firstRun[0]) {
				firstRun[0] = false;

				var modelLoader = ctx.loader();

				var pressModel = initModel(PRESS_PLATE_ID, PRESS_PLATE_MODEL_ID);
				modelLoader.registerModelAndLoadDependencies(PRESS_PLATE_MODEL_ID, pressModel);

				var screwModel = initModel(SCREW_ID, SCREW_MODEL_ID);
				modelLoader.registerModelAndLoadDependencies(SCREW_MODEL_ID, screwModel);
			}

			return model;
		});
	}

	private static @Nullable UnbakedModel initModel(Identifier resourceId, ModelResourceLocation modelId) {
		var model = Minecraft.getInstance().getResourceManager().getResource(resourceId).map(resource -> {
			try (var reader = new InputStreamReader(resource.open())) {
				var context = new BlockModelDefinition.Context();
				context.setDefinition(AurorasCanvasRegistry.CANVAS_PRESS.block().value().getStateDefinition());
				var map = BlockModelDefinition.fromStream(context, reader);
				return new UnbakedVariantModel<>(AurorasCanvasRegistry.CANVAS_PRESS.block().value(), map.getVariants(), List.of(CanvasPressBlock.WATERLOGGED));
			} catch (IOException e) {
				LOGGER.warn("Failed to load the blackboard \"{}\" model.", modelId, e);
				return null;
			}
		});

		if (model.isEmpty()) {
			LOGGER.warn("Failed to load the blackboard \"{}\" model: missing file.", modelId);
			return null;
		} else {
			return model.get();
		}
	}
}