/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.client.ClientCanvasBlockEntityData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class BakedCanvasModel extends WrapperBlockStateModel {
	public BakedCanvasModel(BlockStateModel baseModel) {
		this.wrapped = baseModel;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		super.emitQuads(emitter, level, pos, state, random, cullTest);
		this.emitBlockMesh(level, pos, emitter);
	}

	protected void emitBlockMesh(BlockAndTintGetter world, BlockPos pos, QuadEmitter emitter) {
		var attachment = world.getBlockEntityRenderData(pos);
		if (attachment instanceof ClientCanvasBlockEntityData.RenderAttachmentData(var meshes)) {
			meshes.forEach(mesh -> mesh.outputTo(emitter));
		}
	}
}
