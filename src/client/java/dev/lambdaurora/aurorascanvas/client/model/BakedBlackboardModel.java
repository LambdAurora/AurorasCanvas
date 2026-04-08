package dev.lambdaurora.aurorascanvas.client.model;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasMeshBaker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.LightTexture;
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
public class BakedBlackboardModel extends ForwardingBakedModel {
	public BakedBlackboardModel(BakedModel baseModel) {
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
		var attachment = ((RenderAttachedBlockView) world).getBlockEntityRenderAttachment(pos);
		if (attachment instanceof Mesh mesh) {
			mesh.outputTo(context.getEmitter());
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		super.emitItemQuads(stack, randomSupplier, context);

		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)) {
			var canvas = Canvas.fromNbt(nbt);
			CanvasMeshBaker.buildMesh(
							canvas,
							Direction.NORTH,
							canvas.isLit() ? LightTexture.FULL_BLOCK : 0
					)
					.outputTo(context.getEmitter());
		}
	}
}
