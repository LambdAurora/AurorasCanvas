/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.network.PainterPaletteScrollPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
			method = "onScroll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
			cancellable = true
	)
	private void onScroll(
			long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci,
			@Local(ordinal = 2) double scrollDelta
	) {
		var player = this.minecraft.player;

		if (player != null && player.isShiftKeyDown()) {
			var mainHandStack = player.getMainHandItem();
			var inventory = mainHandStack.get(AurorasCanvasRegistry.PAINTER_PALETTE_INVENTORY_COMPONENT_TYPE);

			if (inventory != null) {
				if (!inventory.isPaletteEmpty()) {
					var payload = new PainterPaletteScrollPayload(scrollDelta, Screen.hasControlDown());
					ClientPlayNetworking.send(payload);

					ci.cancel();
				}
			}
		}
	}
}
