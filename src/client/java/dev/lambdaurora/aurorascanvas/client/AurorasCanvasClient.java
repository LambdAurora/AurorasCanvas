/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.client.mixin.ModelBakeryAccessor;
import dev.lambdaurora.aurorascanvas.client.model.AurorasCanvasModelLayers;
import dev.lambdaurora.aurorascanvas.client.model.UnbakedCanvasModel;
import dev.lambdaurora.aurorascanvas.client.model.entity.EaselEntityModel;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasItemRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasPressBlockEntityRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.EaselEntityRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.GlassCanvasItemRenderer;
import dev.lambdaurora.aurorascanvas.client.screen.PainterPaletteScreen;
import dev.lambdaurora.aurorascanvas.client.tooltip.CanvasTooltipComponent;
import dev.lambdaurora.aurorascanvas.client.tooltip.PainterPaletteTooltipComponent;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import dev.lambdaurora.aurorascanvas.tooltip.PainterPaletteTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.slf4j.Logger;

import java.util.function.Function;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ModelResourceLocation BLACKBOARD_MASK = new ModelResourceLocation(AurorasCanvas.id("blackboard_mask"), "inventory");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
				GLASSBOARD.block().value(),
				WAXED_GLASSBOARD.block().value()
		);

		BlockEntityRenderers.register(BLACKBOARD_PRESS_BLOCK_ENTITY, CanvasPressBlockEntityRenderer::new);

		this.registerCanvasItemRenderer(BLACKBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(CHALKBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WHITEBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(GLASSBOARD.block().value(), GlassCanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_BLACKBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_CHALKBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_WHITEBOARD.block().value(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_GLASSBOARD.block().value(), GlassCanvasItemRenderer::new);

		ClientCanvasBlockEntityData.init();

		MenuScreens.register(PAINTER_PALETTE_MENU_TYPE, PainterPaletteScreen::new);

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof CanvasTooltipData canvasTooltipData) {
				return new CanvasTooltipComponent(canvasTooltipData);
			} else if (data instanceof PainterPaletteTooltipData painterPaletteTooltipData) {
				return new PainterPaletteTooltipComponent(painterPaletteTooltipData.inventory());
			} else {
				return null;
			}
		});

		ColorProviderRegistry.ITEM.register(PAINTER_PALETTE_ITEM::getColor, PAINTER_PALETTE_ITEM);

		ModelLoadingPlugin.register(context -> {
			CanvasPressBlockEntityRenderer.initModels(context);

			context.modifyModelOnLoad().register((model, ctx) -> {
				if (ctx.id() instanceof ModelResourceLocation modelId && !modelId.getVariant().equals("inventory"))
					if (modelId.getPath().endsWith("board")) {
						return UnbakedCanvasModel.of(modelId, model,
								(partId, m) -> {
									var modelLoader = (ModelBakeryAccessor) ctx.loader();
									modelLoader.invokeCacheAndQueueDependencies(partId, m);
									modelLoader.getTopLevelModels().put(partId, m);
								}
						);
					}

				return model;
			});

			ClientCanvasBlockEntityData.markAllMeshesDirty();
		});

		EntityModelLayerRegistry.registerModelLayer(AurorasCanvasModelLayers.EASEL, EaselEntityModel::createBodyLayer);
		EntityRendererRegistry.register(EASEL_ENTITY_TYPE, EaselEntityRenderer::new);

		AurorasCanvasClientNetworking.init();
	}

	private void registerCanvasItemRenderer(CanvasBlock canvas, Function<ModelResourceLocation, CanvasItemRenderer> factory) {
		@SuppressWarnings("deprecation") var id = canvas.builtInRegistryHolder().key().identifier();
		var modelId = new ModelResourceLocation(id.withSuffix("_base"), "inventory");
		BuiltinItemRendererRegistry.INSTANCE.register(canvas, factory.apply(modelId));
		ModelLoadingPlugin.register(context -> context.addModels(modelId, BLACKBOARD_MASK));
	}
}
