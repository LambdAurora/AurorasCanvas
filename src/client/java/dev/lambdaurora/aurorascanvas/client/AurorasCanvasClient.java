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
import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.block.entity.BlackboardBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public final class AurorasCanvasClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ModelResourceLocation BLACKBOARD_MASK = new ModelResourceLocation(AurorasCanvas.id("blackboard_mask"), "inventory");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.put(RenderType.cutout(),
				GLASSBOARD_BLOCK,
				WAXED_GLASSBOARD_BLOCK
		);

		BlockEntityRendererFactories.register(BLACKBOARD_PRESS_BLOCK_ENTITY, BlackboardPressBlockEntityRenderer::new);

		this.registerBlackboardItemRenderer(BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(GLASSBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_GLASSBOARD_BLOCK);

		ModelLoadingPlugin.register(context -> {
			BlackboardPressBlockEntityRenderer.initModels(context);

			context.modifyModelOnLoad().register((model, ctx) -> {
				if (ctx.id() instanceof ModelResourceLocation modelId && !modelId.getVariant().equals("inventory"))
					if (modelId.getPath().endsWith("board")) {
						return UnbakedBlackboardModel.of(modelId, model,
								(partId, m) -> {
									var modelLoader = (ModelLoaderAccessor) ctx.loader();
									modelLoader.invokePutModel(partId, m);
									modelLoader.getModelsToBake().put(partId, m);
								}
						);
					}

				return model;
			});

			BlackboardBlockEntity.markAllMeshesDirty();
		});
	}

	private void registerBlackboardItemRenderer(BlackboardBlock blackboard) {
		@SuppressWarnings("deprecation") var id = blackboard.builtInRegistryHolder().key().identifier();
		var modelId = new ModelResourceLocation(new Identifier(id.getNamespace(), id.getPath() + "_base"),
				"inventory");
		BuiltinItemRendererRegistry.INSTANCE.register(blackboard, new BlackboardItemRenderer(modelId));
		ModelLoadingPlugin.register(context -> context.addModels(modelId, BLACKBOARD_MASK));
	}
}
