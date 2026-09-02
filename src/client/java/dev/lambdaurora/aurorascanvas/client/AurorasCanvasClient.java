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
import dev.lambdaurora.aurorascanvas.item.component.PainterPaletteInventory;
import dev.lambdaurora.aurorascanvas.tooltip.CanvasTooltipData;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.function.Function;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Identifier BLACKBOARD_MASK = AurorasCanvas.id("item/blackboard_mask");

	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockEntityRenderers.register(BLACKBOARD_PRESS_BLOCK_ENTITY, CanvasPressBlockEntityRenderer::new);

		this.registerCanvasItemRenderer(BLACKBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(CHALKBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WHITEBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(GLASSBOARD.block(), GlassCanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_BLACKBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_CHALKBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_WHITEBOARD.block(), CanvasItemRenderer::new);
		this.registerCanvasItemRenderer(WAXED_GLASSBOARD.block(), GlassCanvasItemRenderer::new);

		ClientCanvasBlockEntityData.init();

		MenuScreens.register(PAINTER_PALETTE_MENU_TYPE, PainterPaletteScreen::new);

		ClientTooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof CanvasTooltipData canvasTooltipData) {
				return new CanvasTooltipComponent(canvasTooltipData);
			} else if (data instanceof PainterPaletteInventory inventory) {
				return new PainterPaletteTooltipComponent(inventory);
			} else {
				return null;
			}
		});

		/*ColorResolverRegistry.ITEM.register(PAINTER_PALETTE_ITEM::getColor, PAINTER_PALETTE_ITEM);

		ModelLoadingPlugin.register(context -> {
			CanvasPressBlockEntityRenderer.initModels(context);

			context.modifyModelOnLoad().register((model, ctx) -> {
				if (ctx.id() instanceof ModelResourceLocation modelId && !modelId.getVariant().equals("inventory"))
					if (modelId.id().getPath().endsWith("board")) {
						return UnbakedCanvasModel.of(modelId, model, ctx.getOrLoadModel(ModelBakery.MISSING_MODEL_LOCATION));
					}

				return model;
			});

			ClientCanvasBlockEntityData.markAllMeshesDirty();
		});*/

		ModelLayerRegistry.registerModelLayer(AurorasCanvasModelLayers.EASEL, EaselEntityModel::createBodyLayer);
		EntityRendererRegistry.register(EASEL_ENTITY_TYPE, EaselEntityRenderer::new);

		AurorasCanvasClientNetworking.init();
	}

	private void registerCanvasItemRenderer(BlockEntry<? extends CanvasBlock> canvas, Function<Identifier, CanvasItemRenderer> factory) {
		var modelId = canvas.key().identifier().withPrefix("item/").withSuffix("_base");
		/*BuiltinItemRendererRegistry.INSTANCE.register(canvas.value(), factory.apply(modelId));
		ModelLoadingPlugin.register(context -> context.addModels(modelId, BLACKBOARD_MASK));*/
	}
}
