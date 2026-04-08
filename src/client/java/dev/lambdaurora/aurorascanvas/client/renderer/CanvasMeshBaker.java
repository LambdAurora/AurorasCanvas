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
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

@Environment(EnvType.CLIENT)
public final class CanvasMeshBaker {
	private static final Identifier WHITE_SPRITE_ID = AurorasCanvas.id("block/blackboard/special/white");

	private CanvasMeshBaker() {
		throw new UnsupportedOperationException("CanvasMesher only contains static definitions.");
	}

	public static Mesh buildMesh(Canvas canvas, Direction facing, int light) {
		var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(WHITE_SPRITE_ID);
		var renderer = RendererAccess.INSTANCE.getRenderer();

		var meshBuilder = renderer.meshBuilder();
		var emitter = meshBuilder.getEmitter();

		var lit = light != 0;

		var material = renderer.materialFinder()
				.disableDiffuse(lit)
				.ambientOcclusion(lit ? TriState.FALSE : TriState.DEFAULT)
				.find();
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int color = canvas.getColor(x, y);
				if (color != 0) {
					{
						int red = color & 255;
						int green = (color >> 8) & 255;
						int blue = (color >> 16) & 255;
						color = 0xff000000 | (red << 16) | (green << 8) | blue;
					}

					int squareY = 15 - y;
					emitter.square(
									facing,
									x / 16.f, squareY / 16.f,
									(x + 1) / 16.f, (squareY + 1) / 16.f,
									0.928f
							)
							.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
							.color(color, color, color, color)
							.material(material);
					if (light != 0)
						emitter.lightmap(light, light, light, light);
					emitter.emit();
				}
			}
		}

		return meshBuilder.build();
	}
}
