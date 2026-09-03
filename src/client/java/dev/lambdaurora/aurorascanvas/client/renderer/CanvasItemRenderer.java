/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.GlassCanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.SimpleCanvasHolder;
import dev.lambdaurora.aurorascanvas.client.AurorasCanvasClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Represents the dynamic item renderer of canvases.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class CanvasItemRenderer<T extends CanvasHolder<?, T>> implements SpecialModelRenderer<T> {
	@Override
	public void submit(@Nullable T argument, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		if (argument == null) return;

		matrices.pushPose();
		argument.streamPlacedDefault().forEach(canvas -> {
			if (!canvas.isEmpty()) {
				var canvasTextureId = AurorasCanvasClient.CANVAS_TEXTURE_MANAGER.prepareCanvasTexture(canvas.canvas());
				final int canvasLight = canvas.isGlowing() ? LightCoordsUtil.FULL_BRIGHT : lightCoords;

				submitNodeCollector.submitCustomGeometry(matrices, RenderTypes.text(canvasTextureId), (model, vertices) -> {
					this.quad(model, vertices, canvas.facing(), 0, 0, 1, 1, canvas.depth(), false, canvasLight);

					if (argument instanceof GlassCanvasHolder) {
						this.quad(model, vertices, canvas.facing().getOpposite(), 0, 0, 1, 1, canvas.depth(), true, canvasLight);
					}
				});
			}
		});
		matrices.popPose();
	}

	private void quad(
			PoseStack.Pose model, VertexConsumer vertices,
			Direction nominalFace, float left, float bottom, float right, float top, float depth, boolean mirror, int light
	) {
		if (mirror) {
			depth = .99f - depth;
		}

		switch (nominalFace) {
			case UP:
				depth = 1 - depth;
				top = 1 - top;
				bottom = 1 - bottom;

			case DOWN:
				vertices.addVertex(model, left, depth, top)
						.setColor(255, 255, 255, 255)
						.setUv(0.f, 1.f)
						.setLight(light);
				vertices.addVertex(model, left, depth, bottom)
						.setColor(255, 255, 255, 255)
						.setUv(1.f, 1.f)
						.setLight(light);
				vertices.addVertex(model, right, depth, bottom)
						.setColor(255, 255, 255, 255)
						.setUv(0.f, 0.f)
						.setLight(light);
				vertices.addVertex(model, right, depth, top)
						.setColor(255, 255, 255, 255)
						.setUv(1.f, 0.f)
						.setLight(light);
				break;

			case EAST:
				depth = 1 - depth;
				left = 1 - left;
				right = 1 - right;

			case WEST:
				vertices.addVertex(model, depth, top, left)
						.setColor(255, 255, 255, 255)
						.setUv(0.f, 1.f)
						.setLight(light);
				vertices.addVertex(model, depth, bottom, left)
						.setColor(255, 255, 255, 255)
						.setUv(0.f, 0.f)
						.setLight(light);
				vertices.addVertex(model, depth, bottom, right)
						.setColor(255, 255, 255, 255)
						.setUv(1.f, 0.f)
						.setLight(light);
				vertices.addVertex(model, depth, top, right)
						.setColor(255, 255, 255, 255)
						.setUv(1.f, 1.f)
						.setLight(light);
				break;

			case SOUTH:
				depth = 1 - depth;
				left = 1 - left;
				right = 1 - right;

			case NORTH:
				vertices.addVertex(model, 1 - left, top, depth)
						.setColor(255, 255, 255, 255)
						.setUv(mirror ? 0.f : 1.f, 0.f)
						.setLight(light);
				vertices.addVertex(model, 1 - left, bottom, depth)
						.setColor(255, 255, 255, 255)
						.setUv(mirror ? 0.f : 1.f, 1.f)
						.setLight(light);
				vertices.addVertex(model, 1 - right, bottom, depth)
						.setColor(255, 255, 255, 255)
						.setUv(mirror ? 1.f : 0.f, 1.f)
						.setLight(light);
				vertices.addVertex(model, 1 - right, top, depth)
						.setColor(255, 255, 255, 255)
						.setUv(mirror ? 1.f : 0.f, 0.f)
						.setLight(light);
				break;
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable T extractArgument(ItemStack stack) {
		return (T) stack.get(AurorasCanvasRegistry.CANVAS_COMPONENT_TYPE);
	}

	public static class Simple extends CanvasItemRenderer<SimpleCanvasHolder> {
		@Override
		public @Nullable SimpleCanvasHolder extractArgument(ItemStack stack) {
			return stack.get(AurorasCanvasRegistry.CANVAS_COMPONENT_TYPE);
		}
	}

	public static class Glass extends CanvasItemRenderer<GlassCanvasHolder> {
		@Override
		public @Nullable GlassCanvasHolder extractArgument(ItemStack stack) {
			return stack.get(AurorasCanvasRegistry.GLASS_CANVAS_COMPONENT_TYPE);
		}
	}

	public record UnbakedSimple() implements SpecialModelRenderer.Unbaked<SimpleCanvasHolder> {
		public static final UnbakedSimple INSTANCE = new UnbakedSimple();
		public static final MapCodec<UnbakedSimple> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public SpecialModelRenderer<SimpleCanvasHolder> bake(BakingContext context) {
			return new CanvasItemRenderer.Simple();
		}

		@Override
		public MapCodec<UnbakedSimple> type() {
			return MAP_CODEC;
		}
	}

	public record UnbakedGlass() implements SpecialModelRenderer.Unbaked<GlassCanvasHolder> {
		public static final UnbakedGlass INSTANCE = new UnbakedGlass();
		public static final MapCodec<UnbakedGlass> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public SpecialModelRenderer<GlassCanvasHolder> bake(BakingContext context) {
			return new CanvasItemRenderer.Glass();
		}

		@Override
		public MapCodec<UnbakedGlass> type() {
			return MAP_CODEC;
		}
	}
}
