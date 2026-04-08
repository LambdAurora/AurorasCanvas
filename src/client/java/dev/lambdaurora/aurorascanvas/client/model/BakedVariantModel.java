package dev.lambdaurora.aurorascanvas.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a baked model which is using a variant map to adapt to depending on block states.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BakedVariantModel extends ForwardingBakedModel {
	private final Map<BlockState, BakedModel> variantMap;

	public BakedVariantModel(Map<BlockState, BakedModel> variantMap) {
		this.variantMap = variantMap;
		this.wrapped = variantMap.entrySet().iterator().next().getValue();
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		var model = this.variantMap.get(state);
		if (model == null) return;

		model.emitBlockQuads(world, state, pos, randomSupplier, context);
	}
}
