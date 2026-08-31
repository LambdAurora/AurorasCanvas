/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import dev.lambdaurora.aurorascanvas.item.tree.ItemTree;
import dev.lambdaurora.aurorascanvas.network.AurorasCanvasNetworking;
import dev.lambdaurora.aurorascanvas.network.CanvasEditSubmitPayload;
import dev.lambdaurora.aurorascanvas.network.PainterPaletteScrollPayload;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

/**
 * Represents the Aurora's Canvas mod.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class AurorasCanvas implements ModInitializer {
	public static final String NAMESPACE = "aurorascanvas";

	@Override
	public void onInitialize(ModContainer mod) {
		AurorasCanvasRegistry.init();

		ItemTree.init();

		ServerPlayNetworking.registerGlobalReceiver(
				CanvasEditSubmitPayload.TYPE,
				AurorasCanvasNetworking::handleCanvasSubmitEdit
		);
		ServerPlayNetworking.registerGlobalReceiver(
				PainterPaletteScrollPayload.TYPE,
				AurorasCanvasNetworking::handlePainterPaletteScroll
		);

		SupplementariesCompat.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}
}
