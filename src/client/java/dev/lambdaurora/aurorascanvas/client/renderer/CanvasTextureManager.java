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
import dev.lambdaurora.aurorascanvas.client.CanvasClientUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

/// Represents the canvas texture manager.
///
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.0.0
@Environment(EnvType.CLIENT)
public final class CanvasTextureManager implements AutoCloseable {
	private final Int2ObjectMap<CanvasInstance> canvases = new Int2ObjectOpenHashMap<>();

	private TextureManager textureManager() {
		return Minecraft.getInstance().getTextureManager();
	}

	public void update(final Canvas data) {
		int id = data.pixelsHashCode();
		this.getOrCreateMapInstance(id, data).forceUpload();
	}

	public Identifier prepareCanvasTexture(final Canvas data) {
		int id = data.pixelsHashCode();
		var mapInstance = this.getOrCreateMapInstance(id, data);
		mapInstance.updateTextureIfNeeded();
		return mapInstance.location;
	}

	public void resetData() {
		this.canvases.values().forEach(CanvasInstance::close);
		this.canvases.clear();
	}

	private CanvasInstance getOrCreateMapInstance(final int id, final Canvas data) {
		return this.canvases.compute(id, (key, instance) -> {
			if (instance == null) {
				return new CanvasInstance(key, data);
			}

			instance.replaceCanvas(data);
			return instance;
		});
	}

	@Override
	public void close() {
		this.resetData();
	}

	private class CanvasInstance implements AutoCloseable {
		private Canvas data;
		private final DynamicTexture texture;
		private boolean requiresUpload = true;
		private final Identifier location;

		private CanvasInstance(final int id, final Canvas data) {
			this.data = data;
			this.texture = new DynamicTexture(() -> "Canvas " + id, 16, 16, true);
			this.location = AurorasCanvas.id("canvas/" + id);
			CanvasTextureManager.this.textureManager().register(this.location, this.texture);
		}

		private void replaceCanvas(final Canvas data) {
			boolean dataChanged = this.data != data;
			this.data = data;
			this.requiresUpload |= dataChanged;
		}

		public void forceUpload() {
			this.requiresUpload = true;
		}

		private void updateTextureIfNeeded() {
			if (this.requiresUpload) {
				CanvasClientUtils.exportToImage(this.data, this.texture.getPixels());
				this.texture.upload();
				this.requiresUpload = false;
			}
		}

		@Override
		public void close() {
			this.texture.close();
		}
	}
}
