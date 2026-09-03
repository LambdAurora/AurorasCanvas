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
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.client.model.glass.GlassboardModel.Corner;
import dev.lambdaurora.aurorascanvas.client.model.glass.GlassboardModel.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class UnbakedGlassboardModel implements BlockStateModel.UnbakedRoot {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final BlockStateModel.UnbakedRoot baseModel;
	private final boolean waxed;

	public UnbakedGlassboardModel(BlockStateModel.UnbakedRoot baseModel, boolean waxed) {
		this.baseModel = baseModel;
		this.waxed = waxed;
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.baseModel.resolveDependencies(resolver);

		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				var prefix = waxed ? "waxed/" : "";
				resolver.markDependency(AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix, corner, type)));
				resolver.markDependency(AurorasCanvas.id("block/" + GlassboardModel.getModelPath("pane/" + prefix, corner, type)));
			}
		}
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return this.baseModel.visualEqualityGroup(blockState);
	}

	@Override
	public BlockStateModel bake(BlockState blockState, ModelBaker modelBakery) {
		var base = this.baseModel.bake(blockState, modelBakery);

		return new BakedGlassboardModel(base, this.bakeAllConnectingModels(blockState, modelBakery, base));
	}

	private Int2ObjectMap<FabricBlockStateModel> bakeAllConnectingModels(BlockState blockState, ModelBaker baker, BlockStateModel baseModel) {
		var map = new Int2ObjectOpenHashMap<FabricBlockStateModel>();

		map.put(0, baseModel);

		var bakedModels = new Int2ObjectOpenHashMap<BlockStateModelPart>();
		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				var prefix = waxed ? "waxed/" : "";
				if (blockState.getValue(GlassCanvasBlock.PANE)) {
					prefix = "pane/" + prefix;
				}

				var id = AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix, corner, type));
				var numericId = GlassboardModel.getCornerDataIndex(corner, type);
				var variant = new Variant(id, Variant.SimpleModelState.DEFAULT.withY(GlassboardModel.partYRot(blockState.getValue(GlassCanvasBlock.FACING))));
				bakedModels.put(numericId, variant.bake(baker));
			}
		}

		for (int i = 1; i <= GlassboardModel.ALL_MASK; i++) {
			var models = new ArrayList<BlockStateModelPart>();

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
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(GlassboardModel.getCornerDataIndex(Corner.RIGHT_DOWN, Type.NONE)));
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

			map.put(i, new BakedGlassboardModel.Part(models));
		}

		return map;
	}
}
