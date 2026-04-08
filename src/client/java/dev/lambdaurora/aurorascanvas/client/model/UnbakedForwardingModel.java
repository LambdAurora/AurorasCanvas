/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents an unbaked model that forwards another unbaked model.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public record UnbakedForwardingModel(UnbakedModel baseModel, Function<BakedModel, BakedModel> factory) implements UnbakedModel {
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
		return this.factory.apply(Objects.requireNonNull(this.baseModel.bake(modelBaker, textureGetter, modelState, modelId)));
	}
}
