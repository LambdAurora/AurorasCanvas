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
import net.minecraft.client.renderer.block.BlockModelShaper;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedGlassboardModel extends UnbakedCanvasModel {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Int2ObjectMap<UnbakedModel> models;

	public UnbakedGlassboardModel(
			ModelResourceLocation id, UnbakedModel baseModel, UnbakedModel missingModel, ResourceManager resourceManager
	) {
		super(baseModel);
		this.models = loadModelParts(id, missingModel, resourceManager);
	}

	private static Int2ObjectMap<UnbakedModel> loadModelParts(ModelResourceLocation id, UnbakedModel missingModel, ResourceManager resourceManager) {
		Int2ObjectMap<UnbakedModel> models = new Int2ObjectOpenHashMap<>();
		var deserializationContext = new BlockModelDefinition.Context();

		String prefix = "";
		if (id.id().getPath().contains("waxed")) {
			prefix = "waxed/";
		}

		Block block = BuiltInRegistries.BLOCK.get(AurorasCanvas.id(id.id().getPath()));

		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				var identifier = new ModelResourceLocation(AurorasCanvas.id(GlassboardModel.getModelPath(prefix, corner, type)), id.variant());

				if (block != Blocks.AIR) {
					var resourceId = AurorasCanvas.id("blockstates/" + identifier.id().getPath() + ".json");
					var resource = resourceManager.getResource(resourceId);

					if (resource.isEmpty()) {
						LOGGER.warn("Could not load glassboard model part ({}, {}): could not locate the blockstate file.", corner, type);
					} else {
						try (var reader = new InputStreamReader(resource.get().open())) {
							deserializationContext.setDefinition(block.getStateDefinition());
							var definition = BlockModelDefinition.fromStream(deserializationContext, reader);

							if (definition.isMultiPart()) {
								models.put(GlassboardModel.getCornerDataIndex(corner, type), definition.getMultiPart());
							} else {
								models.put(
										GlassboardModel.getCornerDataIndex(corner, type),
										loadModelVariants(identifier, deserializationContext, definition, missingModel)
								);
							}
						} catch (IOException e) {
							LOGGER.warn("Could not load glassboard model part ({}, {}):", corner, type, e);
						}
					}
				}
			}
		}

		return models;
	}

	private static UnbakedModel loadModelVariants(
			ModelResourceLocation id, BlockModelDefinition.Context context, BlockModelDefinition definition, UnbakedModel missingModel
	) {
		final var loaded = new HashMap<ModelResourceLocation, UnbakedModel>();

		definition.getVariants()
				.forEach(
						(variant, data) -> {
							try {
								context.getDefinition().getPossibleStates().stream()
										.filter(BlockStateModelLoader.predicate(context.getDefinition(), variant))
										.forEach(
												state -> {
													var key = BlockModelShaper.stateToModelLocation(id.id(), state);

													var loadedModel = loaded.put(key, data);
													if (loadedModel != null) {
														loaded.put(key, missingModel);
														throw new RuntimeException(
																"Overlapping definition with: "
																		+ (definition.getVariants().entrySet().stream().filter(entry -> entry.getValue() == loadedModel).findFirst().get())
																		.getKey()
														);
													}
												}
										);
							} catch (Exception e) {
								LOGGER.warn(
										"Exception loading blockstate definition: '{}' for variant: '{}': {}",
										id,
										variant,
										e.getMessage()
								);
							}
						}
				);

		return loaded.getOrDefault(id, missingModel);
	}

	@Override
	public Collection<Identifier> getDependencies() {
		var dependencies = new ArrayList<>(super.getDependencies());
		this.models.values().stream().map(UnbakedModel::getDependencies).forEach(dependencies::addAll);
		return dependencies;
	}

	@Override
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		super.resolveParents(models);
		this.models.values().forEach(model -> model.resolveParents(models));
	}

	@Override
	public BakedModel bake(
			ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state
	) {
		var baseModel = this.bakeBaseModel(baker, spriteGetter, state);

		return new BakedGlassboardModel(baseModel, this.bakeAllConnectingModels(baker, spriteGetter, state, baseModel));
	}

	private Int2ObjectMap<List<BakedModel>> bakeAllConnectingModels(
			ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, BakedModel baseModel
	) {
		var map = new Int2ObjectOpenHashMap<List<BakedModel>>();

		map.put(0, List.of(baseModel));

		var bakedModels = new Int2ObjectOpenHashMap<BakedModel>();
		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				int id = GlassboardModel.getCornerDataIndex(corner, type);
				bakedModels.put(id, this.models.get(id).bake(baker, spriteGetter, state));
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
