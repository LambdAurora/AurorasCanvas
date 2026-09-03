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
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

@Environment(EnvType.CLIENT)
public final class CanvasMeshBaker {
	private static final Identifier WHITE_SPRITE_ID = AurorasCanvas.id("canvas/special/white");

	private CanvasMeshBaker() {
		throw new UnsupportedOperationException("CanvasMesher only contains static definitions.");
	}

	public static Mesh buildMesh(PlacedCanvas canvas) {
		var sprite = Minecraft.getInstance().getAtlasManager().get(Sheets.BLOCKS_MAPPER.apply(WHITE_SPRITE_ID));
		var renderer = Renderer.get();

		var meshBuilder = renderer.mutableMesh();
		var emitter = meshBuilder.emitter();

		int light = canvas.isGlowing() ? LightCoordsUtil.FULL_BRIGHT : 0;

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int color = canvas.getColor(x, y);
				if (color != 0) {
					int squareY = 15 - y;
					emitter.square(
									canvas.facing(),
									x / 16.f, squareY / 16.f,
									(x + 1) / 16.f, (squareY + 1) / 16.f,
									canvas.depth()
							)
							.atlas(QuadAtlas.BLOCK)
							.materialBake(new Material.Baked(sprite, false), MutableQuadView.BAKE_LOCK_UV)
							.color(color, color, color, color)
							.ambientOcclusion(canvas.isGlowing() ? TriState.FALSE : TriState.DEFAULT)
							.diffuseShade(!canvas.isGlowing());
					if (canvas.isGlowing())
						emitter.lightmap(light, light, light, light);
					emitter.emit();
				}
			}
		}

		return meshBuilder.immutableCopy();
	}
}
