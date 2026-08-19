/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model.glass;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.client.model.UnbakedCanvasModel;
import dev.lambdaurora.aurorascanvas.client.model.glass.GlassboardModel.Corner;
import dev.lambdaurora.aurorascanvas.client.model.glass.GlassboardModel.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedGlassboardModel extends UnbakedCanvasModel {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Int2ObjectMap<Identifier> identifiers = new Int2ObjectOpenHashMap<>();
	private final String variant;

	public UnbakedGlassboardModel(
			ModelResourceLocation id, UnbakedModel baseModel, ResourceManager resourceManager,
			BlockModelDefinition.Context deserializationContext, BiConsumer<Identifier, UnbakedModel> modelConsumer
	) {
		super(baseModel);
		this.variant = id.getVariant();

		String prefix = "";
		if (id.getPath().contains("waxed")) {
			prefix = "waxed/";
		}

		Block block = BuiltInRegistries.BLOCK.get(AurorasCanvas.id(id.getPath()));

		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				var identifier = new ModelResourceLocation(AurorasCanvas.id(GlassboardModel.getModelPath(prefix, corner, type)), variant);

				this.identifiers.put(GlassboardModel.getCornerDataIndex(corner, type), identifier);

				if (block != Blocks.AIR) {
					var resourceId = AurorasCanvas.id("blockstates/" + identifier.getPath() + ".json");
					var resource = resourceManager.getResource(resourceId);

					if (resource.isEmpty()) {
						LOGGER.warn("Could not load glassboard model part ({}, {}): could not locate the blockstate file.", corner, type);
					} else {
						try (var reader = new InputStreamReader(resource.get().open())) {
							deserializationContext.setDefinition(block.getStateDefinition());
							var map = BlockModelDefinition.fromStream(deserializationContext, reader);

							map.getVariants().forEach((variant, model) -> modelConsumer.accept(
									new ModelResourceLocation(identifier.getNamespace(), identifier.getPath(), this.variant.replaceFirst("facing=\\w+,pane=\\w+", variant)),
									model
							));
						} catch (IOException e) {
							LOGGER.warn("Could not load glassboard model part ({}, {}):", corner, type, e);
						}
					}
				}
			}
		}
	}

	@Override
	public BakedModel bake(
			ModelBaker modelBaker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, Identifier modelId
	) {
		var baseModel = this.bakeBaseModel(modelBaker, textureGetter, modelState, modelId);

		return new BakedGlassboardModel(baseModel, this.bakeAllConnectingModels(modelBaker, textureGetter, modelState, modelId, baseModel));
	}

	private Int2ObjectMap<List<BakedModel>> bakeAllConnectingModels(
			ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState,
			Identifier modelId, BakedModel baseModel
	) {
		var map = new Int2ObjectOpenHashMap<List<BakedModel>>();

		map.put(0, List.of(baseModel));

		var bakedModels = new Int2ObjectOpenHashMap<BakedModel>();
		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				int id = GlassboardModel.getCornerDataIndex(corner, type);
				bakedModels.put(id, baker.getModel(this.identifiers.get(id))
						.bake(baker, textureGetter, modelState, modelId)
				);
			}
		}

		for (int i = 1; i <= GlassboardModel.ALL_MASK; i++) {
			var models = new ArrayList<BakedModel>();

			boolean left = (i & GlassboardModel.LEFT_MASK) != 0;
			boolean up = (i & GlassboardModel.UP_MASK) != 0;
			boolean right = (i & GlassboardModel.RIGHT_MASK) != 0;
			boolean down = (i & GlassboardModel.DOWN_MASK) != 0;

			// Left Up
			if (left && up) {
				if ((i & GlassboardModel.LEFT_UP_MASK) != 0)
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_UP, Type.CENTER)));
				else
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_UP, Type.INNER)));
			} else if (left) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_UP, Type.HORIZONTAL)));
			} else if (up) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_UP, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_UP, Type.NONE)));
			}

			// Right Up
			if (right && up) {
				if ((i & GlassboardModel.RIGHT_UP_MASK) != 0)
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_UP, Type.CENTER)));
				else
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_UP, Type.INNER)));
			} else if (right) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_UP, Type.HORIZONTAL)));
			} else if (up) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_UP, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_UP, Type.NONE)));
			}

			// Right Down
			if (right && down) {
				if ((i & GlassboardModel.RIGHT_DOWN_MASK) != 0)
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, Type.CENTER)));
				else
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, Type.INNER)));
			} else if (right) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, Type.HORIZONTAL)));
			} else if (down) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, GlassboardModel.Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, GlassboardModel.Type.NONE)));
			}

			// Left Down
			if (left && down) {
				if ((i & GlassboardModel.LEFT_DOWN_MASK) != 0)
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_DOWN, Type.CENTER)));
				else
					models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_DOWN, Type.INNER)));
			} else if (left) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_DOWN, Type.HORIZONTAL)));
			} else if (down) {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_DOWN, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.LEFT_DOWN, Type.NONE)));
			}

			map.put(i, models);
		}

		return map;
	}
}
