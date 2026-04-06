/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.renderer;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a canvas texture.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public final class CanvasTexture {
	private static final CanvasTextureLRUCache TEXTURE_CACHE = new CanvasTextureLRUCache(64);
	private static final Deque<CanvasTexture> UNUSED_TEXTURE_CACHE = new ArrayDeque<>();

	private final DynamicTexture texture = new DynamicTexture(16, 16, true);
	private final RenderType renderType;

	public CanvasTexture() {
		var id = Minecraft.getInstance().getTextureManager().register(AurorasCanvas.NAMESPACE + "/canvas", this.texture);
		this.renderType = RenderType.text(id);
	}

	public static CanvasTexture fromCanvas(Canvas canvas) {
		return TEXTURE_CACHE.computeIfAbsent(canvas, newBlackboard -> {
			var texture = getOrCreateTexture();
			texture.update(newBlackboard);
			return texture;
		});
	}

	public static CanvasTexture getOrCreateTexture() {
		if (UNUSED_TEXTURE_CACHE.isEmpty()) {
			return new CanvasTexture();
		} else {
			synchronized (UNUSED_TEXTURE_CACHE) {
				return UNUSED_TEXTURE_CACHE.pop();
			}
		}
	}

	public static void cacheTexture(CanvasTexture texture) {
		synchronized (UNUSED_TEXTURE_CACHE) {
			UNUSED_TEXTURE_CACHE.push(texture);
		}
	}

	public void render(Matrix4f model, MultiBufferSource vertexConsumers, int light, boolean mirror) {
		var vertices = vertexConsumers.getBuffer(this.renderType);
		vertices.vertex(model, mirror ? 1.f : 0.f, 1.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 1.f : 0.f, 1.f).uv2(light).endVertex();
		vertices.vertex(model, mirror ? 0.f : 1.f, 1.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 0.f : 1.f, 1.f).uv2(light).endVertex();
		vertices.vertex(model, mirror ? 0.f : 1.f, 0.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 0.f : 1.f, 0.f).uv2(light).endVertex();
		vertices.vertex(model, mirror ? 1.f : 0.f, 0.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 1.f : 0.f, 0.f).uv2(light).endVertex();
	}

	public void update(Canvas blackboard) {
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				this.texture.getPixels().setPixelRGBA(x, y, blackboard.getColor(x, y));
			}
		}
		this.texture.upload();
	}

	static class CanvasTextureLRUCache extends LinkedHashMap<Canvas, CanvasTexture> {
		private final int capacity;

		public CanvasTextureLRUCache(int capacity) {
			this.capacity = capacity;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Canvas, CanvasTexture> eldest) {
			if (this.size() > capacity) {
				cacheTexture(eldest.getValue());
				return true;
			}
			return false;
		}
	}
}
