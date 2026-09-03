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
import dev.lambdaurora.aurorascanvas.client.item.ItemDisplayContextConditionalProperty;
import dev.lambdaurora.aurorascanvas.client.item.PainterPaletteTintSource;
import dev.lambdaurora.aurorascanvas.client.model.AurorasCanvasModelLayers;
import dev.lambdaurora.aurorascanvas.client.model.UnbakedCanvasModel;
import dev.lambdaurora.aurorascanvas.client.model.entity.EaselEntityModel;
import dev.lambdaurora.aurorascanvas.client.model.glass.UnbakedGlassboardModel;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasItemRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasPressBlockEntityRenderer;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasTextureManager;
import dev.lambdaurora.aurorascanvas.client.renderer.EaselEntityRenderer;
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
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.function.Function;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Identifier BLACKBOARD_MASK = AurorasCanvas.id("item/blackboard_mask");

	public static final CanvasTextureManager CANVAS_TEXTURE_MANAGER = new CanvasTextureManager();

	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockEntityRenderers.register(BLACKBOARD_PRESS_BLOCK_ENTITY, CanvasPressBlockEntityRenderer::new);

		SpecialModelRenderers.ID_MAPPER.put(AurorasCanvas.id("special/canvas"), CanvasItemRenderer.UnbakedSimple.MAP_CODEC);
		SpecialModelRenderers.ID_MAPPER.put(AurorasCanvas.id("special/glass_canvas"), CanvasItemRenderer.UnbakedGlass.MAP_CODEC);

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

		ConditionalItemModelProperties.ID_MAPPER.put(AurorasCanvas.id("display_context"), ItemDisplayContextConditionalProperty.CODEC);
		ItemTintSources.ID_MAPPER.put(AurorasCanvas.id("painter_palette"), PainterPaletteTintSource.MAP_CODEC);

		ModelLoadingPlugin.register(context -> {
			context.modifyBlockModelOnLoad().register((model, ctx) -> {
				if (SIMPLE_CANVAS_BLOCKS.contains(ctx.state().getBlock())) {
					return new UnbakedCanvasModel(model);
				} else if (ctx.state().is(GLASSBOARD.block().value()) || ctx.state().is(WAXED_GLASSBOARD.block().value())) {
					return new UnbakedGlassboardModel(model, ctx.state().is(WAXED_GLASSBOARD.block().value()));
				}

				return model;
			});
		});

		ModelLoadingPlugin.register(_ -> ClientCanvasBlockEntityData.markAllMeshesDirty());

		ModelLayerRegistry.registerModelLayer(AurorasCanvasModelLayers.EASEL, EaselEntityModel::createBodyLayer);
		EntityRenderers.register(EASEL_ENTITY_TYPE, EaselEntityRenderer::new);

		AurorasCanvasClientNetworking.init();
	}

	private void registerCanvasItemRenderer(BlockEntry<? extends CanvasBlock> canvas, Function<Identifier, CanvasItemRenderer> factory) {
		var modelId = canvas.key().identifier().withPrefix("item/").withSuffix("_base");
		/*BuiltinItemRendererRegistry.INSTANCE.register(canvas.value(), factory.apply(modelId));
		ModelLoadingPlugin.register(targetDisplayContext -> targetDisplayContext.addModels(modelId, BLACKBOARD_MASK));*/
	}
}
