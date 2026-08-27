/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import dev.lambdaurora.aurorascanvas.client.ClientCanvasBlockEntityData;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasMeshBaker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedCanvasModel extends ForwardingBakedModel {
	public BakedCanvasModel(BakedModel baseModel) {
		this.wrapped = baseModel;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		super.emitBlockQuads(world, state, pos, randomSupplier, context);

		this.emitBlockMesh(world, pos, context);
	}

	protected void emitBlockMesh(BlockAndTintGetter world, BlockPos pos, RenderContext context) {
		var attachment = world.getBlockEntityRenderData(pos);
		if (attachment instanceof ClientCanvasBlockEntityData.RenderAttachmentData(var meshes)) {
			meshes.forEach(mesh -> mesh.outputTo(context.getEmitter()));
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		super.emitItemQuads(stack, randomSupplier, context);

		var canvases = stack.get(AurorasCanvasRegistry.CANVAS_COMPONENT_TYPE);
		if (canvases != null && !canvases.canvas().isEmpty()) {
			CanvasMeshBaker.buildMesh(new PlacedCanvas(canvases.canvas(), Direction.NORTH))
					.outputTo(context.getEmitter());
		}
	}
}
