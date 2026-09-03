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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class UnbakedCanvasModel implements BlockStateModel.UnbakedRoot {
	protected final BlockStateModel.UnbakedRoot baseModel;

	public UnbakedCanvasModel(BlockStateModel.UnbakedRoot baseModel) {
		this.baseModel = baseModel;
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.baseModel.resolveDependencies(resolver);
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return this.baseModel.visualEqualityGroup(blockState);
	}

	@Override
	public BlockStateModel bake(BlockState blockState, ModelBaker modelBakery) {
		return new BakedCanvasModel(this.bakeBaseModel(blockState, modelBakery));
	}

	protected BlockStateModel bakeBaseModel(
			BlockState blockState, ModelBaker modelBakery
	) {
		return Objects.requireNonNull(this.baseModel.bake(blockState, modelBakery));
	}
}
