/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */package dev.lambdaurora.aurorascanvas.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedBlackboardModel implements UnbakedModel {
	protected final UnbakedModel baseModel;

	public static UnbakedBlackboardModel of(ModelResourceLocation id, UnbakedModel baseModel, BiConsumer<Identifier, UnbakedModel> modelConsumer) {
		if (id.getPath().contains("glass")) {
			return new UnbakedGlassboardModel(id, baseModel,
					Minecraft.getInstance().getResourceManager(), new BlockModelDefinition.Context(), modelConsumer
			);
		} else {
			return new UnbakedBlackboardModel(baseModel);
		}
	}

	protected UnbakedBlackboardModel(UnbakedModel baseModel) {
		this.baseModel = baseModel;
	}

	@Override
	public Collection<Identifier> getDependencies() {
		return this.baseModel.getDependencies();
	}

	@Override
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		this.baseModel.resolveParents(models);
	}

	@Override
	public BakedModel bake(
			ModelBaker modelBaker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, Identifier modelId
	) {
		return new BakedBlackboardModel(this.bakeBaseModel(modelBaker, textureGetter, modelState, modelId));
	}

	protected BakedModel bakeBaseModel(
			ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, Identifier modelId
	) {
		return Objects.requireNonNull(this.baseModel.bake(loader, textureGetter, modelState, modelId));
	}
}
