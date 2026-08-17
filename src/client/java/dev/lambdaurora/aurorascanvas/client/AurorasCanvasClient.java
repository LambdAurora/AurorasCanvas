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
import dev.lambdaurora.aurorascanvas.client.model.UnbakedCanvasModel;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasItemRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasPressBlockEntityRenderer;
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
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

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

		this.registerBlackboardItemRenderer(BLACKBOARD.block().value());
		this.registerBlackboardItemRenderer(CHALKBOARD.block().value());
		this.registerBlackboardItemRenderer(GLASSBOARD.block().value());
		this.registerBlackboardItemRenderer(WAXED_BLACKBOARD.block().value());
		this.registerBlackboardItemRenderer(WAXED_CHALKBOARD.block().value());
		this.registerBlackboardItemRenderer(WAXED_GLASSBOARD.block().value());

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
	}

	private void registerBlackboardItemRenderer(CanvasBlock blackboard) {
		@SuppressWarnings("deprecation") var id = blackboard.builtInRegistryHolder().key().identifier();
		var modelId = new ModelResourceLocation(new Identifier(id.getNamespace(), id.getPath() + "_base"),
				"inventory");
		BuiltinItemRendererRegistry.INSTANCE.register(blackboard, new CanvasItemRenderer(modelId));
		ModelLoadingPlugin.register(context -> context.addModels(modelId, BLACKBOARD_MASK));
	}
}
